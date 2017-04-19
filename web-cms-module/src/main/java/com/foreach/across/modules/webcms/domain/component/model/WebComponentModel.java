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

package com.foreach.across.modules.webcms.domain.component.model;

import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

import java.util.Objects;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class WebComponentModel
{
	/**
	 * Original component this model represents.
	 */
	private WebCmsComponent component;

	/**
	 * Type of the WebCmsComponent.
	 */
	private WebCmsComponentType componentType;

	/**
	 * Unique object id of this component.
	 */
	private String objectId;

	/**
	 * Unique object id of the asset that owns this component.
	 * There is no actual referential integrity here, custom asset implementations must make sure they perform the required cleanup.
	 */
	private String ownerObjectId;

	/**
	 * Optional descriptive title of the component.
	 */
	private String title;

	/**
	 * Name of the component - if set must be unique within the owner.
	 */
	private String name;

	protected WebComponentModel( WebCmsComponent component ) {
		this.component = component;
		BeanUtils.copyProperties( component, this, "body", "metadata" );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		WebComponentModel that = (WebComponentModel) o;
		return Objects.equals( component, that.component ) &&
				Objects.equals( objectId, that.objectId );
	}

	@Override
	public int hashCode() {
		return Objects.hash( component, objectId );
	}

	@Override
	public String toString() {
		return "WebComponentModel{" +
				"componentType=" + componentType +
				", objectId='" + objectId + '\'' +
				'}';
	}
}
