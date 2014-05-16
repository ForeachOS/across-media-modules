package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;

import java.io.InputStream;

public interface ImageStoreService {
    void storeOriginalImage(Image image, byte[] imageBytes);

    void storeOriginalImage(Image image, InputStream imageStream);

    StreamImageSource getOriginalImage(Image image);

    void storeVariantImage(Image image, Context context, ImageModificationDto modification, ImageVariant imageVariant, InputStream imageStream);

    StreamImageSource getVariantImage(Image image, Context context, ImageModificationDto modification, ImageVariant imageVariant);

    void removeVariantImage(Image image, Context context, ImageModificationDto modification, ImageVariant imageVariant);

    void removeVariants(int imageId);
}
