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

package com.foreach.across.modules.webcms.domain.article;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.image.ImageOwner;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Optional;

import static com.foreach.across.modules.webcms.domain.article.WebCmsArticle.OBJECT_TYPE;

/**
 * A single article from a {@link com.foreach.across.modules.webcms.domain.publication.WebCmsPublication}.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@DiscriminatorValue(OBJECT_TYPE)
@Table(name = "wcm_article")
@NoArgsConstructor
@Getter
@Setter
public class WebCmsArticle extends WebCmsAsset<WebCmsArticle> implements ImageOwner
{
	/**
	 * Object type name (discriminator value).
	 */
	public static final String OBJECT_TYPE = "article";

	/**
	 * Prefix that all object ids for a WebCmsArticle have.
	 */
	public static final String COLLECTION_ID = "wcm:asset:article";

	/**
	 * Publication this article belongs to.
	 */
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "publication_id")
	private WebCmsPublication publication;

	/**
	 * Title of the article. Used in previews, list views etc.
	 */
	@Column(name = "title")
	@NotBlank
	@Length(max = 255)
	private String title;

	/**
	 * Sub title of the article.
	 */
	@Column(name = "sub_title")
	@Length(max = 255)
	private String subTitle;

	/**
	 * Short description of the article contents.
	 */
	@Column(name = "description")
	@Length(max = 255)
	private String description;

	/**
	 * Temporary: body of the article.
	 */
	@Column(name = "body")
	private String body;

	/**
	 * Temporary: The image of this post
	 */
	@ManyToOne
	@JoinColumn(name = "image_id")
	private WebCmsImage image;

	@Builder(toBuilder = true)
	protected WebCmsArticle( @Builder.ObtainVia(method = "getId") Long id,
	                         @Builder.ObtainVia(method = "getNewEntityId") Long newEntityId,
	                         @Builder.ObtainVia(method = "getObjectId") String objectId,
	                         @Builder.ObtainVia(method = "getCreatedBy") String createdBy,
	                         @Builder.ObtainVia(method = "getCreatedDate") Date createdDate,
	                         @Builder.ObtainVia(method = "getLastModifiedBy") String lastModifiedBy,
	                         @Builder.ObtainVia(method = "getLastModifiedDate") Date lastModifiedDate,
	                         @Builder.ObtainVia(method = "isPublished") boolean published,
	                         @Builder.ObtainVia(method = "getPublicationDate") Date publicationDate,
	                         WebCmsPublication publication,
	                         String title,
	                         String subTitle,
	                         String description,
	                         String body ) {
		super( id, newEntityId, objectId, createdBy, createdDate, lastModifiedBy, lastModifiedDate, published, publicationDate );
		this.publication = publication;
		this.title = title;
		this.subTitle = subTitle;
		this.description = description;
		this.body = body;
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
		return "WebCmsArticle{" +
				"objectId='" + getObjectId() + "\'," +
				"title='" + title + '\'' +
				'}';
	}

	@Override
	public Optional<String> getImageServerKey() {
		return image != null ? Optional.ofNullable( image.getExternalId() ) : Optional.empty();
	}
}