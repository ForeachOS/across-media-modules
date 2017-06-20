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

package com.foreach.across.modules.webcms.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Supports the default collections:
 * <ul>
 * <li>assets</li>
 * <li>types</li>
 * </ul>
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public final class WebCmsCollectionsImporter implements WebCmsDataImporter
{
	private WebCmsDataImportService dataImportService;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return ( "assets".equals( data.getKey() ) || "types".equals( data.getKey() ) ) && !data.hasParent();
	}

	@Override
	public void importData( WebCmsDataEntry data ) {
		data.getMapData().forEach( ( key, properties ) -> dataImportService.importData( new WebCmsDataEntry( key, data, properties ) ) );
	}

	@Autowired
	void setDataImportService( WebCmsDataImportService dataImportService ) {
		this.dataImportService = dataImportService;
	}
}
