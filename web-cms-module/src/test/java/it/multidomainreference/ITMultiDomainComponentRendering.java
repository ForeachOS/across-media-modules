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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
class ITMultiDomainComponentRendering extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;

	@BeforeEach
	void setUp() {
		if ( html == null ) {
			html = html( "http://foreach.be/render-components" );
		}
	}

	@Test
	void verifyPageAndComponentsHaveBeenInstalled() {
		val foreachBe = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		assertNotNull( foreachBe );

		val page = pageService.findByCanonicalPathAndDomain( "/render-components", foreachBe )
		                      .orElse( null );
		assertNotNull( page );

		assertEquals( "wcm:asset:page:render-components", page.getObjectId() );
		assertEquals( "Render components", page.getTitle() );
		assertEquals( "th/test/pages/render-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "render-components", page.getPathSegment() );
		assertEquals( "/render-components", page.getCanonicalPath() );
		assertNull( page.getParent() );

		val content = componentModelService.getComponentModelByNameAndDomain( "content", page, page.getDomain(), TextWebCmsComponentModel.class );
		assertNotNull( content );
		assertEquals( "content", content.getName() );
		assertEquals( "Content", content.getTitle() );
		assertEquals( "Page component: content 2", content.getContent() );

		val custom = componentModelService.getComponentModelByNameAndDomain( "custom", page, page.getDomain(), TextWebCmsComponentModel.class );
		assertNotNull( custom );
		assertEquals( "custom", custom.getName() );
		assertEquals( "Custom", custom.getTitle() );
		assertEquals( "Page component: custom", custom.getContent() );
	}

	@Test
	void ifComponentIsNotFoundTheRegularMarkupIsRendered() {
		html.assertElementHasText( "Not found - default markup.", "#not-found-default-markup" );
	}

	@Test
	void markupIsAlwaysReplacedEvenIfNoComponentWhenAttributeIsPresent() {
		html.assertElementIsEmpty( "#not-found-replaced" );
	}

	@Test
	void componentFoundInDefaultScope() {
		html.assertElementHasText( "Page component: custom", "#found-in-page" );
	}

	@Test
	void parentScopesAreSearchedIfNoScopeAndNotExplicitlyDenied() {
		html.assertElementHasText( "Global component: footer", "#found-in-global" );
	}

	@Test
	void componentFoundInLowestScopeTakesPrecedence() {
		html.assertElementHasText( "Page component: content 2", "#shadowing-global" );
	}

	@Test
	void specifiedScopeIsAlwaysUsed() {
		html.assertElementHasText( "Global component: content", "#using-global" );
	}

	@Test
	void assetScopeIsAliasForPage() {
		html.assertElementHasText( "Page component: custom", "#using-asset" );
	}

	@Test
	void domainScopeIsDomainBoundObjects() {
		html.assertElementHasText( "Global component: content (BE)", "#using-domain" );
	}

	@Test
	void notSearchingParentScopesIfExplicitlyDenied() {
		html.assertElementHasText( "Not found in scope and not searching parents.", "#not-found-in-scope" );
	}

	@Test
	void notSearchParentScopesIfScopeSpecifiedAndNotExplicitlyEnabled() {
		html.assertElementHasText( "Not found in scope specified and not searching parents.", "#not-found-in-scope-specified" );
	}

	@Test
	void searchingParentScopesIfScopeSpecifiedButExplicitlyEnabled() {
		html.assertElementHasText( "Global component: footer", "#found-by-search" );
	}

	@Test
	void selfClosingTagIsSupported() {
		html.assertElementHasText( "Page component: custom", "#self-closing-tag" );
	}
}
