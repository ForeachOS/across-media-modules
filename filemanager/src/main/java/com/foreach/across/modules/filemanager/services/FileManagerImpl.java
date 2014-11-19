package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileStorageException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of both FileManager and FileRepositoryRegistry.  The registry deals out
 * delegate implementations ensuring that the actual implementation can be replaced at runtime.
 *
 * @see com.foreach.across.modules.filemanager.services.FileRepositoryDelegate
 */
@Service
public class FileManagerImpl implements FileManager, FileRepositoryRegistry
{
	private FileRepositoryFactory repositoryFactory;
	private Map<String, FileRepositoryDelegate> repositories = new HashMap<>();

	@Override
	public File createTempFile() {
		FileRepository tempRepository = requireRepository( TEMP_REPOSITORY );
		return tempRepository.getAsFile( tempRepository.createFile() );
	}

	@Override
	public String getRepositoryId() {
		return requireRepository( DEFAULT_REPOSITORY ).getRepositoryId();
	}

	@Override
	public FileDescriptor createFile() {
		return createFile( DEFAULT_REPOSITORY );
	}

	@Override
	public FileDescriptor moveInto( File file ) {
		return moveInto( DEFAULT_REPOSITORY, file );
	}

	@Override
	public FileDescriptor save( File file ) {
		return save( DEFAULT_REPOSITORY, file );
	}

	@Override
	public FileDescriptor save( InputStream inputStream ) {
		return save( DEFAULT_REPOSITORY, inputStream );
	}

	@Override
	public FileDescriptor createFile( String repositoryId ) {
		return requireRepository( repositoryId ).createFile();
	}

	@Override
	public FileDescriptor moveInto( String repositoryId, File file ) {
		return requireRepository( repositoryId ).moveInto( file );
	}

	@Override
	public FileDescriptor save( String repositoryId, File file ) {
		return requireRepository( repositoryId ).save( file );
	}

	@Override
	public FileDescriptor save( String repositoryId, InputStream inputStream ) {
		return requireRepository( repositoryId ).save( inputStream );
	}

	@Override
	public boolean delete( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).delete( descriptor );
	}

	@Override
	public OutputStream getOutputStream( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getOutputStream( descriptor );
	}

	@Override
	public InputStream getInputStream( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getInputStream( descriptor );
	}

	@Override
	public File getAsFile( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).getAsFile( descriptor );
	}

	@Override
	public boolean exists( FileDescriptor descriptor ) {
		return requireRepository( descriptor.getRepositoryId() ).exists( descriptor );
	}

	@Override
	public void setFileRepositoryFactory( FileRepositoryFactory factory ) {
		this.repositoryFactory = factory;
	}

	@Override
	public FileRepository getRepository( FileDescriptor descriptor ) {
		return getRepository( descriptor.getRepositoryId() );
	}

	@Override
	public FileRepository getRepository( String repositoryId ) {
		FileRepository repository = repositories.get( repositoryId );

		if ( repository == null ) {
			return createAndRegister( repositoryId );
		}

		return repository;
	}

	@Override
	public boolean repositoryExists( String repositoryId ) {
		return repositories.containsKey( repositoryId );
	}

	@Override
	public FileRepository registerRepository( FileRepository fileRepository ) {
		FileRepositoryDelegate delegate = repositories.get( fileRepository.getRepositoryId() );

		if ( delegate == null ) {
			delegate = new FileRepositoryDelegate();
			repositories.put( fileRepository.getRepositoryId(), delegate );
		}

		delegate.setActualImplementation( fileRepository );

		return delegate;
	}

	private FileRepository requireRepository( String repositoryId ) {
		FileRepository repository = getRepository( repositoryId );

		if ( repository == null ) {
			if ( repositoryFactory != null ) {
				throw new FileStorageException( String.format(
						"No FileRepository with id %s available.  The factory did not create an instance.",
						repositoryId ) );
			}

			throw new FileStorageException( String.format(
					"No FileRepository with id %s available, it has not been registered and no factory is available.",
					repositoryId ) );
		}

		return repository;
	}

	private FileRepository createAndRegister( String repositoryId ) {
		FileRepository repository = repositoryFactory.create( repositoryId );

		if ( repository != null ) {
			return registerRepository( repository );
		}

		return null;
	}
}