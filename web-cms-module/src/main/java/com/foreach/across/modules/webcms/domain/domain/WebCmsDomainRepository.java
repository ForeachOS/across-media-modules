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

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.hibernate.jpa.repositories.IdBasedEntityJpaRepository;
import com.foreach.across.modules.webcms.domain.WebCmsObjectRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
@Exposed
public interface WebCmsDomainRepository extends WebCmsObjectRepository<WebCmsDomain>, IdBasedEntityJpaRepository<WebCmsDomain>, QueryDslPredicateExecutor<WebCmsDomain>
{
	WebCmsDomain findOneByDomainKey( String domainKey );

	@Override
	List<WebCmsDomain> findAll( Predicate predicate );

	@Override
	List<WebCmsDomain> findAll( Predicate predicate, Sort sort );

	@Override
	List<WebCmsDomain> findAll( Predicate predicate, OrderSpecifier<?>[] orders );

	@Override
	List<WebCmsDomain> findAll( OrderSpecifier<?>[] orders );
}