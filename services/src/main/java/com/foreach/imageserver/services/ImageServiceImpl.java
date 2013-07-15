package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Image;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.data.ImageDao;
import com.foreach.imageserver.services.repositories.RepositoryLookupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImageServiceImpl implements ImageService
{
	private static final Logger LOG = LoggerFactory.getLogger( ImageServiceImpl.class );

	@Autowired
	private ImageDao imageDao;

	@Autowired
	private ImageStoreService imageStoreService;

	@Autowired
	private ImageModificationService imageModificationService;

	public Image getImageByKey( String key, int applicationId ) {
		return imageDao.getImageByKey( key, applicationId );
	}

	@Transactional
	@Override
	public void save( Image image, RepositoryLookupResult lookupResult ) {
		image.setDimensions( lookupResult.getDimensions() );
		image.setImageType( lookupResult.getImageType() );

		boolean isInsert = isNewImage( image );

		if ( isInsert ) {
			image.setFilePath( imageStoreService.generateRelativeImagePath( image ) );
			imageDao.insertImage( image );
		}

		ImageFile savedFile = imageStoreService.saveImage( image, lookupResult.getContent() );

		image.setFileSize( savedFile.getFileSize() );

		imageDao.updateImage( image );
		if ( !isInsert ) {
			imageStoreService.deleteVariants( image );
		}
	}

	private boolean isNewImage( Image image ) {
		return image.getId() <= 0;
	}

	@Override
	public ImageFile fetchImageFile( Image image, ImageModifier modifier ) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Requesting image {} with modifier {}", image.getId(), modifier );
		}

		ImageFile file = imageStoreService.getImageFile( image, modifier );

		if ( file == null ) {
			ImageFile original = imageStoreService.getImageFile( image );
			ImageFile modified = imageModificationService.apply( original, modifier );

			file = imageStoreService.saveImageFile( image, modifier, modified );
		}

		return file;
	}

	@Transactional
	@Override
	public void delete( Image image, boolean variantsOnly ) {
		if ( variantsOnly ) {
			// Delete physical variant files
			imageStoreService.deleteVariants( image );
		}
		else {
			// First delete the database entry - this avoids requests coming in
			imageDao.deleteImage( image.getId() );

			// Delete the actual physical files
			imageStoreService.delete( image );
		}
	}
}
