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
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.bootstrapui.elements.TableViewElement;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.web.asset.builders.ImageUploadViewElementBuilder;
import com.foreach.across.modules.webcms.web.asset.processors.WebCmsImageFormViewProcessor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
@AcrossDepends(required = BootstrapUiModule.NAME)
public class WebCmsImageConfiguration implements EntityConfigurer
{
	private final WebCmsImageFormViewProcessor imageFormViewProcessor;
	private final ImageUploadViewElementBuilder thumbnailViewElementBuilder;
	private final ListViewProcessor listViewProcessor;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsImage.class )
		        .properties(
				        props -> props.property( "publish-settings" ).hidden( true ).and()
				                      .property( "objectId" ).hidden( true ).and()
				                      .property( "externalId" ).writable( false ).and()
				                      .property( "image-upload" )
				                      .displayName( "Image file" )
				                      .hidden( true )
				                      .writable( true )
				                      .readable( false )
				                      .viewElementBuilder(
						                      ViewElementMode.CONTROL,
						                      thumbnailViewElementBuilder
				                      )
		        )
		        .createOrUpdateFormView(
				        fvb -> fvb.showProperties( ".", "image-upload" )
				                  .postProcess( ( factory, processors ) -> {
					                  // ensure image uploading happens before saving the image record
					                  processors.addProcessor( imageFormViewProcessor, 0 );
				                  } )
		        )
		        .listView( lvb -> lvb.viewProcessor( listViewProcessor ) )

		;
	}

	@Component
	@AcrossDepends(required = BootstrapUiModule.NAME)
	private static class ListViewProcessor extends EntityViewProcessorAdapter
	{
		@Override
		protected void postRender( EntityViewRequest entityViewRequest,
		                           EntityView entityView,
		                           ContainerViewElement container,
		                           ViewElementBuilderContext builderContext ) {
			ContainerViewElementUtils.find( container, "table", TableViewElement.class )
			                         .ifPresent( table -> table.setCustomTemplate( "th/webCmsModule/test-admin-images :: content" ) );
		}
	}

	@Data
	static class ImageHolder
	{
		@NotNull
		private MultipartFile imageData;
	}
}