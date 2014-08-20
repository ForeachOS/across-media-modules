package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepositoryImpl;
import com.foreach.imageserver.core.business.ImageProfile;
import org.springframework.stereotype.Repository;

@Repository
public class ImageProfileRepositoryImpl extends BasicRepositoryImpl<ImageProfile> implements ImageProfileRepository
{
}
