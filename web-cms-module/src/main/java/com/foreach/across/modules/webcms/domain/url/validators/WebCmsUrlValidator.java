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

package com.foreach.across.modules.webcms.domain.url.validators;

import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.domain.url.repositories.WebCmsUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * This class validates that only one {@code WebCmsUrl} from a {@code WebCmsEndpoint} is the primary URL,
 * and that the primary URL is not in itself a redirect.
 *
 * @author Sander Van Loock
 * @since 0.0.1
 */
@Component
@RequiredArgsConstructor
public class WebCmsUrlValidator extends EntityValidatorSupport<WebCmsUrl>
{
	private final WebCmsUrlRepository urlRepository;

	@Override
	public boolean supports( Class<?> clazz ) {
		return WebCmsUrl.class.isAssignableFrom( clazz );
	}

	@Override
	protected void postValidation( WebCmsUrl entity, Errors errors, Object... validationHints ) {
		if ( !errors.hasFieldErrors( "path" ) ) {
			urlRepository.findOneByPathAndEndpoint_Domain( entity.getPath(), entity.getEndpoint().getDomain() )
			             .filter( existing -> !entity.equals( existing ) )
			             .ifPresent( e -> errors.rejectValue( "path", "alreadyExists" ) );
		}

		if ( !errors.hasErrors() && entity.isPrimary() ) {
			if ( HttpStatus.Series.valueOf( entity.getHttpStatus() ) == HttpStatus.Series.REDIRECTION ) {
				errors.rejectValue( "httpStatus", "primaryUrlCannotBeRedirect", new Object[0], "Primary URL may not have a redirection HttpStatus." );
			}

			urlRepository
					.findAllByEndpoint( entity.getEndpoint() )
					.stream()
					.filter( url -> !url.equals( entity ) && url.isPrimary() )
					.findFirst()
					.ifPresent(
							existingPrimary -> errors.rejectValue(
									"primary",
									"onlyOnePrimaryUrlPerEndpoint",
									new Object[] { existingPrimary.getPath() },
									"Another primary URL exists."
							)
					);
		}
	}
}
