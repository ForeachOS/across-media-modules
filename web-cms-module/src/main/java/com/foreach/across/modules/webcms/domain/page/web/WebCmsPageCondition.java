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
import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;

import static com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsConditionUtils.combineArrays;
import static com.foreach.across.modules.webcms.domain.endpoint.web.controllers.WebCmsConditionUtils.compareArrays;
import static org.apache.commons.lang3.ArrayUtils.contains;

/**
 * A condition to use for retrieving the correct {@link com.foreach.across.modules.webcms.domain.page.WebCmsPage}.  This condition takes all properties of
 * {@link WebCmsPageMapping} into account.
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@RequiredArgsConstructor
@Slf4j
final class WebCmsPageCondition extends AbstractCustomRequestCondition<WebCmsPageCondition>
{
	private final ConfigurableWebCmsEndpointContext context;
	private final WebCmsEndpointContextResolver resolver;

	private String[] canonicalPath = {};
	private String[] pageType = {};
	private String[] objectId = {};

	/**
	 * Set the values for this condition based on the attributes of the annotated element.
	 *
	 * @param annotatedElement this condition is attached to
	 */
	@Override
	public void setAnnotatedElement( AnnotatedElement annotatedElement ) {
		WebCmsPageMapping endpointMapping = AnnotatedElementUtils.findMergedAnnotation( annotatedElement, WebCmsPageMapping.class );

		canonicalPath = endpointMapping.canonicalPath();
		pageType = endpointMapping.pageType();
		objectId = endpointMapping.objectId();
	}

	@Override
	protected Collection<?> getContent() {
		return Arrays.asList( canonicalPath, pageType );
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

		result.canonicalPath = combineArrays( this.canonicalPath, other.canonicalPath );
		result.pageType = combineArrays( this.pageType, other.pageType );
		result.objectId = combineArrays( this.objectId, other.objectId );

		return result;
	}

	@Override
	public WebCmsPageCondition getMatchingCondition( HttpServletRequest request ) {
		if ( !context.isResolved() ) {
			resolver.resolve( context, request );
		}

		WebCmsAsset rawAsset = context.getEndpoint( WebCmsAssetEndpoint.class ).getAsset();
		if ( context.isOfType( WebCmsAssetEndpoint.class ) && WebCmsPage.class.isInstance( rawAsset ) ) {
			WebCmsPage page = (WebCmsPage) rawAsset;
			if ( !isValidCanonicalPath( page ) || !isValidPageType( page ) ) {
				return null;
			}
			LOG.trace( "Matching condition is {}", this );
			return this;
		}
		return null;
	}

	private boolean isValidPageType( WebCmsPage page ) {
		if ( pageType.length > 0 && page.getPageType() != null ) {
			return contains( pageType, page.getPageType().getTypeKey() ) ||
					contains( pageType, page.getPageType().getObjectId() );
		}
		return true;
	}

	private boolean isValidCanonicalPath( WebCmsPage page ) {
		if ( canonicalPath.length > 0 ) {
			return contains( this.canonicalPath, page.getCanonicalPath() );
		}
		return true;
	}

	@Override
	public int compareTo( WebCmsPageCondition other, HttpServletRequest request ) {
		int val = compareArrays( objectId, other.objectId );

		if ( val == 0 ) {
			val = compareArrays( canonicalPath, other.canonicalPath );
		}

		if ( val == 0 ) {
			val = compareArrays( pageType, other.pageType );
		}

		return val;
	}
}
