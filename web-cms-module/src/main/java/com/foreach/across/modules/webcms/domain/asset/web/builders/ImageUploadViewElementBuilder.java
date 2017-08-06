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

package com.foreach.across.modules.webcms.domain.asset.web.builders;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.webcms.domain.image.WebCmsImage;
import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * This builder builds a {@link ViewElement} for image upload for the current entity which is an {@link WebCmsImage}.
 * The generated element will be a row with two columns.  The first has a preview of the image, the second an upload button to upload or replace the image.
 */
@Deprecated
@Component
@AcrossDepends(required = BootstrapUiModule.NAME)
@RequiredArgsConstructor
@Slf4j
public class ImageUploadViewElementBuilder implements ViewElementBuilder<ViewElement>
{
	private final BootstrapUiFactory bootstrapUiFactory;
	private final WebCmsImageConnector imageConnector;

	@Override
	public ViewElement build( ViewElementBuilderContext viewElementBuilderContext ) {
		WebCmsImage image = EntityViewElementUtils.currentEntity( viewElementBuilderContext, WebCmsImage.class );

		if ( image.isNew() ) {
			// Only allow uploads for a new image
			return bootstrapUiFactory.file()
			                         .controlName( "extensions[image].imageData" )
			                         .build( viewElementBuilderContext );
		}

		String imageUrl = imageConnector.buildImageUrl( image, 800, 600 );
		return bootstrapUiFactory.div()
		                         .css( "image-preview-container" )
		                         .add(
				                         bootstrapUiFactory.node( "img" )
				                                           .attribute( "src", imageUrl )
		                         )
		                         .build( viewElementBuilderContext );

		/*
		ViewElement thumbnailPreview = image.getImageServerKey()
		                                    .map( imageServerKey -> {
			                                    String imageUrl = imageConnector.buildImageUrl( image, 200, 200 );
			                                    LOG.trace( "Rendering thumbnail of url {}", imageUrl );
			                                    return bootstrapUiFactory.node( "img" )
			                                                             .attribute( "src", imageUrl )
			                                                             .build( viewElementBuilderContext );
		                                    } )
		                                    .orElseGet( () -> {
			                                    LOG.trace( "No image yet for {}", image );
			                                    return bootstrapUiFactory.node( "div" )
			                                                             .add( bootstrapUiFactory.text( "Upload image.." ) )
			                                                             .build( viewElementBuilderContext );
		                                    } );

		LOG.trace( "Rendering image upload for owner {}", image );
		NodeViewElement fileUpload = new NodeViewElement( "input" );
		fileUpload.setAttribute( "type", "file" );
		fileUpload.addCssClass( "form-control" );
		fileUpload.setAttribute( "name", "extensions[image].imageData" );

		return bootstrapUiFactory.row()
		                         .add(
				                         bootstrapUiFactory.column( Grid.Device.MD.width( 6 ) ).add( thumbnailPreview ),
				                         bootstrapUiFactory.column( Grid.Device.MD.width( 6 ) ).add( fileUpload )
		                         )
		                         .build();
		*/
	}
}
