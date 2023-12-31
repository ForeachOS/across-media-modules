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

package com.foreach.across.modules.webcms.domain.component.config;

import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.bootstrapui.options.OptionIterableBuilder;
import com.foreach.across.modules.entity.views.processors.*;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.webcms.WebCmsEntityAttributes;
import com.foreach.across.modules.webcms.config.ConditionalOnAdminUI;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModelService;
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.text.TextWebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.web.ComponentTypeOptionIterableBuilder;
import com.foreach.across.modules.webcms.domain.component.web.ContainerMemberViewProcessor;
import com.foreach.across.modules.webcms.domain.component.web.SearchComponentViewProcessor;
import com.foreach.across.modules.webcms.domain.component.web.SingleWebCmsComponentFormProcessor;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import com.foreach.across.modules.webcms.domain.image.component.ImageWebCmsComponentModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.validation.groups.Default;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
class WebCmsComponentConfiguration
{
	@Autowired
	void registerSingleValuePropertyConverters( WebCmsDataConversionService conversionService ) {
		conversionService.registerSingleValueProperty( TextWebCmsComponentModel.class, "content" );
		conversionService.registerSingleValueProperty( ImageWebCmsComponentModel.class, "image" );
	}

	@ConditionalOnAdminUI
	@Configuration
	@RequiredArgsConstructor
	static class AdminUiConfiguration implements EntityConfigurer
	{
		private final ContainerMemberViewProcessor containerMemberViewProcessor;
		private final SingleWebCmsComponentFormProcessor formProcessor;
		private final SearchComponentViewProcessor searchComponentViewProcessor;
		private final WebCmsComponentModelService componentModelService;
		private final WebCmsMultiDomainConfiguration multiDomainConfiguration;
		//	private final ComponentTypeFormProcessor componentTypeFormProcessor;
		private final ComponentTypeOptionIterableBuilder componentTypeOptionIterableBuilder;

		@Override
		public void configure( EntitiesConfigurationBuilder entities ) {
			entities.create().entityType( PlaceholderWebCmsComponentModel.Metadata.class, true );
			entities.withType( WebCmsComponentType.class )
			        .properties(
					        props -> props.property( "name" ).order( 0 ).and()
					                      .property( "typeKey" ).order( 1 ).and()
					                      .property( "description" ).order( 2 )
			        )
			        .hide()
			        .updateFormView( fvb -> fvb.showProperties( "name", "typeKey", "description", "created", "lastModified" ) );
			//.listView( lvb -> lvb.showProperties( "name", "typeKey", "description", "lastModified" ) );

			String domainFilterEql = multiDomainConfiguration.isDomainBound( WebCmsComponent.class ) ? " and domain = selectedDomain()" : "";

			// Configure the globally shared components
			entities.withType( WebCmsComponent.class )
			        .entityModel( modelBuilder -> modelBuilder.saveMethod( componentModelService::save ) )
			        .properties(
					        props -> props.property( "componentType" ).order( 0 )
					                      .attribute( OptionIterableBuilder.class, componentTypeOptionIterableBuilder ).and()
					                      .property( "title" ).order( 1 ).and()
					                      .property( "name" ).order( 2 ).and()
					                      .property( "ownerObjectId" ).hidden( true ).and()
					                      .property( "sortIndex" ).order( 10 ).hidden( true ).and()
					                      .property( "objectId" ).writable( false ).hidden( true ).and()
					                      .property( "body" ).hidden( true ).and()
					                      .property( "metadata" ).hidden( true ).and()
					                      .property( "bodyWithContentMarkers" ).hidden( true ).and()
					                      .property( "proxyTarget" ).hidden( true )
			        )
			        .attribute(
					        SearchComponentViewProcessor.COMPONENT_SEARCH_QUERY,
					        "((title like '%{0}%' or name like '%{0}') and ownerObjectId is NULL)" + domainFilterEql
			        )
			        .attribute( WebCmsEntityAttributes.MultiDomainConfiguration.LIST_VIEW_ADJUSTED, true )
			        .listView(
					        lvb -> lvb.entityQueryFilter( true )
					                  .entityQueryPredicate( "(ownerObjectId is NULL)" + domainFilterEql )
					                  .showProperties( "title", "name", "componentType", "lastModified" )
					                  .defaultSort( "title" )
			        )
			        .createFormView(
					        fvb -> fvb
							        .messagePrefix( "views[createShared]", "views[createView]" )
							        .postProcess(
									        DefaultValidationViewProcessor.class,
									        processor -> processor.setValidationHints( Default.class, WebCmsComponent.SharedComponentValidation.class )
							        )
			        )
			        .formView(
					        "createMember",
					        fvb -> fvb.messagePrefix( "views[createMember]", "views[updateView]" )
					                  .viewProcessor( containerMemberViewProcessor )
					                  .postProcess( SingleEntityPageStructureViewProcessor.class,
					                                processor -> processor.setTitleMessageCode( EntityMessages.PAGE_TITLE_CREATE ) )
			        )
			        .deleteFormView( fvb -> fvb.viewProcessor( containerMemberViewProcessor ) )
			        .updateFormView(
					        fvb -> fvb.showProperties()
					                  .viewProcessor( formProcessor )
					                  .removeViewProcessor( SaveEntityViewProcessor.class.getName() )
					                  .removeViewProcessor( DefaultValidationViewProcessor.class.getName() )
					                  .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 12 ) ) )
			        )
			        .listView(
					        "search",
					        lvb -> lvb.showProperties( "title", "name", "componentType", "lastModified" )
					                  .sortableOn()
					                  .showResultNumber( false )
					                  .postProcess( ListFormViewProcessor.class, p -> p.setAddDefaultButtons( false ) )
					                  .postProcess( SortableTableRenderingViewProcessor.class, p -> p.setIncludeDefaultActions( false ) )
					                  .viewProcessor( searchComponentViewProcessor )
			        )
			        .association( ab -> ab.name( "webCmsComponent.proxyTarget" ).hide() );
		}
	}
}
