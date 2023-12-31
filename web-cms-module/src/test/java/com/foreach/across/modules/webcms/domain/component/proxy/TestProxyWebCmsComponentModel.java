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

package com.foreach.across.modules.webcms.domain.component.proxy;

import com.foreach.across.modules.webcms.domain.component.MyComponentMetadata;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
public class TestProxyWebCmsComponentModel
{
	@Mock
	private TextWebCmsComponentModel target;

	private ProxyWebCmsComponentModel proxy = new ProxyWebCmsComponentModel();

	@Test
	public void settingSelfAsProxyTargetIsBadIdea() {
		assertThrows( IllegalArgumentException.class, () -> {
			proxy.setTarget( proxy );
		} );
	}

	@Test
	public void settingAnotherProxyAsTargetIsNotAllowed() {
		assertThrows( IllegalArgumentException.class, () -> {
			proxy.setTarget( mock( ProxyWebCmsComponentModel.class ) );
		} );
	}

	@Test
	public void proxyIsEmptyIfNoTargetOrTargetIsEmpty() {
		assertNull( proxy.getTarget() );
		assertTrue( proxy.isEmpty() );
		assertFalse( proxy.hasTarget() );

		proxy.setTarget( target );
		assertSame( target, proxy.getTarget() );
		assertFalse( proxy.isEmpty() );
		assertTrue( proxy.hasTarget() );

		when( target.isEmpty() ).thenReturn( true );
		assertTrue( proxy.isEmpty() );
		assertTrue( proxy.hasTarget() );
	}

	@Test
	public void asTemplateShouldCopyTheSameTargetModel() {
		WebCmsComponentType componentType = new WebCmsComponentType();
		WebCmsComponent component = WebCmsComponent.builder()
		                                           .id( 123L )
		                                           .name( "component-name" )
		                                           .title( "My title" )
		                                           .componentType( componentType )
		                                           .ownerObjectId( "123" )
		                                           .build();

		proxy = new ProxyWebCmsComponentModel( component, target );
		MyComponentMetadata metadata = MyComponentMetadata.builder().title( "some title" ).number( 123L ).page( new WebCmsPage() ).build();
		proxy.setMetadata( metadata );
		assertFalse( proxy.isNew() );
		assertEquals( "component-name", proxy.getName() );
		assertEquals( "My title", proxy.getTitle() );
		assertEquals( "123", proxy.getOwnerObjectId() );
		assertSame( componentType, proxy.getComponentType() );
		assertSame( target, proxy.getTarget() );

		ProxyWebCmsComponentModel template = proxy.asComponentTemplate();
		assertNotEquals( proxy, template );
		assertTrue( template.isNew() );
		assertEquals( "component-name", template.getName() );
		assertEquals( "My title", template.getTitle() );
		assertNull( template.getOwnerObjectId() );
		assertSame( componentType, template.getComponentType() );
		assertSame( target, template.getTarget() );

		assertTrue( template.hasMetadata() );
		MyComponentMetadata cloned = template.getMetadata( MyComponentMetadata.class );
		assertNotSame( metadata, cloned );
		assertEquals( metadata.getTitle(), cloned.getTitle() );
		assertEquals( metadata.getNumber(), cloned.getNumber() );
		assertSame( metadata.getPage(), cloned.getPage() );
	}
}
