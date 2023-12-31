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

package com.foreach.across.modules.webcms.domain.page.config;

import com.foreach.across.modules.entity.views.events.BuildEntityDeleteViewEvent;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItem;
import com.foreach.across.modules.webcms.domain.menu.WebCmsMenuItemRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author Raf Ceuls
 * @since 0.0.2
 */
@Component
@RequiredArgsConstructor
public class WebCmsPageDeleteEventHandler
{
	private final WebCmsAssetEndpointRepository endpointRepository;
	private final WebCmsMenuItemRepository menuItemRepository;

	@EventListener
	public void handleDeleteEvent( BuildEntityDeleteViewEvent<WebCmsPage> event ) {
		if ( event.getEntity() != null ) {
			List<WebCmsAssetEndpoint> endpoints = endpointRepository.findAllByAsset( event.getEntity() );
			endpoints.forEach(
					endpointOnPage -> {
						Collection<WebCmsMenuItem> menuItem = menuItemRepository.findAllByEndpoint( endpointOnPage );
						if ( !menuItem.isEmpty() ) {
							event.setDeleteDisabled( true );
						}
					}
			);
		}
	}
}