package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;

import java.util.List;

public interface CropGenerator {
    Crop generateCrop(Image image, Context context, ImageResolution resolution, List<ImageModification> modifications);
}