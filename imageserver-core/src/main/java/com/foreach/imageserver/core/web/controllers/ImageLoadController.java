package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.JsonResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

import java.util.Date;

@Controller
public class ImageLoadController extends BaseImageAPIController {

    public static final String LOAD_IMAGE_PATH = "load";
    public static final String IMAGE_EXISTS_PATH = "imageExists";

    @Value("${accessToken}")
    private String accessToken;

    @Autowired
    private ImageService imageService;

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {
        binder.registerCustomEditor(byte[].class, new ByteArrayMultipartFileEditor());
    }

    @RequestMapping(value = "/" + LOAD_IMAGE_PATH, method = RequestMethod.POST)
    @ResponseBody
    public JsonResponse load(@RequestParam(value = "token", required = true) String accessToken,
                             @RequestParam(value = "iid", required = true) String externalId,
                             @RequestParam(value = "imageData", required = true) byte[] imageData,
                             @RequestParam(value = "created", required = false) Integer createdTimestamp) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Date createdDate = (createdTimestamp != null) ? new Date(createdTimestamp) : new Date();

        Dimensions imageDimensions = imageService.saveImage(externalId, imageData, createdDate);

        return success(dto(imageDimensions));
    }

    @RequestMapping(value = "/" + IMAGE_EXISTS_PATH, method = RequestMethod.GET)
    @ResponseBody
    public JsonResponse exists(@RequestParam(value = "token", required = true) String accessToken,
                               @RequestParam(value = "iid", required = true) String externalId) {
        if (!this.accessToken.equals(accessToken)) {
            return error("Access denied.");
        }

        Image image = imageService.getByExternalId(externalId);

        return success(Boolean.valueOf(image != null));
    }

    private DimensionsDto dto(Dimensions dimensions) {
        DimensionsDto dto = new DimensionsDto();
        dto.setWidth(dimensions.getWidth());
        dto.setHeight(dimensions.getHeight());
        return dto;
    }

}

