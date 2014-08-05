package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;

import java.util.List;

public interface ImageResolutionManager
{
	ImageResolution getById( int resolutionId );

	List<ImageResolution> getForContext( int contextId );

	List<ImageResolution> getAllResolutions();

	void saveResolution( ImageResolution resolution );

	ImageResolution getByDimensions( int width, int height );
}
