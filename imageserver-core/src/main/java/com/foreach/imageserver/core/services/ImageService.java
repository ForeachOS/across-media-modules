package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreException;
import com.foreach.imageserver.core.transformers.ImageSource;
import com.foreach.imageserver.dto.ImageConvertDto;
import com.foreach.imageserver.dto.ImageConvertResultDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface ImageService
{
	Logger LOG = LoggerFactory.getLogger( ImageService.class );

	Optional<Image> getById( long imageId );

	Image getByExternalId( String externalId );

	Image saveImage( String externalId, byte[] imageBytes, Date imageDate, boolean replaceExisting ) throws ImageStoreException;

	/**
	 * Creates a temporary Image object containing ImageAttributes and the image data.
	 *
	 * @param imageBytes the image
	 * @return image
	 */
	Image createImage( byte[] imageBytes );

	/**
	 * Load data for given image data.
	 *
	 * @param imageBytes the image
	 * @return image
	 */
	Image loadImageData( @NonNull byte[] imageBytes );

	/**
	 * Save a new image modification for an image.
	 * <p>
	 * <strong>WARNING:</strong> This will remove the existing variant images once done.  When saving more than one
	 * {@link ImageModification} use {@link #saveImageModifications(List, Image)} instead.
	 * </p>
	 *
	 * @param modification to register
	 * @param image        to add the modification to
	 */
	void saveImageModification( ImageModification modification, Image image );

	/**
	 * Registers a list of image modifications for an image.  This will remove the existing variant images once done.
	 *
	 * @param modifications to register
	 * @param image         to add the modifications to
	 */
	void saveImageModifications( List<ImageModification> modifications, Image image );

	ImageSource generateModification( Image image,
	                                  ImageModificationDto modificationDto,
	                                  ImageVariant imageVariant );

	ImageSource getVariantImage( Image image,
	                             ImageContext context,
	                             ImageResolution imageResolution,
	                             ImageVariant imageVariant );

	boolean hasModification( int imageId );

	Optional<ImageResolution> getResolution( long resolutionId );

	ImageResolution getResolution( int width, int height );

	List<ImageModification> getModifications( long imageId, long contextId );

	List<ImageModification> getAllModifications( long imageId );

	Collection<ImageResolution> getAllResolutions();

	void saveImageResolution( ImageResolution resolution );

	/**
	 * Delete the image registered under the external id.
	 *
	 * @param externalId image should be registered under
	 * @return true if image was deleted, false if not found
	 */
	boolean deleteImage( String externalId );

	/**
	 * Convert the supplied image using the given transformations.
	 *
	 * @param imageConvertDto object that contains the source image + all the desired targets and which transformations to apply for each target
	 * @return object containing all generated imaged
	 */
	ImageConvertResultDto convertImageToTargets( ImageConvertDto imageConvertDto );
}
