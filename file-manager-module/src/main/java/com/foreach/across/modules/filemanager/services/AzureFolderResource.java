package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

public class AzureFolderResource implements FolderResource
{
	private final FolderDescriptor descriptor;
	private final CloudBlobClient blobClient;
	private final String containerName;
	private final String directoryName;

	public AzureFolderResource( @NonNull FolderDescriptor descriptor,
	                            @NonNull CloudBlobClient cloudBlobClient,
	                            @NonNull String containerName,
	                            @NonNull String directoryName ) {
		this.descriptor = descriptor;
		this.blobClient = cloudBlobClient;
		this.containerName = containerName;
		this.directoryName = directoryName;
	}

	@Override
	public FolderDescriptor getDescriptor() {
		return descriptor;
	}

	public CloudBlobClient getBlobClient() {
		return blobClient;
	}

	public String getContainerName() {
		return containerName;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return descriptor.getParentFolderDescriptor()
		                 .map( fd -> new AzureFolderResource( fd, blobClient, containerName, extractParentObjectName() ) );
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		if ( relativePath.endsWith( "/" ) ) {
			FolderDescriptor descriptor = this.descriptor.createFolderDescriptor( relativePath );
			String childPath = stripCurrentFolderId( descriptor.getFolderId() );
			String childObjectName = Paths.get( directoryName, childPath ).toString() + "/";

			return new AzureFolderResource( descriptor, blobClient, containerName, childObjectName );
		}

		FileDescriptor descriptor = this.descriptor.createFileDescriptor( relativePath );
		String childPath = stripCurrentFolderId( descriptor.getFolderId() );
		String childObjectName = Paths.get( directoryName, childPath, descriptor.getFileId() ).toString();

		return new AzureFileResource( descriptor, blobClient, containerName, childObjectName );
	}

	@Override
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		Set<FileRepositoryResource> resources = new LinkedHashSet<>();
		AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
		String p = StringUtils.startsWith( pattern, "/" ) ? pattern.substring( 1 ) : pattern;
		boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

		if ( matchOnlyDirectories ) {
			p = p.substring( 0, p.length() - 1 );
		}

		BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
			if ( pathMatcher.match( antPattern, antPattern.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" ) ) ) {
				return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
			}
			return false;
		};

		addAllMatchingResources( directoryName, resources, keyMatcher, p );
		return resources;
	}

	@SneakyThrows
	private void addAllMatchingResources( String prefix,
	                                      Collection<FileRepositoryResource> resources,
	                                      BiPredicate<String, String> keyMatcher,
	                                      String pattern ) {
		for ( ListBlobItem candidate : blobClient.getContainerReference( containerName ).listBlobs( prefix ) ) {
			if ( candidate instanceof CloudBlockBlob ) {
				CloudBlockBlob listedBlob = (CloudBlockBlob) candidate;
				String objectName = listedBlob.getName();
				if ( !objectName.equals( directoryName ) && !objectName.endsWith( "/" ) && keyMatcher.test( objectName, directoryName + pattern ) ) {
					resources.add( buildResourceFromListBlobItem( candidate ) );
				}
			}
			if ( candidate instanceof CloudBlobDirectory ) {
				CloudBlobDirectory listedDirectory = (CloudBlobDirectory) candidate;
				String objectName = listedDirectory.getPrefix();
				if ( keyMatcher.test( objectName, directoryName + pattern ) ) {
					resources.add( buildResourceFromListBlobItem( candidate ) );
				}
				if ( keyMatcher.test( objectName, directoryName + getRootPattern( pattern ) ) ) {
					addAllMatchingResources( objectName, resources, keyMatcher, pattern );
				}
			}
		}
	}

	private String getRootPattern( String pattern ) {
		if ( pattern.contains( "/" ) ) {
			return pattern.substring( 0, pattern.lastIndexOf( '/' ) );
		}
		return pattern;
	}

	private FileRepositoryResource buildResourceFromListBlobItem( ListBlobItem listBlobItem ) {
		if ( listBlobItem instanceof CloudBlockBlob ) {
			CloudBlockBlob listedBlob = (CloudBlockBlob) listBlobItem;
			String objectName = listedBlob.getName();
			String path = StringUtils.removeStart( objectName, directoryName );
			return new AzureFileResource( descriptor.createFileDescriptor( path ), blobClient, containerName, objectName );
		}
		if ( listBlobItem instanceof CloudBlobDirectory ) {
			CloudBlobDirectory listedDirectory = (CloudBlobDirectory) listBlobItem;
			String objectName = listedDirectory.getPrefix();
			String path = StringUtils.removeStart( objectName, directoryName );
			return new AzureFolderResource( descriptor.createFolderDescriptor( path ), blobClient, containerName, objectName );
		}

		throw new FileStorageException( "Unsupported ListBlobItem type: " + listBlobItem.getClass() );
	}

	@Override
	public boolean delete( boolean deleteChildren ) {
		boolean deleted = false;
		if ( deleteChildren ) {
			deleted = deleteChildren();
		}
		if ( isNotRootFolder() ) {
			deleted = true;
		}
		return deleted;
	}

	@Override
	public boolean deleteChildren() {
		Collection<FileRepositoryResource> resources = findResources( "*" );
		if ( !resources.isEmpty() ) {
			resources.forEach( r -> {
				if ( r instanceof FileResource ) {
					( (FileResource) r ).delete();
				}
				else {
					( (FolderResource) r ).delete( true );
				}
			} );
			return true;
		}
		return false;
	}

	@Override
	public boolean create() {
		return isNotRootFolder();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		return o != null && ( o instanceof FolderResource && descriptor.equals( ( (FolderResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public String toString() {
		return "axfs [" + descriptor.toString() + "] -> "
				+ String.format( "Azure storage blob resource[container='%s', blob='%s']", containerName, directoryName );
	}

	private String stripCurrentFolderId( String folderId ) {
		return StringUtils.defaultString( descriptor.getFolderId() != null
				                                  ? StringUtils.removeStart( folderId, descriptor.getFolderId() )
				                                  : folderId );
	}

	private String extractParentObjectName() {
		Path parent = Paths.get( directoryName ).getParent();
		return parent != null ? parent.toString().replaceAll( "\\\\", "/" ) + "/" : "";
	}

	private boolean isNotRootFolder() {
		return !"".equals( directoryName );
	}
}
