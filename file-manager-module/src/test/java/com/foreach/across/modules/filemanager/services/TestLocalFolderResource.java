package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.*;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@ExtendWith(MockitoExtension.class)
class TestLocalFolderResource
{
	@Mock
	private LocalFileRepository repository;

	private File tempDir;
	private FolderDescriptor descriptor;
	private FolderResource resource;

	private LocalFolderResource childFolder;
	private LocalFolderResource childFolderInChildFolder;
	private LocalFileResource childFile;
	private LocalFileResource childFileInChildFolder;

	@BeforeEach
	@SneakyThrows
	void createResource() {
		descriptor = FolderDescriptor.of( "my-repo", "123/456" );
		tempDir = Paths.get( System.getProperty( "java.io.tmpdir" ), UUID.randomUUID().toString(), UUID.randomUUID().toString() ).toFile();
		resource = new LocalFolderResource( repository, descriptor, tempDir );
	}

	@AfterEach
	void tearDown() {
		try {
			FileUtils.deleteDirectory( tempDir.getParentFile() );
		}
		catch ( Exception ignore ) {
		}
	}

	@Test
	void descriptor() {
		assertThat( resource.getDescriptor() ).isEqualTo( descriptor );
	}

	@Test
	void uri() {
		assertThat( resource.getURI() ).isEqualTo( descriptor.toResourceURI() );
	}

	@Test
	void equals() {
		assertThat( resource )
				.isEqualTo( resource )
				.isNotEqualTo( mock( Resource.class ) )
				.isEqualTo( new LocalFolderResource( repository, descriptor, tempDir ) )
				.isNotEqualTo( new LocalFolderResource( repository, FolderDescriptor.of( "1:2/" ), tempDir ) );
	}

	@Test
	void parentFolderResource() {
		LocalFolderResource rootFolder = new LocalFolderResource( repository, FolderDescriptor.rootFolder( "my-repo" ), tempDir );
		assertThat( rootFolder.getParentFolderResource() ).isEmpty();

		when( repository.getFolderResource( FolderDescriptor.of( "my-repo", "123" ) ) ).thenReturn( rootFolder );
		assertThat( resource.getParentFolderResource() ).contains( rootFolder );
	}

	@Test
	void exists() {
		assertThat( resource.exists() ).isFalse();
		assertThat( tempDir.mkdirs() ).isTrue();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void create() {
		assertThat( tempDir.exists() ).isFalse();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.create() ).isTrue();
		assertThat( tempDir.exists() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.create() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	@SneakyThrows
	void existingFileDoesNotCountAsFolder() {
		assertThat( tempDir.getParentFile().mkdir() ).isTrue();
		assertThat( tempDir.createNewFile() ).isTrue();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.create() ).isFalse();
		assertThat( resource.create() ).isFalse();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.delete( false ) ).isFalse();
	}

	@Test
	void deleteIfDirectoryEmpty() {
		assertThat( tempDir.mkdirs() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isTrue();
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.delete( false ) ).isFalse();
		assertThat( tempDir.exists() ).isFalse();

		assertThat( tempDir.mkdirs() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.delete( true ) ).isTrue();
		assertThat( resource.exists() ).isFalse();
	}

	@Test
	void deleteIfDirectoryNotEmpty() {
		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();
		assertThat( resource.delete( false ) ).isFalse();
		assertThat( tempDir.exists() ).isTrue();

		assertThat( resource.delete( true ) ).isTrue();
		assertThat( tempDir.exists() ).isFalse();
		assertThat( childFileInChildFolder.exists() ).isFalse();
	}

	@Test
	void deleteChildren() {
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.deleteChildren() ).isFalse();

		createFileTree();

		assertThat( resource.exists() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isTrue();

		assertThat( resource.deleteChildren() ).isTrue();
		assertThat( childFileInChildFolder.exists() ).isFalse();
		assertThat( resource.exists() ).isTrue();
	}

	@Test
	void findResourcesEmptyIfResourceNotExists() {
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.findResources( "*" ) ).isEmpty();
	}

	@Test
	void findResources() {
		createFileTree();

		assertThat( resource.findResources( "*" ) )
				.isNotEmpty()
				.hasSize( 2 )
				.containsExactlyInAnyOrder( childFile, childFolder )
				.isEqualTo( resource.findResources( "/*" ) );
		assertThat( resource.findResources( "**" ) )
				.isNotEmpty()
				.hasSize( 4 )
				.containsExactlyInAnyOrder( childFile, childFolder, childFolderInChildFolder, childFileInChildFolder )
				.isEqualTo( resource.findResources( "/**" ) );

		assertThat( resource.findResources( "*/*" ) ).containsExactlyInAnyOrder( childFolderInChildFolder, childFileInChildFolder );
		assertThat( resource.findResources( "*/" ) ).containsExactly( childFolder );
		assertThat( resource.findResources( "*/*/" ) ).containsExactly( childFolderInChildFolder );
		assertThat( resource.findResources( "**/" ) ).containsExactly( childFolder, childFolderInChildFolder );

		assertThat( resource.findResources( "*", FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.findResources( "*", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.findResources( "**", FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.findResources( "**", FolderResource.class ) ).containsExactlyInAnyOrder( childFolder, childFolderInChildFolder );
		assertThat( resource.findResources( "**/", FileResource.class ) ).isEmpty();
	}

	@Test
	void listChildren() {
		assertThat( resource.listChildren( false ) ).isEmpty();
		assertThat( resource.listChildren( true ) ).isEmpty();

		createFileTree();
		assertThat( resource.listChildren( false ) ).containsExactlyInAnyOrder( childFile, childFolder );
		assertThat( resource.listChildren( true ) )
				.containsExactlyInAnyOrder( childFile, childFolder, childFolderInChildFolder, childFileInChildFolder );

		assertThat( resource.listChildren( false, FileResource.class ) ).containsExactlyInAnyOrder( childFile );
		assertThat( resource.listChildren( false, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder );
		assertThat( resource.listChildren( true, FileResource.class ) ).containsExactlyInAnyOrder( childFile, childFileInChildFolder );
		assertThat( resource.listChildren( true, FolderResource.class ) ).containsExactlyInAnyOrder( childFolder, childFolderInChildFolder );
	}

	@Test
	void listFiles() {
		assertThat( resource.listFiles() ).isEmpty();

		createFileTree();
		assertThat( resource.listFiles() ).containsExactly( childFile );
	}

	@Test
	void listFolders() {
		assertThat( resource.listFolders() ).isEmpty();

		createFileTree();
		assertThat( resource.listFolders() ).containsExactly( childFolder );
	}

	@Test
	void emptyIfResourceNotExists() {
		assertThat( resource.exists() ).isFalse();
		assertThat( resource.isEmpty() ).isTrue();
	}

	@Test
	void notEmptyIfChildren() {
		assertThat( tempDir.mkdirs() ).isTrue();
		assertThat( resource.exists() ).isTrue();
		assertThat( resource.isEmpty() ).isTrue();

		createFileTree();
		assertThat( resource.isEmpty() ).isFalse();
	}

	@Test
	void getResource() {
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getResource( null ) );

		assertThat( resource.getResource( "" ) ).isSameAs( resource );
		assertThat( resource.getResource( "/" ) ).isSameAs( resource );

		FileRepositoryResource fileResource = resource.getResource( "childFile" );
		assertThat( fileResource ).isNotNull().isInstanceOf( FileResource.class ).isEqualTo( resource.getResource( "/childFile" ) );
		assertThat( fileResource.exists() ).isFalse();

		FileRepositoryResource folderResource = resource.getResource( "childFolder/" );
		assertThat( folderResource ).isNotNull().isInstanceOf( FolderResource.class ).isEqualTo( resource.getResource( "/childFolder/" ) );
		assertThat( folderResource.exists() ).isFalse();

		createFileTree();
		assertThat( fileResource.exists() ).isTrue();
		assertThat( folderResource.exists() ).isTrue();

		assertThat( resource.getResource( "childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFileInChildFolder" ).exists() ).isTrue();
		assertThat( resource.getResource( "childFolder/childFileInChildFolder/" ).exists() ).isFalse();
		assertThat( resource.getResource( "childFolder/childFolderInChildFolder" ).exists() ).isFalse();
		assertThat( resource.getResource( "childFolder/childFolderInChildFolder/" ).exists() ).isTrue();
		assertThat( resource.getResource( "/childFolder/childFolderInChildFolder/" ).exists() ).isTrue();

		FolderResource created = (FolderResource) resource.getResource( "/childFolder/childFolderInChildFolder/nestedFolder/" );
		assertThat( created.exists() ).isFalse();
		assertThat( created.create() ).isTrue();

		File createdFile = new File( childFileInChildFolder.getTargetFile().getParentFile(), "childFolderInChildFolder/nestedFolder" );
		assertThat( createdFile.exists() ).isTrue();
		assertThat( createdFile.isDirectory() ).isTrue();
	}

	@Test
	void getFileResource() {
		createFileTree();

		assertThat( resource.getFileResource( "childFile" ) ).isEqualTo( childFile ).matches( Resource::exists );
		assertThat( resource.getFileResource( "/childFile" ) ).isEqualTo( childFile );

		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "/" ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( null ) );
		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( "childFile/" ) );
	}

	@Test
	void getFolderResource() {
		createFileTree();

		assertThat( resource.getFolderResource( "childFolder" ) ).isEqualTo( childFolder ).matches( FileRepositoryResource::exists );
		assertThat( resource.getFolderResource( "/childFolder" ) ).isEqualTo( childFolder );
		assertThat( resource.getFolderResource( "/childFolder/" ) ).isEqualTo( childFolder );

		assertThatExceptionOfType( IllegalArgumentException.class ).isThrownBy( () -> resource.getFileResource( null ) );
		assertThat( resource.getFolderResource( "" ) ).isSameAs( resource );
		assertThat( resource.getFolderResource( "/" ) ).isSameAs( resource );

		assertThat( resource.getFolderResource( "/childFile" ) ).isNotEqualTo( childFile ).matches( r -> !r.exists() );
	}

	@Test
	void createFileResource() {
		FileResource one = resource.createFileResource();
		FileResource two = resource.createFileResource();
		assertThat( one ).isNotNull().isNotEqualTo( two );

		assertThat( one.getDescriptor().getFolderDescriptor() ).isEqualTo( resource.getDescriptor() );
		assertThat( two.getDescriptor().getFolderDescriptor() ).isEqualTo( resource.getDescriptor() );
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@SneakyThrows
	private void createFileTree() {
		tempDir.mkdirs();

		File child = new File( tempDir, "childFile" );
		child.createNewFile();
		childFile = new LocalFileResource( repository, FileDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId(), "childFile" ), child );
		assertThat( childFile.exists() ).isTrue();

		child = new File( tempDir, "childFolder" );
		child.mkdir();
		childFolder = new LocalFolderResource( repository,
		                                       FolderDescriptor.of( descriptor.getRepositoryId(), descriptor.getFolderId() + "/childFolder" ),
		                                       child );
		assertThat( childFolder.exists() ).isTrue();

		child = new File( child, "childFileInChildFolder" );
		child.createNewFile();
		childFileInChildFolder = new LocalFileResource(
				repository, FileDescriptor.of( descriptor.getRepositoryId(), childFolder.getDescriptor().getFolderId(), "childFileInChildFolder" ), child
		);
		assertThat( childFileInChildFolder.exists() ).isTrue();

		child = new File( childFileInChildFolder.getTargetFile().getParentFile(), "childFolderInChildFolder" );
		child.mkdir();
		childFolderInChildFolder = new LocalFolderResource( repository,
		                                                    FolderDescriptor.of( descriptor.getRepositoryId(),
		                                                                         childFolder.getDescriptor().getFolderId() + "/childFolderInChildFolder" ),
		                                                    child );
		assertThat( childFolderInChildFolder.exists() ).isTrue();
	}
}
