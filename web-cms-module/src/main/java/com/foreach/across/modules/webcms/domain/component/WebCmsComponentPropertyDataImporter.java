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

package com.foreach.across.modules.webcms.domain.component;

import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.data.WebCmsPropertyDataImporter;
import com.foreach.across.modules.webcms.domain.WebCmsObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

/**
 * Supports assets or type specifiers to have the <strong>wcm:components</strong> property,
 * imports all components specified that way.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsComponentPropertyDataImporter implements WebCmsPropertyDataImporter<WebCmsObject>
{
	public static final String PROPERTY_NAME = "wcm:components";

	private final BeanFactory beanFactory;

	@Override
	public boolean supports( Phase phase,
	                         WebCmsDataEntry dataEntry, Object asset,
	                         WebCmsDataAction action ) {
		return Phase.AFTER_ASSET_SAVED.equals( phase ) && PROPERTY_NAME.equals( dataEntry.getParentKey() ) && asset instanceof WebCmsObject;
	}

	@Override
	public boolean importData( Phase phase,
	                           WebCmsDataEntry propertyData,
	                           WebCmsObject asset,
	                           WebCmsDataAction action ) {
		WebCmsComponentImporter componentImporter = beanFactory.getBean( WebCmsComponentImporter.class );
		componentImporter.setOwner( asset );

		componentImporter.importData( propertyData );

		return true;
	}

}
