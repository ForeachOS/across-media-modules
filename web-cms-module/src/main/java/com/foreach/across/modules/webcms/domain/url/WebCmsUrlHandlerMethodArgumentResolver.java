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

package com.foreach.across.modules.webcms.domain.url;

import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * This {@link HandlerMethodArgumentResolver} enables the usage of {@link WebCmsUrl} as argument
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@RequiredArgsConstructor
public class WebCmsUrlHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver
{
	private final WebCmsEndpointContext context;

	@Override
	public boolean supportsParameter( MethodParameter parameter ) {
		return WebCmsUrl.class.isAssignableFrom( parameter.getParameterType() );
	}

	@Override
	public Object resolveArgument( MethodParameter parameter,
	                               ModelAndViewContainer mavContainer,
	                               NativeWebRequest webRequest,
	                               WebDataBinderFactory binderFactory ) throws Exception {
		return context.isAvailable() ? context.getUrl() : null;
	}
}
