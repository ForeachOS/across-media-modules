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

package com.foreach.across.modules.webcms.domain.asset.web;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsConditionCombination;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsEndpointMapping;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A condition to use for retrieving the correct {@link WebCmsEndpoint}.  This condition takes all properties of
 * {@link WebCmsEndpointMapping} into account.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RequiredArgsConstructor
@Slf4j
public class WebCmsAssetCondition extends AbstractCustomRequestCondition<WebCmsAssetCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;
	private Class<?> assetType;
	protected String[] objectId = {};

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsAssetMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsAssetMapping.class );

		assetType = endpointMapping.value();
		objectId = endpointMapping.objectId();
	}

	@Override
	protected Collection<?> getContent() {
		return Arrays.asList( assetType, objectId );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	@Override
	public WebCmsAssetCondition combine( WebCmsAssetCondition other ) {
		WebCmsAssetCondition result = new WebCmsAssetCondition( this.context, this.resolver );
		if ( this.assetType.isAssignableFrom( other.assetType ) ) {
			result.assetType = other.assetType;
		}
		else if ( !other.assetType.isAssignableFrom( this.assetType ) ) {
			String message = String.format( "A condition with asset type %s and type %s cannot be merged", this.assetType,
			                                other.assetType );
			throw new InvalidWebCmsConditionCombination( message );
		}
		else {
			result.assetType = this.assetType;
		}

		result.objectId = combineObjectIds( other );

		return result;
	}

	private String[] combineObjectIds( WebCmsAssetCondition other ) {
		if ( this.objectId.length == 0 && other.objectId.length == 0 ) {
			return new String[0];
		}

		if ( this.objectId.length == 0 ) {
			return other.objectId;
		}

		if ( other.objectId.length == 0 ) {
			return this.objectId;
		}

		List<String> combined = new ArrayList<String>();

		// check that "other" is more specific (being a subset) or equal to "this"
		for ( String otherObjectId : other.objectId ) {
			if ( !ArrayUtils.contains( this.objectId, otherObjectId ) ) {
				throw new InvalidWebCmsConditionCombination(
						String.format( "Current objectid collection does not contain [%s]", otherObjectId ) );
			}
			combined.add( otherObjectId );
		}
		return combined.toArray( new String[combined.size()] );
	}

	@Override
	public WebCmsAssetCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}

		if ( !context.isOfType( WebCmsAssetEndpoint.class ) ) {
			return null;
		}

		WebCmsAsset asset = context.getEndpoint( WebCmsAssetEndpoint.class ).getAsset();
		WebCmsAssetCondition result = new WebCmsAssetCondition( context, resolver );

		if ( assetType.isInstance( asset ) ) {
			result.assetType = this.assetType;

			if ( this.objectId.length == 0 ) {
				return result;
			}

			if ( ArrayUtils.contains( this.objectId, asset.getObjectId() ) ) {
				result.objectId = this.objectId;
				LOG.trace( "Matching condition is [{}] with objectId [{}]", result.assetType, result.objectId );
				return result;
			}
		}
		return null;
	}

	@Override
	public int compareTo( WebCmsAssetCondition other, HttpServletRequest request ) {
		if ( objectId.length > 0 && other.objectId.length == 0 ) {
			return -1;
		}
		else if ( objectId.length == 0 && other.objectId.length > 0 ) {
			return 1;
		}
		else if ( objectId.length > 0 || other.objectId.length > 0 ) {
			return Integer.compare( objectId.length, other.objectId.length );
		}
		else if ( assetType != null && other.assetType != null && !assetType.equals( other.assetType ) ) {
			return assetType.isAssignableFrom( other.assetType ) ? 1 : -1;
		}

		return 0;
	}
}
