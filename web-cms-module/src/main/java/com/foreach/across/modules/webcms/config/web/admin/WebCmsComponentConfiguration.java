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

package com.foreach.across.modules.webcms.config.web.admin;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.Grid;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.processors.DefaultValidationViewProcessor;
import com.foreach.across.modules.entity.views.processors.SingleEntityFormViewProcessor;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponent;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.web.component.WebComponentFormProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.validation.groups.Default;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossDepends(required = AdminWebModule.NAME)
@Configuration
@RequiredArgsConstructor
class WebCmsComponentConfiguration implements EntityConfigurer
{
	private final WebComponentFormProcessor formProcessor;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsComponentType.class ).hide();

		entities.withType( WebCmsComponent.class )
		        .properties(
				        props -> props.property( "componentType" ).order( 0 ).and()
				                      .property( "title" ).order( 1 ).and()
				                      .property( "name" ).order( 2 ).and()
				                      .property( "ownerObjectId" ).hidden( true ).and()
				                      .property( "sortIndex" ).hidden( true ).and()
				                      .property( "objectId" ).writable( false ).hidden( true ).and()
				                      .property( "body" ).hidden( true ).and()
				                      .property( "metadata" ).hidden( true )
		        )
		        .listView(
				        lvb -> lvb.entityQueryFilter( true )
				                  .showProperties( "componentType", "title", "name", "lastModified" )
				                  .defaultSort( "title" )
		        )
		        .createOrUpdateFormView(
				        fvb -> fvb.postProcess(
						        DefaultValidationViewProcessor.class,
						        processor -> processor.setValidationHints( Default.class, WebCmsComponent.SharedComponentValidation.class )
				        )
		        )
		        .updateFormView(
				        fvb -> fvb.properties( props -> props.property( "componentType" ).writable( false ) )
				                  .showProperties( "componentType", "title", "name", "lastModified" )
				                  .viewProcessor( formProcessor )
				                  .postProcess( SingleEntityFormViewProcessor.class, processor -> processor.setGrid( Grid.create( 8, 4 ) ) )
		        );
	}
}