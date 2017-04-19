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

package com.foreach.across.modules.webcms.config;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetHandlerMethodArgumentResolver;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpointHandlerMethodArgumentResolver;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrlHandlerMethodArgumentResolver;
import com.foreach.across.modules.webcms.web.endpoint.context.WebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Configuration
@RequiredArgsConstructor
public class WebCmsWebModuleConfiguration extends WebMvcConfigurerAdapter
{
	private final WebCmsEndpointContext context;

	@Override
	public void addArgumentResolvers( List<HandlerMethodArgumentResolver> argumentResolvers ) {
		argumentResolvers.add( new WebCmsEndpointHandlerMethodArgumentResolver( context ) );
		argumentResolvers.add( new WebCmsUrlHandlerMethodArgumentResolver( context ) );
		argumentResolvers.add( new WebCmsAssetHandlerMethodArgumentResolver( context ) );
	}
}