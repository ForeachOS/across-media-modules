package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * Still to verify and/or implement:
 * - Files.createTempPath needs to be atomic.
 * - Files.copy with option REPLACE_EXISTING should never cause the temp file to not exist.
 * - We should not store all images in one big folder. A hashing mechanism should be set up for this.
 * <p/>
 * TODO Resolve the above.
 */
@Service
public class ImageStoreServiceImpl implements ImageStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(ImageStoreServiceImpl.class);

    private final Path tempFolder;
    private final Path originalsFolder;
    private final Path variantsFolder;

    private final FileAttribute[] folderAttributes;
    private final FileAttribute[] fileAttributes;

    @Autowired
    public ImageStoreServiceImpl(@Value("${imagestore.folder}") File imageStoreFolder,
                                 @Value("${imagestore.permissions.folders}") String folderPermissions,
                                 @Value("${imagestore.permissions.files}") String filePermissions) throws IOException {
        folderAttributes = toFileAttributes(folderPermissions);
        fileAttributes = toFileAttributes(filePermissions);

        Path imageStoreFolderPath = imageStoreFolder.toPath();

        tempFolder = imageStoreFolderPath.resolve("temp");
        originalsFolder = imageStoreFolderPath.resolve("originals");
        variantsFolder = imageStoreFolderPath.resolve("variants");

        Files.createDirectories(tempFolder, folderAttributes);
        Files.createDirectories(originalsFolder, folderAttributes);
        Files.createDirectories(variantsFolder, folderAttributes);
    }

    @Override
    public void storeOriginalImage(Image image, byte[] imageBytes) {
        InputStream imageStream = null;
        try {
            imageStream = new ByteArrayInputStream(imageBytes);
            this.storeOriginalImage(image, imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    @Override
    public void storeOriginalImage(Image image, InputStream imageStream) {
        Path targetPath = getTargetPath(image);
        createFoldersSafely(targetPath.getParent());
        writeSafely(imageStream, targetPath);
    }

    @Override
    public StreamImageSource getOriginalImage(Image image) {
        Path targetPath = getTargetPath(image);
        return read(targetPath, image.getImageType());
    }

    @Override
    public void storeVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant, InputStream imageStream) {
        Path targetPath = getTargetPath(image, context, imageResolution, imageVariant);
        createFoldersSafely(targetPath.getParent());
        writeSafely(imageStream, targetPath);
    }

    @Override
    public StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        Path targetPath = getTargetPath(image, context, imageResolution, imageVariant);
        return read(targetPath, imageVariant.getOutputType());
    }

    @Override
    public void removeVariants(int imageId) {
        final String variantFileNamePrefix = variantFileNamePrefix(imageId);

        /**
         * Experiments have revealed that removing the same set of files from different threads
         * concurrently should work, provided that we ignore all IOExceptions. I could not quickly
         * find a good way around this; Files::walkFileTree and Files::deleteIfExists both raise
         * exceptions when the file they are trying to consider is suddenly missing.
         *
         * This was also tested on a linux server on an NFS mount.
         */
        try {
            Files.walkFileTree(variantsFolder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.getFileName().toString().startsWith(variantFileNamePrefix)) {
                        try {
                            Files.deleteIfExists(file);
                        } catch (IOException e) {
                            // Unfortunately, we need to ignore this error. (See comment above)
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // Unfortunately, we need to ignore this error. (See comment above)
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            // I'm not really sure whether this will ever happen, given that the above implementation catches all
            // io exceptions.
            LOG.error(String.format("Encountered failure while removing variants for image %d.", imageId), e);
            throw new ImageStoreException(e);
        }
    }

    private Path getTargetPath(Image image) {
        /**
         * We may at some point need image repositories that cannot re-retrieve their images. For this reason we
         * create a per-repository parent folder, so we can easily distinguish between repositories.
         */

        String fileName = constructFileName(image);
        String repositoryCode = image.getRepositoryCode();

        Path targetFolder = originalsFolder.resolve(repositoryCode);

        return targetFolder.resolve(fileName);
    }

    private Path getTargetPath(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        String fileName = constructFileName(image, imageResolution, imageVariant);

        Path targetFolder = variantsFolder.resolve(context.getCode());

        return targetFolder.resolve(fileName);
    }

    private String constructFileName(Image image, ImageResolution imageResolution, ImageVariant imageVariant) {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(variantFileNamePrefix(image.getImageId()));
        if (imageResolution.getWidth() != null) {
            fileNameBuilder.append('w');
            fileNameBuilder.append(imageResolution.getWidth());
            fileNameBuilder.append('-');
        }
        if (imageResolution.getHeight() != null) {
            fileNameBuilder.append('h');
            fileNameBuilder.append(imageResolution.getHeight());
        }
        fileNameBuilder.append('.');
        fileNameBuilder.append(imageVariant.getOutputType().getExtension());

        return fileNameBuilder.toString();
    }

    private String constructFileName(Image image) {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(image.getImageId());
        fileNameBuilder.append('.');
        fileNameBuilder.append(image.getImageType().getExtension());

        return fileNameBuilder.toString();
    }

    private String variantFileNamePrefix(int imageId) {
        StringBuilder prefixBuilder = new StringBuilder();
        prefixBuilder.append(imageId);
        prefixBuilder.append('-');
        return prefixBuilder.toString();
    }

    private void writeSafely(InputStream inputStream, Path targetPath) {
        try {
            Path temporaryPath = Files.createTempFile(tempFolder, "image", ".tmp", fileAttributes);
            Files.copy(inputStream, temporaryPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporaryPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private StreamImageSource read(Path targetPath, ImageType imageType) {
        // Do not use .exists() kind of logic here! Open the file and check for an exception instead.
        // This will ensure that we can read the full file contents, even should the file be deleted or replaced while
        // we are busy with it.

        InputStream imageStream = null;
        try {
            imageStream = Files.newInputStream(targetPath);
        } catch (IOException e) {
            // Let imageStream be null.
        }

        StreamImageSource imageSource = null;
        if (imageStream != null) {
            imageSource = new StreamImageSource(imageType, imageStream);
        }

        return imageSource;
    }

    private FileAttribute[] toFileAttributes(String permissions) {
        FileAttribute[] attributes = null;
        if (StringUtils.isNotBlank(permissions)) {
            FileAttribute attribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(permissions));
            attributes = new FileAttribute[1];
            attributes[0] = attribute;
        } else {
            attributes = new FileAttribute[0];
        }
        return attributes;
    }

    private void createFoldersSafely(Path path) {
        /**
         * Although I'm not entirely sure of this, I suspect that createDirectories might cause issues when multiple
         * actors try to create the same folder structure simultaneously. For this reason, we will simply retry
         * the creation a few times. This will most likely suffice as we know the folder structure will just be created
         * once and will be left untouched afterwards.
         */

        boolean done = false;

        for (int i = 0; i < 3 && !done; ++i) {
            done = createFoldersWithoutFailing(path);
            if (!done) {
                sleep(20);
            }
        }

        if (!done) {
            createFoldersAndFail(path);
        }
    }

    private boolean createFoldersWithoutFailing(Path path) {
        boolean done = false;
        try {
            Files.createDirectories(path, folderAttributes);
            done = true;
        } catch (IOException e) {
            // Ignore failure.
        }
        return done;
    }

    private void createFoldersAndFail(Path path) {
        try {
            Files.createDirectories(path, folderAttributes);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            // Ignore.
        }
    }

}
