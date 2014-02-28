package com.foreach.imageserver.data;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageModification;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface ImageModificationDao
{
	Collection<ImageModification> getModificationsForImage( int imageId );

	void insertModification( ImageModification modification );

	void updateModification( ImageModification modification );

	ImageModification getModification( @Param("imageId") int imageId, @Param("dimensions") Dimensions dimensions );

	void deleteModification( @Param("imageId") int imageId, @Param("dimensions") Dimensions dimensions );
}