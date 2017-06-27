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

package com.foreach.across.modules.webcms.domain.page.web;

import com.foreach.across.modules.web.mvc.condition.AbstractCustomRequestCondition;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.endpoint.web.controllers.InvalidWebCmsEndpointConditionCombination;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A condition to use for retrieving the correct {@link com.foreach.across.modules.webcms.domain.page.WebCmsPage}.  This condition takes all properties of
 * {@link WebCmsPageMapping} into account.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@RequiredArgsConstructor
@Slf4j
public class WebCmsPageCondition extends AbstractCustomRequestCondition<WebCmsPageCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;

	private Class<?> assetType;
	private String[] canonicalPath;
	private String[] pageType;

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsPageMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsPageMapping.class );

		canonicalPath = endpointMapping.canonicalPath();
		pageType = endpointMapping.pageTypes();
	}

	@Override
	protected Collection<?> getContent() {
		return Lists.asList( assetType, canonicalPath, pageType );
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

	@Override
	public WebCmsPageCondition combine( WebCmsPageCondition other ) {
		if ( !( context.isOfType( WebCmsAssetEndpoint.class ) ) ) {
			return null;
		}

		WebCmsPageCondition result = new WebCmsPageCondition( this.context, this.resolver );

		result.canonicalPath = combineStringArrays( this.canonicalPath, other.canonicalPath );
		result.pageType = combineStringArrays( this.pageType, other.pageType );

		return result;
	}

	private String[] combineStringArrays( String[] fromThis, String[] fromOther ) {
		if ( fromThis.length == 0 && fromOther.length == 0 ) {
			return new String[0];
		}

		if ( fromThis.length == 0 ) {
			return fromOther;
		}

		if ( fromOther.length == 0 ) {
			return fromThis;
		}

		List<String> combined = new ArrayList<String>();

		// check that "other" is more specific (being a subset) or equal to "this"
		for ( String otherIdentifier : fromOther ) {
			if ( !ArrayUtils.contains( fromThis, otherIdentifier ) ) {
				throw new InvalidWebCmsEndpointConditionCombination(
						String.format( "Current collection does not contain [%s]", otherIdentifier ) );
			}
			combined.add( otherIdentifier );
		}
		return combined.toArray( new String[combined.size()] );
	}

	@Override
	public WebCmsPageCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}
		if ( context.isOfType( WebCmsAssetEndpoint.class ) && assetType.isInstance( context.getEndpoint( WebCmsAssetEndpoint.class ).getAsset() ) ) {
			WebCmsPageCondition result = new WebCmsPageCondition( context, resolver );
			result.assetType = this.assetType;
			LOG.trace( "Matching condition is {}", result );
			return result;
		}
		return null;
	}

	@Override
	public int compareTo( WebCmsPageCondition other, HttpServletRequest request ) {
		if ( assetType != null && other.assetType != null && !assetType.equals( other.assetType ) ) {
			return assetType.isAssignableFrom( other.assetType ) ? 1 : -1;
		}
		return 0;
	}
}
