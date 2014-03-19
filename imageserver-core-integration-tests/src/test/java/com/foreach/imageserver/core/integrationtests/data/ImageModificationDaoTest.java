package com.foreach.imageserver.core.integrationtests.data;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageModification;
import com.foreach.imageserver.core.data.ImageModificationDao;
import com.foreach.imageserver.core.integrationtests.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Date;

import static org.junit.Assert.*;

public class ImageModificationDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageModificationDao imageModificationDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void insertAndGetById() {
        String applicationSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(applicationSql, 1010, "the_application_code");

        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageSql, 9998, new Date(2012, 11, 13), "the_repository_code");

        String imageResolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageResolutionSql, 8, 1111, 2222);

        Crop writtenCrop = new Crop();
        writtenCrop.setX(100);
        writtenCrop.setY(101);
        writtenCrop.setWidth(102);
        writtenCrop.setHeight(103);

        Dimensions writtenDensity = new Dimensions();
        writtenDensity.setWidth(106);
        writtenDensity.setHeight(107);

        ImageModification writtenImageModification = new ImageModification();
        writtenImageModification.setImageId(9998);
        writtenImageModification.setContextId(1010);
        writtenImageModification.setResolutionId(8);
        writtenImageModification.setCrop(writtenCrop);
        writtenImageModification.setDensity(writtenDensity);

        imageModificationDao.insert(writtenImageModification);

        ImageModification readImageModification = imageModificationDao.getById(9998, 1010, 8);
        assertEquals(writtenImageModification.getImageId(), readImageModification.getImageId());
        assertEquals(writtenImageModification.getContextId(), readImageModification.getContextId());
        assertEquals(writtenImageModification.getResolutionId(), readImageModification.getResolutionId());
        assertEquals(writtenImageModification.getCrop().getX(), readImageModification.getCrop().getX());
        assertEquals(writtenImageModification.getCrop().getY(), readImageModification.getCrop().getY());
        assertEquals(writtenImageModification.getCrop().getWidth(), readImageModification.getCrop().getWidth());
        assertEquals(writtenImageModification.getCrop().getHeight(), readImageModification.getCrop().getHeight());
        assertEquals(writtenImageModification.getDensity().getWidth(), readImageModification.getDensity().getWidth());
        assertEquals(writtenImageModification.getDensity().getHeight(), readImageModification.getDensity().getHeight());
    }

    @Test
    public void hasModification() {
        String applicationSql = "INSERT INTO CONTEXT ( id, code ) VALUES ( ?, ? )";
        jdbcTemplate.update(applicationSql, 1, "the_application_code");

        String imageSql = "INSERT INTO IMAGE ( imageId, created, repositoryCode ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageSql, 1, new Date(2012, 11, 13), "the_repository_code");
        jdbcTemplate.update(imageSql, 2, new Date(2012, 11, 13), "the_repository_code");

        String imageResolutionSql = "INSERT INTO IMAGE_RESOLUTION ( id, width, height ) VALUES ( ?, ?, ? )";
        jdbcTemplate.update(imageResolutionSql, 1, 100, 200);

        imageModificationDao.insert(someModification(1, 1, 1));

        assertTrue(imageModificationDao.hasModification(1));
        assertFalse(imageModificationDao.hasModification(2));
    }

    private ImageModification someModification(int contextId, int imageId, int imageResolutionId) {
        Crop crop = new Crop();
        crop.setX(100);
        crop.setY(101);
        crop.setWidth(102);
        crop.setHeight(103);

        Dimensions density = new Dimensions();
        density.setWidth(106);
        density.setHeight(107);

        ImageModification modification = new ImageModification();
        modification.setImageId(imageId);
        modification.setContextId(contextId);
        modification.setResolutionId(imageResolutionId);
        modification.setCrop(crop);
        modification.setDensity(density);

        return modification;
    }

}
