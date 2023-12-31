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

package com.foreach.across.modules.webcms.domain.image;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.web.WebCmsAssetType;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;
import java.util.Optional;

import static com.foreach.across.modules.webcms.domain.image.WebCmsImage.OBJECT_TYPE;

/**
 * A single image asset.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(OBJECT_TYPE)
@Table(name = "wcm_image")
@NoArgsConstructor
@Getter
@Setter
public class WebCmsImage extends WebCmsAsset<WebCmsImage> implements ImageOwner
{
	/**
	 * Object type name (discriminator value).
	 */
	public static final String OBJECT_TYPE = "image";

	/**
	 * Prefix that all object ids for a WebCmsImage have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:image";

	/**
	 * Name of the image.
	 */
	@NotBlank
	@Column(name = "name")
	@Length(max = 255)
	private String name;

	/**
	 * External system id of the image.
	 */
	@Column(name = "external_id")
	@Length(max = 255)
	private String externalId;

	/**
	 * The source of the image. (e.g. press agency)
	 */
	@Column(name = "source")
	@Length(max = 255)
	private String source;

	/**
	 * Description of the image.
	 */
	@Column(name = "description")
	private String description;

	/**
	 * Keywords of the image.
	 */
	@Column(name = "keywords")
	private String keywords;

	@Builder(toBuilder = true)
	protected WebCmsImage( @Builder.ObtainVia(method = "getId") Long id,
	                       @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                       @Builder.ObtainVia(method = "getObjectId") String objectId,
	                       @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                       @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                       @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                       @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                       @Builder.ObtainVia(method = "getDomain") WebCmsDomain domain,
	                       @Builder.ObtainVia(method = "isPublished") boolean published,
	                       @Builder.ObtainVia(method = "getPublicationDate") Date publicationDate,
	                       @Builder.ObtainVia(method = "getSortIndex") int sortIndex,
	                       String name,
	                       String externalId,
	                       String source,
	                       String description,
	                       String keywords ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, domain, published, publicationDate, sortIndex );
		this.name = name;
		this.externalId = externalId;
		this.source = source;
		this.description = description;
		this.keywords = keywords;
	}

	@Override
	public final String getObjectType() {
		return OBJECT_TYPE;
	}

	@Override
	protected final String getObjectCollectionId() {
		return COLLECTION_ID;
	}

	@Override
	public String toString() {
		return "WebCmsImage{" +
				"objectId='" + getObjectId() + "\'," +
				"externalId='" + name + '\'' +
				'}';
	}

	@Override
	public WebCmsAssetType getAssetType() {
		return null;
	}

	@Deprecated
	@Override
	public Optional<String> getImageServerKey() {
		return Optional.ofNullable( externalId );
	}

	@SuppressWarnings("findbugs:EI_EXPOSE_REP2")
	public static class WebCmsImageBuilder
	{
		private int sortIndex = 1000;
	}
}
