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

package it;

import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.webcms.WebCmsModule;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleTypeRepository;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentTypeRepository;
import com.foreach.across.modules.webcms.domain.publication.*;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierRepository;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;

import static com.foreach.across.modules.webcms.domain.article.QWebCmsArticleType.webCmsArticleType;
import static com.foreach.across.modules.webcms.domain.component.QWebCmsComponentType.webCmsComponentType;
import static com.foreach.across.modules.webcms.domain.publication.QWebCmsPublicationType.webCmsPublicationType;
import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class ITDefaultObjects
{
	@Test
	public void byDefaultTheAssetsShouldBeImported() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .build()) {
			verifyComponentTypes( ctx );
			verifyArticleTypes( ctx );
			verifyPublicationTypes( ctx );
			verifyPublications( ctx );
		}
	}

	private void verifyComponentTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsComponentTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsComponentTypeRepository.class );

		WebCmsComponentType textField = publicationTypeRepository.findOne( webCmsComponentType.objectId.eq( "wcm:type:component:text-field" ) );
		assertNotNull( textField );
		assertEquals( "wcm:type:component:text-field", textField.getObjectId() );
		assertEquals( "component", textField.getObjectType() );
		assertEquals( "Text field", textField.getName() );
		assertEquals( "text-field", textField.getTypeKey() );
		assertNotNull( textField.getDescription() );
		assertEquals( textField, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "component", "text-field" ) );

		WebCmsComponentType richText = publicationTypeRepository.findOne( webCmsComponentType.objectId.eq( "wcm:type:component:rich-text" ) );
		assertNotNull( richText );
		assertEquals( "wcm:type:component:rich-text", richText.getObjectId() );
		assertEquals( "component", richText.getObjectType() );
		assertEquals( "Rich text", richText.getName() );
		assertEquals( "rich-text", richText.getTypeKey() );
		assertNotNull( richText.getDescription() );
		assertEquals( richText, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "component", "rich-text" ) );
	}

	private void verifyArticleTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsArticleTypeRepository articleTypeRepository = beanRegistry.getBeanOfType( WebCmsArticleTypeRepository.class );

		WebCmsArticleType newsType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:news" ) );
		assertNotNull( newsType );
		assertEquals( "wcm:type:article:news", newsType.getObjectId() );
		assertEquals( "article", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( newsType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "article", "news" ) );

		WebCmsArticleType blogType = articleTypeRepository.findOne( webCmsArticleType.objectId.eq( "wcm:type:article:blog" ) );
		assertNotNull( blogType );
		assertEquals( "wcm:type:article:blog", blogType.getObjectId() );
		assertEquals( "article", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertEquals( blogType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "article", "blog" ) );
	}

	private void verifyPublicationTypes( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsTypeSpecifierRepository typeSpecifierRepository = beanRegistry.getBeanOfType( WebCmsTypeSpecifierRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );

		WebCmsPublicationType newsType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:news" ) );
		assertNotNull( newsType );
		assertEquals( "wcm:type:publication:news", newsType.getObjectId() );
		assertEquals( "publication", newsType.getObjectType() );
		assertEquals( "News", newsType.getName() );
		assertEquals( "news", newsType.getTypeKey() );
		assertEquals( newsType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "publication", "news" ) );

		WebCmsPublicationType blogType = publicationTypeRepository.findOne( webCmsPublicationType.objectId.eq( "wcm:type:publication:blog" ) );
		assertNotNull( blogType );
		assertEquals( "wcm:type:publication:blog", blogType.getObjectId() );
		assertEquals( "publication", blogType.getObjectType() );
		assertEquals( "Blog", blogType.getName() );
		assertEquals( "blog", blogType.getTypeKey() );
		assertEquals( blogType, typeSpecifierRepository.findOneByObjectTypeAndTypeKey( "publication", "blog" ) );
	}

	private void verifyPublications( AcrossContextBeanRegistry beanRegistry ) {
		WebCmsPublicationRepository publicationRepository = beanRegistry.getBeanOfType( WebCmsPublicationRepository.class );
		WebCmsPublicationTypeRepository publicationTypeRepository = beanRegistry.getBeanOfType( WebCmsPublicationTypeRepository.class );

		WebCmsPublication news = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:news" ) );
		assertEquals( "wcm:asset:publication:news", news.getObjectId() );
		assertEquals( "News", news.getName() );
		assertEquals( "news", news.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKey( "news" ), news.getPublicationType() );

		WebCmsPublication blogs = publicationRepository.findOne( QWebCmsPublication.webCmsPublication.objectId.eq( "wcm:asset:publication:blogs" ) );
		assertEquals( "wcm:asset:publication:blogs", blogs.getObjectId() );
		assertEquals( "Blogs", blogs.getName() );
		assertEquals( "blogs", blogs.getPublicationKey() );
		assertEquals( publicationTypeRepository.findOneByTypeKey( "blog" ), blogs.getPublicationType() );
	}

	@Test
	public void disablingTheDefaultAssetsInstaller() {
		try (AcrossTestContext ctx = web().modules( WebCmsModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .property( "webCmsModule.default-data.assets.enabled", "false" )
		                                  .build()) {
			WebCmsPublicationRepository publicationRepository = ctx.getBeanOfType( WebCmsPublicationRepository.class );
			assertEquals( 0, publicationRepository.count() );
		}
	}
}
