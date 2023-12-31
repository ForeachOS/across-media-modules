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

package com.foreach.across.modules.webcms.domain.type;

import com.foreach.across.modules.webcms.data.WebCmsDataConversionService;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;

/**
 * @author Steven Gentens
 * @since 0.0.3
 */
@Component
@RequiredArgsConstructor
public class MapToWebCmsTypeSpecifierConverter implements ConverterFactory<Map<String, Object>, WebCmsTypeSpecifier>
{
	private final WebCmsDataConversionService conversionService;
	private final WebCmsTypeSpecifierService typeSpecifierService;
	private final WebCmsTypeSpecifierRepository typeSpecifierRepository;

	@PostConstruct
	void registerToConversionService() {
		conversionService.addConverterFactory( this );
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends WebCmsTypeSpecifier> Converter<Map<String, Object>, T> getConverter( Class<T> targetType ) {
		return ( data ) -> {
			if ( data.containsKey( "objectId" ) ) {
				return targetType.cast( typeSpecifierRepository.findOneByObjectId( Objects.toString( data.get( "objectId" ) ) ) );
			}

			if ( data.containsKey( "typeKey" ) ) {
				String typeKey = Objects.toString( data.get( "typeKey" ) );

				if ( data.containsKey( "domain" ) ) {
					return typeSpecifierService.getTypeSpecifierByKey(
							typeKey, targetType, conversionService.convert( data.get( "domain" ), WebCmsDomain.class )
					);
				}

				return typeSpecifierService.getTypeSpecifierByKey( typeKey, targetType );
			}

			return null;
		};
	}
}
