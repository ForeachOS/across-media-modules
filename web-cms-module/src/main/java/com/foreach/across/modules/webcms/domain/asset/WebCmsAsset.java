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

package com.foreach.across.modules.webcms.domain.asset;

import com.foreach.across.modules.hibernate.business.SettableIdAuditableEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Generic base class for an identifiable asset in the WebCmsModule.  An asset is of a particular type - identified by the subclass -
 * and has an automatic long id along with a manually set {@link #assetKey}.
 * <p/>
 * An asset implements the {@link com.foreach.across.modules.hibernate.business.Auditable} interface.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_asset")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "asset_type", discriminatorType = DiscriminatorType.STRING)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class WebCmsAsset<T extends WebCmsAsset<T>> extends SettableIdAuditableEntity<T>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_asset_id")
	@GenericGenerator(
			name = "seq_wcm_asset_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_asset_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "5")
			}
	)
	private Long id;

	/**
	 * Globally unique key for this asset. Alternative for the generated id property as the key should be set manually.
	 * By default a new UUID will be used as the unique key, consumer code should use {@link #isNew()} to determine if the
	 * asset is represented by a persisted entity or if it is new.
	 * <p/>
	 * Can be used for synchronization of assets between environments.
	 */
	@Column(name = "asset_key", unique = true)
	@NotBlank
	@Length(max = 255)
	private String assetKey = UUID.randomUUID().toString();

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId( Long id ) {
		this.id = id;
	}

	protected WebCmsAsset( Long id, Long newEntityId, String assetKey, String createdBy, Date createdDate, String lastModifiedBy, Date lastModifiedDate ) {
		setNewEntityId( newEntityId );
		setId( id );
		setCreatedBy( createdBy );
		setCreatedDate( createdDate );
		setLastModifiedBy( lastModifiedBy );
		setLastModifiedDate( lastModifiedDate );

		this.assetKey = assetKey;
	}

	protected WebCmsAsset() {
	}
}
