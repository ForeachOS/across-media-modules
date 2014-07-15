package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.logging.LogHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageRepositoryServiceImpl implements ImageRepositoryService {

    private static Logger LOG = LoggerFactory.getLogger(ImageRepositoryServiceImpl.class);

    @Autowired
    private ImageRepositoryRegistry imageRepositoryRegistry;

    @Override
    public ImageRepository determineImageRepository(String code) {
        if (StringUtils.isBlank(code)) {
            LOG.warn("Null parameters not allowed - ImageRepositoryServiceImpl#determineImageRepository: code={}", code);
        }

        for (ImageRepository repository : imageRepositoryRegistry.getMembers()) {
            if (repository.getCode().equals(code)) {
                return repository;
            }
        }
        return null;
    }
}
