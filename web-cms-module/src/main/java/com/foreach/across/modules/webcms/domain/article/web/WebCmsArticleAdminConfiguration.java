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

package com.foreach.across.modules.webcms.domain.article.web;

import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.bootstrapui.elements.TextareaFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionFormElementBuilder;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.options.OptionIterableBuilder;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticleType;
import com.foreach.across.modules.webcms.domain.asset.web.processors.WebCmsAssetListViewProcessor;
import com.foreach.across.modules.webcms.domain.component.web.WebCmsComponentsFormProcessor;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublication;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLink;
import com.foreach.across.modules.webcms.domain.type.WebCmsTypeSpecifierLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@ConditionalOnAdminUI
@Configuration
public class WebCmsArticleAdminConfiguration
{
	@ConditionalOnAdminUI
	@Configuration
	@RequiredArgsConstructor
	static class ArticleAdminUi implements EntityConfigurer
	{
		private final WebCmsComponentsFormProcessor componentsFormProcessor;
		private final ArticleCreateFormProcessor createFormProcessor;
		private final ArticleUpdateFormProcessor updateFormProcessor;
		private final ArticleTypeForPublicationIterableBuilder articleTypeForPublicationIterableBuilder;

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			componentsFormProcessor.setComponentNames( "content" );

			entities.withType( WebCmsArticleType.class )
			        .association(
					        ab -> ab.name( "webCmsArticle.articleType" )
					                .listView( lvb -> lvb.showProperties( "publication", "title", "publicationDate", "lastModified" )
					                                     .viewProcessor( new WebCmsAssetListViewProcessor() ) )
			        );

			entities.withType( WebCmsArticle.class )
			        .properties( props -> props
					        .property( "objectId" ).hidden( true ).and()
					        .property( "body" ).hidden( true ).and()
					        .property( "image" ).hidden( true ).and()
					        .property( "title" ).viewElementType( ViewElementMode.CONTROL, TextareaFormElement.ELEMENT_TYPE ).and()
					        .property( "subTitle" ).viewElementType( ViewElementMode.CONTROL, TextareaFormElement.ELEMENT_TYPE ).and()
					        .property( "description" ).viewElementType( ViewElementMode.CONTROL, TextareaFormElement.ELEMENT_TYPE ).and()
					        .property( "publication" ).attribute( EntityAttributes.OPTIONS_ENTITY_QUERY, "published = TRUE" )
			        )
			        .listView(
					        lvb -> lvb.showProperties( "publication", "title", "articleType", "publicationDate", "lastModified" )
					                  .defaultSort( Sort.by( Sort.Direction.DESC, "lastModifiedDate" ) )
					                  .entityQueryPredicate( "publication.published = true" )
					                  .viewProcessor( new WebCmsArticleListViewProcessor() )
			        )
			        .createOrUpdateFormView(
					        fvb -> fvb.properties(
							        props -> props.property( "articleType" ).attribute( OptionIterableBuilder.class, articleTypeForPublicationIterableBuilder )
					        ) )
			        .createFormView(
					        fvb -> fvb.showProperties( ".", "~subTitle", "~publish-settings" )
					                  .viewProcessor( createFormProcessor )
			        )
			        .updateFormView( fvb -> fvb
					        .properties( props -> props
							        .property( "publication" ).writable( false ).and()
							        .property( "articleType" ).writable( false )
					        )
					        .showProperties( ".", "publication", "articleType" )
					        .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 9, 3 ) ) )
					        .viewProcessor( updateFormProcessor )
					        .viewProcessor( componentsFormProcessor )
			        );
		}
	}

	/**
	 * Selects the possible article types for a particular publication.
	 */
	@ConditionalOnAdminUI
	@Component
	@RequiredArgsConstructor
	static class ArticleTypeForPublicationIterableBuilder implements OptionIterableBuilder
	{
		private final WebCmsTypeSpecifierLinkRepository linkRepository;

		@Override
		public Iterable<OptionFormElementBuilder> buildOptions( ViewElementBuilderContext builderContext ) {
			List<OptionFormElementBuilder> options = new ArrayList<>();

			WebCmsArticle article = EntityViewElementUtils.currentEntity( builderContext, WebCmsArticle.class );
			WebCmsPublication publication = article.getPublication();

			if ( publication != null ) {
				linkRepository.findAllByOwnerObjectIdAndLinkTypeOrderBySortIndexAsc( publication.getPublicationType().getObjectId(),
				                                                                     WebCmsArticleType.OBJECT_TYPE )
				              .stream()
				              .map( WebCmsTypeSpecifierLink::getTypeSpecifier )
				              .forEach( type ->
						                        options.add( new OptionFormElementBuilder()
								                                     .rawValue( type )
								                                     .label( type.getName() )
								                                     .value( type.getId() ) )
				              );
			}

			return options;
		}
	}
}
