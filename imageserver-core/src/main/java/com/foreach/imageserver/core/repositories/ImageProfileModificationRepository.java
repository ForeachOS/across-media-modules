package com.foreach.imageserver.core.repositories;

import com.foreach.across.modules.hibernate.repositories.BasicRepository;
import com.foreach.imageserver.core.business.ImageProfileModification;

public interface ImageProfileModificationRepository extends BasicRepository<ImageProfileModification>
{
	ImageProfileModification getModification( long profileId,
	                                          long contextId,
	                                          long resolutionId );
}