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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.webcms.data.AbstractWebCmsDataImporter;
import com.foreach.across.modules.webcms.data.WebCmsDataAction;
import com.foreach.across.modules.webcms.data.WebCmsDataEntry;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.menu.validators.WebCmsMenuValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates one (or many) @{@link WebCmsMenu}s from a yml file
 *
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebCmsMenuImporter extends AbstractWebCmsDataImporter<WebCmsMenu, WebCmsMenu>
{
	private final WebCmsMenuRepository webCmsMenuRepository;
	private final WebCmsMenuValidator menuValidator;

	@Override
	public boolean supports( WebCmsDataEntry data ) {
		return "menus".equals( data.getParentKey() );
	}

	@Override
	protected WebCmsMenu retrieveExistingInstance( WebCmsDataEntry data ) {
		String dataKey = data.getMapData().containsKey( "name" ) ? (String) data.getMapData().get( "name" ) : data.getKey();
		String objectId = (String) data.getMapData().get( "objectId" );
		if ( objectId != null ) {
			return webCmsMenuRepository.findOneByObjectId( objectId ).orElse( null );
		}
		WebCmsDomain domain = retrieveDomainForDataEntry( data, WebCmsMenu.class );
		return webCmsMenuRepository.findOneByNameAndDomain( dataKey, domain ).orElse( null );
	}

	@Override
	protected WebCmsMenu createDto( WebCmsDataEntry data, WebCmsMenu existing, WebCmsDataAction action, Map<String, Object> dataValues ) {
		if ( existing == null ) {
			return createNewMenuDto( data );
		}
		else if ( action == WebCmsDataAction.REPLACE ) {
			WebCmsMenu dto = createNewMenuDto( data );
			dto.setId( existing.getId() );
			return dto;
		}

		return existing.toDto();
	}

	private WebCmsMenu createNewMenuDto( WebCmsDataEntry data ) {
		String dataKey = data.getMapData().containsKey( "name" ) ? (String) data.getMapData().get( "name" ) : data.getKey();
		return WebCmsMenu.builder().name( dataKey ).build();
	}

	@Override
	protected boolean applyDataValues( Map<String, Object> values, WebCmsMenu dto ) {
		Map<String, Object> filtered = new HashMap<>( values );
		filtered.remove( WebCmsMenuItemImporter.PROPERTY_NAME );

		return super.applyDataValues( filtered, dto );
	}

	@Override
	protected void deleteInstance( WebCmsMenu instance, WebCmsDataEntry data ) {
		webCmsMenuRepository.delete( instance );
	}

	@Override
	protected void saveDto( WebCmsMenu dto, WebCmsDataAction action, WebCmsDataEntry data ) {
		webCmsMenuRepository.save( dto );
	}

	@Override
	protected void validate( WebCmsMenu itemToBeSaved, Errors errors ) {
		menuValidator.validate( itemToBeSaved, errors );
	}
}
