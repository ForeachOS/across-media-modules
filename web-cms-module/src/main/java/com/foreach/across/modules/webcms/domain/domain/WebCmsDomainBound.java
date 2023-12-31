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

package com.foreach.across.modules.webcms.domain.domain;

/**
 * API for any entity that can be linked to a {@link WebCmsDomain}.
 *
 * @author Arne Vandamme
 * @since 0.0.3
 */
public interface WebCmsDomainBound
{
	/**
	 * @return the domain this entity belongs to
	 */
	WebCmsDomain getDomain();

	/**
	 * Set the {@link WebCmsDomain} this entity belongs to.
	 *
	 * @param domain to set
	 */
	void setDomain( WebCmsDomain domain );
}
