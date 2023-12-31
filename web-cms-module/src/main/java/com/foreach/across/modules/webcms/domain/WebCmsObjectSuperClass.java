/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainBound;
import com.foreach.across.modules.webcms.infrastructure.WebCmsUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.UUID;

/**
 * Base implementation for a {@link WebCmsObject} entity that ensures that the object id is part of an object collection.
 * Requires the entity table to have a <strong>object_id</strong> column with a max length of 100.
 * <p/>
 * This is a base class for single entities, if you want a base class for an inheritance strategy, consider using
 * the {@link WebCmsObjectInheritanceSuperClass}.
 *
 * @author Arne Vandamme
 * @see WebCmsObjectInheritanceSuperClass
 * @since 0.0.1
 */
@Access(AccessType.FIELD)
@MappedSuperclass
public abstract class WebCmsObjectSuperClass<T extends WebCmsObjectSuperClass<T>> extends SettableIdAuditableEntity<T> implements WebCmsObject, WebCmsDomainBound
{
	/**
	 * Globally unique id for this object. Alternative for the generated id property as the key should be set manually.
	 * Consumer code should use {@link #isNew()} to determine if the object is represented by a persisted entity or if it is new.
	 * <p/>
	 * Can be used for synchronization of assets between environments.  Like the regular id the key should preferably
	 * never be modified after creation of an entity, as it determines the global identity of the asset.
	 */
	@Column(name = "object_id", unique = true)
	@NotBlank
	@Length(max = 100)
	private String objectId;

	/**
	 * The (optional) {@link WebCmsDomain} this object belongs to.
	 */
	@Getter
	@Setter
	@ManyToOne
	@JoinColumn(name = "domain_id")
	private WebCmsDomain domain;

	protected WebCmsObjectSuperClass() {
		setObjectId( UUID.randomUUID().toString() );
	}

	protected WebCmsObjectSuperClass( Long id,
	                                  Long newEntityId,
	                                  String objectId,
	                                  String createdBy,
	                                  Date createdDate,
	                                  String lastModifiedBy,
	                                  Date lastModifiedDate,
	                                  WebCmsDomain domain ) {
		setNewEntityId( newEntityId );
		setId( id );
		setCreatedBy( createdBy );
		if ( createdDate != null ) {
			setCreatedDate( new Date( createdDate.getTime() ) );
		}
		setLastModifiedBy( lastModifiedBy );
		if ( lastModifiedDate != null ) {
			setLastModifiedDate( new Date( lastModifiedDate.getTime() ) );
		}

		setObjectId( StringUtils.isNotEmpty( objectId ) ? objectId : UUID.randomUUID().toString() );
		setDomain( domain );
	}

	/**
	 * @return the globally unique object id
	 */
	@Override
	public String getObjectId() {
		return objectId;
	}

	/**
	 * Manually set the unique object id.  If the id set does not start with the collection id returned by {@link #getObjectCollectionId()},
	 * it will be prefixed.
	 *
	 * @param objectId to use
	 */
	public void setObjectId( String objectId ) {
		this.objectId = StringUtils.isEmpty( objectId ) ? null : WebCmsUtils.prefixObjectIdForCollection( objectId, getObjectCollectionId() );
	}

	/**
	 * @return the unique collection id these objects belong to, the object id will have the collection id as prefix
	 */
	protected abstract String getObjectCollectionId();
}
