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

package com.foreach.across.modules.webcms.domain.url.repositories;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Exposed
public interface WebCmsUrlRepository extends IdBasedEntityJpaRepository<WebCmsUrl>, QueryDslPredicateExecutor<WebCmsUrl>
{
	List<WebCmsUrl> findByPath( String path );

	List<WebCmsUrl> findAllByEndpoint( WebCmsEndpoint entity );

	Page<WebCmsUrl> findAllByEndpoint( WebCmsEndpoint entity, Pageable pageable );
}