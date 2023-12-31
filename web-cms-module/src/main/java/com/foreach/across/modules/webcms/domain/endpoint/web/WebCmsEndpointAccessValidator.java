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

package com.foreach.across.modules.webcms.domain.endpoint.web;

import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;

/**
 * Validates that a resolved endpoint should be available.  If any validator returns
 * {@code false}, this should result in an endpoint not found.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsEndpointAccessValidator<T extends WebCmsEndpoint>
{
	/**
	 * @return true if this validator should be used for that endpoint
	 */
	boolean appliesFor( WebCmsEndpoint endpoint );

	/**
	 * @return true if access is allowed
	 */
	boolean validateAccess( T endpoint );
}
