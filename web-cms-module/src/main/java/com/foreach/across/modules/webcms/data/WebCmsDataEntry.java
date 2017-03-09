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

import lombok.Data;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a set of data to be imported or exported.
 *
 * @author Arne Vandamme
 * @see WebCmsDataImportService
 * @see WebCmsDataExportService
 * @since 0.0.1
 */
@Data
public final class WebCmsDataEntry
{
	/**
	 * Key of this data entry item.
	 */
	private final String key;

	/**
	 * Optional parent key of the data entry.
	 */
	private final String parentKey;

	/**
	 * Data itself - should not be null.
	 */
	private final Map<String, Object> data;

	public WebCmsDataEntry( String key, Object data ) {
		this( key, null, data );
	}

	@SuppressWarnings("unchecked")
	public WebCmsDataEntry( String key, String parentKey, Object data ) {
		Assert.notNull( data );
		Assert.isTrue( data instanceof Map, "Only Map type data is supported" );

		this.key = key;
		this.parentKey = parentKey;
		this.data = Collections.unmodifiableMap( (Map<String, Object>) data );
	}
}