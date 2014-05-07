package com.foreach.imageserver.core.managers;

import com.foreach.imageserver.core.business.ImageResolution;

import java.util.List;

public interface ImageResolutionManager {
    ImageResolution getById(int resolutionId);

    List<ImageResolution> getForContext(int contextId);
}