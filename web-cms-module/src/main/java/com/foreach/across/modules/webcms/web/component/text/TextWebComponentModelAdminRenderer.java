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

package com.foreach.across.modules.webcms.web.component.text;

import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.webcms.domain.component.WebComponentModel;
import com.foreach.across.modules.webcms.domain.component.text.TextWebComponentModel;
import com.foreach.across.modules.webcms.web.component.WebComponentModelAdminRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@AcrossDepends(required = AdminWebModule.NAME)
@Component
@RequiredArgsConstructor
public class TextWebComponentModelAdminRenderer implements WebComponentModelAdminRenderer<TextWebComponentModel>
{
	private final BootstrapUiFactory bootstrapUiFactory;

	@Override
	public boolean supports( WebComponentModel componentModel ) {
		return TextWebComponentModel.class.isInstance( componentModel );
	}

	@Override
	public ViewElementBuilder createContentViewElementBuilder( TextWebComponentModel componentModel ) {
		return bootstrapUiFactory.formGroup()
		                         .label( "Content" )
		                         .control(
				                         bootstrapUiFactory.textbox()
				                                           .controlName( "extensions[componentModel].content" )
				                                           .multiLine( componentModel.isMultiLine() )
				                                           .text( componentModel.getContent() )
				                                           .attribute( "data-wcm-text-type", componentModel.getTextType() )
				                                           .attribute( "data-wcm-profile", componentModel.getProfile() )
		                         );

	}
}
