package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static com.foreach.imageserver.core.config.ServicesConfiguration.*;

@Component
@Slf4j
public class DefaultImageFileDescriptorFactory implements ImageFileDescriptorFactory
{
	@Override
	public FileDescriptor createForOriginal( Image image ) {
		String fileName = constructFileName( image );
		String targetPath = getFolderName( image );
		String defaultTargetRepository = IMAGESERVER_ORIGINALS_REPOSITORY;

		if ( image.isTemporaryImage() ) {
			defaultTargetRepository = IMAGESERVER_TEMP_REPOSITORY;
		}

		return composeFileDescriptor( image, fileName, targetPath, defaultTargetRepository, null );
	}

	@Override
	public FileDescriptor createForVariant( Image image, ImageContext context, ImageResolution imageResolution, ImageVariant imageVariant ) {
		String fileName = generateFileName( image, context, imageResolution, imageVariant );
		String targetPath = getFolderName( image, context );

		return composeFileDescriptor( image, fileName, targetPath, IMAGESERVER_VARIANTS_REPOSITORY, context );
	}

	@Override
	public void removeVariantsForImageAndContext( FileManager fileManager, Image image, ImageContext imageContext ) {
		StopWatch sw = new StopWatch();
		sw.start();

		String lookupPath = StringUtils.replace(
				IMAGESERVER_VARIANTS_REPOSITORY + ":" + imageContext.getCode() + "/" + image.getVariantPath() + "/" + image.getId() + "-*.*", "//", "/" );
		Collection<FileResource> files = fileManager.findFiles( lookupPath );
		files.forEach( FileResource::delete );
		LOG.debug( "Deleted {} variants for {} and context {} in {} ms", files.size(), image, imageContext.getCode(), sw.getTime() );
	}

	private FileDescriptor composeFileDescriptor( Image image, String fileName, String targetPath, String defaultTargetRepository, ImageContext context ) {
		FileDescriptor fileDescriptor;
		String[] targetPathParts = targetPath.split( ":" );
		boolean isFileDescriptor = targetPathParts.length > 1;

		// When we are not dealing with a fileDescriptor (Legacy) --> Create one
		if ( isFileDescriptor ) {
			String fileRepository = targetPathParts[0];
			String fileDescriptorPath = targetPathParts[1];

			fileDescriptor = FileDescriptor.of( fileRepository, fileDescriptorPath, fileName );
		}
		else {
			fileDescriptor = FileDescriptor.of( defaultTargetRepository, targetPath, fileName );
		}

		return fileDescriptor;
	}

	private String constructFileName( Image image ) {
		String name = image.isTemporaryImage() ? image.getExternalId() : String.valueOf( image.getId() );
		return name + '.' + image.getImageType().getExtension();
	}

	private String getFolderName( Image image ) {
		return image.isTemporaryImage() ? "" : image.getOriginalPath();
	}

	private String getFolderName( Image image,
	                              ImageContext context ) {

		if ( image.getVariantPath().contains( ":" ) ) {
			return image.getVariantPath().replace( ":", StringUtils.join( ":", context.getCode(), "/" ) );
		}

		return context.getCode() + "/" + image.getVariantPath();
	}

	private String variantFileNamePrefix( long imageId ) {
		return String.valueOf( imageId ) + '-';
	}

}
