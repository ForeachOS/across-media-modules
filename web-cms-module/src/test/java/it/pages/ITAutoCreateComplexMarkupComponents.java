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

package it.pages;

import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import it.AbstractCmsApplicationWithTestDataIT;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 * @since 0.0.2
 */
class ITAutoCreateComplexMarkupComponents extends AbstractCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageService pageService;

	@Autowired
	private WebCmsComponentModelService componentModelService;

	private static Html html;
	private static WebCmsPage page;

	@BeforeEach
	void setUp() {
		if ( html == null ) {
			page = pageService.findByCanonicalPath( "/auto-create-complex-markup-components" )
			                  .orElse( null );
			verifyPageDoesButNoneOfTheComponentsExist( page );
			html = html( "/auto-create-complex-markup-components" );
		}
	}

	void verifyPageDoesButNoneOfTheComponentsExist( WebCmsPage page ) {
		assertNotNull( page );

		assertEquals( "wcm:asset:page:auto-create-complex-markup-components", page.getObjectId() );
		assertEquals( "Auto create complex markup components", page.getTitle() );
		assertEquals( "th/test/pages/auto-create/complex-markup-components", page.getTemplate() );
		assertTrue( page.isCanonicalPathGenerated() );
		assertFalse( page.isPathSegmentGenerated() );
		assertEquals( "auto-create-complex-markup-components", page.getPathSegment() );
		assertEquals( "/auto-create-complex-markup-components", page.getCanonicalPath() );
		assertNull( page.getParent() );

		assertTrue( componentModelService.getComponentModelsForOwner( page, null ).isEmpty() );
		assertNull( componentModelService.getComponentModelByName( "auto-created-for-include", null ) );
	}

	@Test
	void placeholdersInsideMarkupResultInPlaceholderMarkers() {
		html.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-placeholders" );

		val markup = componentModelService.getComponentModelByName( "markup-with-placeholders", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Default markup @@wcm:placeholder(one)@@ with @@wcm:placeholder(two)@@ content", markup.getContent() );
	}

	@Test
	void placeholdersInsideMarkupAreRenderedIfTheyHaveAnIncludeValue() {
		html.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-included-placeholders" );

		val markup = componentModelService.getComponentModelByName( "markup-with-included-placeholders", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Default markup <span>one</span> with @@wcm:placeholder(two)@@ content", markup.getContent() );
	}

	@Test
	void componentsInsideMarkupResultInComponentMarkers() {
		html.assertElementHasHTML( "Markup with Global component: footer and content Global component: content and not found: <div></div>",
		                           "#markup-with-components" );

		val markup = componentModelService.getComponentModelByName( "markup-with-components", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Markup with @@wcm:component(footer,default,true)@@ " +
				                              "and content @@wcm:component(content,global,false)@@ " +
				                              "and not found: <div>@@wcm:component(content,default,false)@@</div>", markup.getContent() );

		assertNull( componentModelService.getComponentModelByName( "content", page ) );
	}

	@Test
	void componentsInsideMarkupAreRenderedIfTheyHaveAnIncludeValue() {
		html.assertElementHasHTML(
				"Markup with Global component: footer and content Global component: content and not found: <div>not found</div>",
				"#markup-with-included-components"
		);

		val markup = componentModelService.getComponentModelByName( "markup-with-included-components", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Markup with Global component: footer " +
				                              "and content @@wcm:component(content,global,false)@@ " +
				                              "and not found: <div>not found</div>", markup.getContent() );
	}

	@Test
	void componentsInsideMarkupCanBeCreatedAutomaticallyAsWell() {
		val globalCreated = componentModelService.getComponentModelByName( "auto-created-for-include", null, TextWebCmsComponentModel.class );
		assertEquals( "Global component: auto-created for include", globalCreated.getContent() );

		val pageCreated = componentModelService.getComponentModelByName( "page-html-created-for-include", page, TextWebCmsComponentModel.class );
		assertEquals( "Page component: auto-created for include", pageCreated.getContent() );

		val markup = componentModelService.getComponentModelByName( "markup-with-auto-created-components", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Markup with @@wcm:component(auto-created-for-include,default,true)@@ " +
				                              "and content <div>Page component: auto-created for include</div>", markup.getContent() );

		html.assertElementHasHTML(
				"Markup with Global component: auto-created for include and content <div>Page component: auto-created for include</div>",
				"#markup-with-auto-created-components"
		);
	}

	@Test
	void componentsAndPlaceholdersCanBeCombinedInMarkup() {
		val markup = componentModelService.getComponentModelByName( "markup-with-placeholder-and-component", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Markup with @@wcm:component(footer,default,true)@@ and @@wcm:placeholder(two)@@", markup.getContent() );

		html.assertElementHasHTML( "Markup with Global component: footer and two", "#markup-with-placeholder-and-component" );
	}

	@Test
	void componentsAndPlaceholdersCanBeNested() {
		val componentTwo = componentModelService.getComponentModelByName( "component-two", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Component two: Placeholder two", componentTwo.getContent() );

		val componentOne = componentModelService.getComponentModelByName( "component-one", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Component one: @@wcm:placeholder(one)@@", componentOne.getContent() );

		val markup = componentModelService.getComponentModelByName( "markup-with-nested-placeholders-and-components", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "Root markup: @@wcm:component(component-one,default,true)@@", markup.getContent() );

		html.assertElementHasHTML(
				"Root markup: Component one: <div> Placeholder one: <div>  Component two: Placeholder two </div> </div>",
				"#markup-with-nested-placeholders-and-components"
		);
	}

	@Test
	void componentsInsidePlaceholdersAreAutoCreatedAndRendered() {
		val headerText = componentModelService.getComponentModelByName( "header-text-in-placeholder", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "new component", headerText.getContent() );

		val bodyInPlaceholder = componentModelService.getComponentModelByName( "body-in-placeholder", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "@@wcm:placeholder(body-text)@@", bodyInPlaceholder.getContent() );

		val markup = componentModelService.getComponentModelByName( "markup-with-component-in-placeholders", page, TextWebCmsComponentModel.class );
		assertEqualsIgnoreWhitespace( "@@wcm:placeholder(header)@@ @@wcm:component(body-in-placeholder,default,true)@@", markup.getContent() );

		html.assertElementHasHTML(
				"<div>Placeholder component 1: new component</div>Placeholder component 2:<div>Global component: footer</div>",
				"#markup-with-component-in-placeholders"
		);
	}

	@Test
	void secondRenderYieldsSameOutput() {
		Html secondRender = html( "/auto-create-complex-markup-components" );
		secondRender.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-placeholders" );
		secondRender.assertElementHasHTML( "Default markup <span>one</span> with two content", "#markup-with-included-placeholders" );
		secondRender.assertElementHasHTML(
				"Markup with Global component: footer and content Global component: content and not found: <div></div>",
				"#markup-with-components"
		);
		secondRender.assertElementHasHTML(
				"Markup with Global component: footer and content Global component: content and not found: <div>not found</div>",
				"#markup-with-included-components"
		);
		secondRender.assertElementHasHTML(
				"Markup with Global component: auto-created for include and content <div>Page component: auto-created for include</div>",
				"#markup-with-auto-created-components"
		);
		secondRender.assertElementHasHTML(
				"Root markup: Component one: <div> Placeholder one: <div>  Component two: Placeholder two </div> </div>",
				"#markup-with-nested-placeholders-and-components"
		);
		secondRender.assertElementHasHTML(
				"<div>Placeholder component 1: new component</div>Placeholder component 2:<div>Global component: footer</div>",
				"#markup-with-component-in-placeholders"
		);
	}
}
