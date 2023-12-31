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

package com.foreach.across.modules.webcms.domain.endpoint.web.interceptor;

import com.foreach.across.modules.webcms.domain.endpoint.web.context.WebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
public class TestWebCmsEndpointHandlerInterceptor
{
	@Mock
	private WebCmsEndpointContext context;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Object handler;

	@InjectMocks
	private WebCmsEndpointHandlerInterceptor interceptor;

	@Test
	public void preHandle() throws Exception {
		HttpStatus expectedStatus = HttpStatus.ALREADY_REPORTED;
		int expectedStatusCode = expectedStatus.value();
		when( context.isAvailable() ).thenReturn( true );
		when( context.getUrl() ).thenReturn( WebCmsUrl.builder().httpStatus( expectedStatus ).build() );
		interceptor = new WebCmsEndpointHandlerInterceptor( context );

		interceptor.preHandle( request, response, handler );

		verify( response, times( 1 ) ).setStatus( expectedStatusCode );
	}
}
