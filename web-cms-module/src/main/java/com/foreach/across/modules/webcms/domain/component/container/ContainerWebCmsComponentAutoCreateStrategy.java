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

package com.foreach.across.modules.webcms.domain.component.container;

import com.foreach.across.modules.webcms.domain.component.WebCmsContentMarker;
import com.foreach.across.modules.webcms.domain.component.model.WebCmsComponentModel;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateService;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateStrategy;
import com.foreach.across.modules.webcms.domain.component.model.create.WebCmsComponentAutoCreateTask;
import com.foreach.across.modules.webcms.domain.component.placeholder.PlaceholderWebCmsComponentModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Responsible for auto-creation of default container models: adds members to the container.
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
class ContainerWebCmsComponentAutoCreateStrategy implements WebCmsComponentAutoCreateStrategy<ContainerWebCmsComponentModel>
{
	@Override
	public boolean supports( WebCmsComponentModel componentModel, WebCmsComponentAutoCreateTask task ) {
		return componentModel instanceof ContainerWebCmsComponentModel;
	}

	@Override
	public void buildComponentModel( WebCmsComponentAutoCreateService autoCreateService,
	                                 ContainerWebCmsComponentModel componentModel,
	                                 WebCmsComponentAutoCreateTask task ) {
		String markup = StringUtils.trimToEmpty( task.getOutput() );

		task.getChildren()
		    .forEach( childTask -> {
			    WebCmsComponentModel member = autoCreateService.buildComponent( childTask );

			    if ( member != null ) {
				    member.setOwner( componentModel );
				    componentModel.addMember( member );
			    }
		    } );

		markup = replacePlaceholdersByComponents( componentModel, markup );

		componentModel.setMarkup( markup );
	}

	private String replacePlaceholdersByComponents( ContainerWebCmsComponentModel componentModel, String originalMarkup ) {
		String markup = originalMarkup;

		for ( WebCmsComponentModel member : componentModel.getMembers() ) {
			if ( member instanceof PlaceholderWebCmsComponentModel ) {
				PlaceholderWebCmsComponentModel placeholderComponent = (PlaceholderWebCmsComponentModel) member;
				WebCmsContentMarker contentMarker = new WebCmsContentMarker( "wcm:placeholder", placeholderComponent.getPlaceholderName() );
				WebCmsContentMarker componentMarker = new WebCmsContentMarker( "wcm:component", member.getName() + ",container,false" );

				markup = StringUtils.replace( markup, contentMarker.toString(), componentMarker.toString() );
			}
		}

		return markup;
	}
}
