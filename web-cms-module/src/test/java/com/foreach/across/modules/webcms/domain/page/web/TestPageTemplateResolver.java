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

import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.WebCmsPageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestPageTemplateResolver
{
	private PageTemplateProperties properties;
	private PageTemplateResolver resolver;

	@BeforeEach
	public void setUp() {
		properties = new PageTemplateProperties();
		resolver = new PageTemplateResolver( properties );
	}

	@Test
	public void defaultTemplateIsReturnedForBlank() {
		assertEquals( PageTemplateProperties.DEFAULT_TEMPLATE, resolver.resolvePageTemplate( (String) null ) );
		assertEquals( PageTemplateProperties.DEFAULT_TEMPLATE, resolver.resolvePageTemplate( "" ) );

		properties.setDefaultTemplate( "default" );
		assertEquals( "default", resolver.resolvePageTemplate( (String) null ) );
		assertEquals( "default", resolver.resolvePageTemplate( "" ) );
	}

	@Test
	public void defaultTemplateForPageTypeReturnedIfNotMoreSpecific() {
		WebCmsPage page = WebCmsPage.builder().pageType( WebCmsPageType.builder().attribute( "template", "xyz" ).build() ).build();

		properties.setDefaultTemplate( "default" );
		assertEquals( "xyz", resolver.resolvePageTemplate( page ) );
	}

	@Test
	public void moreSpecificTemplateForPageTypeReturnedIfMoreSpecific() {
		WebCmsPage page = WebCmsPage.builder().
				template( "huphup" )
		                            .pageType( WebCmsPageType.builder().attribute( "template", "xyz" ).build() ).build();

		properties.setDefaultTemplate( "default" );
		assertEquals( "huphup", resolver.resolvePageTemplate( page ) );
	}

	@Test
	public void defaultReturnedIfNoDefaultTemplateSpecifiedOnWebCmsPageType() {
		WebCmsPage page = WebCmsPage.builder().pageType( WebCmsPageType.builder().build() ).build();

		properties.setDefaultTemplate( "default" );
		assertEquals( "default", resolver.resolvePageTemplate( page ) );
	}

	@Test
	public void noPrefixLeavesTemplateUntouched() {
		assertEquals( "template", resolver.resolvePageTemplate( "template" ) );
		assertEquals( "/template", resolver.resolvePageTemplate( "/template" ) );
	}

	@Test
	public void prefixIsAddedIfSpecified() {
		properties.setTemplatePrefix( "th/pages/" );
		assertEquals( "th/pages/template", resolver.resolvePageTemplate( "template" ) );
		assertEquals( "th/pages/template", resolver.resolvePageTemplate( "/template" ) );

		properties.setTemplatePrefix( "th/pages-" );
		assertEquals( "th/pages-template", resolver.resolvePageTemplate( "template" ) );
		assertEquals( "th/pages-/template", resolver.resolvePageTemplate( "/template" ) );
	}

	@Test
	public void templateIsUntouchedIfStartsWithReservedPrefix() {
		properties.setTemplatePrefix( "th/pages/" );

		assertEquals( "th/mytemplate", resolver.resolvePageTemplate( "th/mytemplate" ) );
		assertEquals( "th/pages/jsp/mytemplate", resolver.resolvePageTemplate( "jsp/mytemplate" ) );

		properties.setTemplatePrefixToIgnore( new String[] { "th/", "jsp/" } );
		assertEquals( "th/mytemplate", resolver.resolvePageTemplate( "th/mytemplate" ) );
		assertEquals( "jsp/mytemplate", resolver.resolvePageTemplate( "jsp/mytemplate" ) );
	}

	@Test
	public void suffixIsAlwaysStripped() {
		assertEquals( "mytemplate", resolver.resolvePageTemplate( "mytemplate.html" ) );

		properties.setTemplatePrefix( "th/pages/" );
		assertEquals( "th/pages/mytemplate", resolver.resolvePageTemplate( "mytemplate.html" ) );
		assertEquals( "th/pages/mytemplate.jsp", resolver.resolvePageTemplate( "mytemplate.jsp" ) );
		assertEquals( "th/mytemplate", resolver.resolvePageTemplate( "th/mytemplate.html" ) );

		properties.setTemplateSuffixToRemove( ".jsp" );
		assertEquals( "th/pages/mytemplate.html", resolver.resolvePageTemplate( "mytemplate.html" ) );
		assertEquals( "th/pages/mytemplate", resolver.resolvePageTemplate( "mytemplate.jsp" ) );
	}
}
