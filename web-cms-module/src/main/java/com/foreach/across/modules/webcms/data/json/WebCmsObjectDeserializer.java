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

package com.foreach.across.modules.webcms.data.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

/**
 * Deserializes a {@link com.foreach.across.modules.webcms.domain.WebCmsObject} by using the {@link WebCmsDataConversionService} instead.
 * This way it should be enough for most custom implementations to just provide a converter from object id ({@code String} to their implementation, and vice versa).
 *
 * @author Arne Vandamme
 * @since 0.0.2
 */
@RequiredArgsConstructor
public final class WebCmsObjectDeserializer extends JsonDeserializer<Object>
{
	private final Class<?> targetType;
	private final WebCmsDataConversionService cmsDataConversionService;

	@Override
	public Class<?> handledType() {
		return targetType;
	}

	@Override
	public Object deserialize( JsonParser jsonParser, DeserializationContext context ) throws IOException {
		return cmsDataConversionService.convert( jsonParser.getValueAsString(), targetType );
	}
}
