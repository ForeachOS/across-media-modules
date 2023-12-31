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

package com.foreach.across.modules.webcms.domain.page.validators;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.page.services.WebCmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Validator for {@link WebCmsPage}.  Applies {@link WebCmsPageService#prepareForSaving(WebCmsPage)}
 * before performing validation, this prepares the instance based on the settings.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Component
public class WebCmsPageValidator extends EntityValidatorSupport<WebCmsPage>
{
	private WebCmsPageService pageService;
	private WebCmsPageRepository pageRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsPage.class.isAssignableFrom( clazz );
	}

	@Override
	protected void preValidation( WebCmsPage page, Errors errors, Object... validationHints ) {
		pageService.prepareForSaving( page );
	}

	@Override
	protected void postValidation( WebCmsPage page, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "canonicalPath" ) ) {
			pageRepository.findOneByCanonicalPathAndDomain( page.getCanonicalPath(), page.getDomain() )
			              .filter( existing -> !page.equals( existing ) )
			              .ifPresent( e -> errors.rejectValue( "canonicalPath", "alreadyExists" ) );
		}
	}

	@Autowired
	void setPageService( WebCmsPageService pageService ) {
		this.pageService = pageService;
	}

	@Autowired
	void setPageRepository( WebCmsPageRepository pageRepository ) {
		this.pageRepository = pageRepository;
	}
}
