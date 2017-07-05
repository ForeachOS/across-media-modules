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

package com.foreach.across.modules.webcms.domain.endpoint;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAsset;
import com.foreach.across.modules.webcms.domain.endpoint.support.EndpointModificationType;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import com.foreach.across.modules.webcms.infrastructure.ModificationReport;

import java.util.Optional;

/**
 * Central API for endpoint and URL related functions.
 *
 * @author Sander Van Loock, Arne Vandamme
 * @since 0.0.1
 */
public interface WebCmsEndpointService
{
	/**
	 * Retrieve the {@link WebCmsUrl} corresponding to a particular path.
	 *
	 * @param path to find the url for
	 * @return url if found
	 */
	Optional<WebCmsUrl> getUrlForPath( String path );

	/**
	 * Update the primary URL for a {@link WebCmsAsset}.  Depending on the published status of the asset
	 * will either create or update the existing primary url, or will update the previous primary url
	 * as a redirect to the new primary url.
	 * <p/>
	 * Note that if there is no single {@link com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint}
	 * for the asset, this method will do nothing.  The {@link ModificationReport} returned should contain the
	 * status information on which action was performed.  A {@link com.foreach.across.modules.webcms.infrastructure.ModificationStatus#FAILED}
	 * means another endpoint already has that url.
	 * <p/>
	 * The third parameter indicates if a {@link com.foreach.across.modules.webcms.domain.endpoint.support.PrimaryUrlForAssetFailedEvent} should
	 * be published in case of a conflict with another endpoint.  This allows another bean to take action and change the modification report.
	 *
	 * @param primaryUrl            for the asset
	 * @param asset                 to update
	 * @param publishEventOnFailure true if an even
	 * @return modification report, optionally containing the old and new primary url
	 */
	ModificationReport<EndpointModificationType, WebCmsUrl> updateOrCreatePrimaryUrlForAsset( String primaryUrl,
	                                                                                          WebCmsAsset asset,
	                                                                                          boolean publishEventOnFailure );

	/**
	 * Get the primary URL for a {@link WebCmsAsset}.
	 *
	 * @param asset to get the primary url for
	 * @return url or none if no endpoint with a primary url
	 */
	Optional<WebCmsUrl> getPrimaryUrlForAsset( WebCmsAsset asset );

	/**
	 * Builds a preview url for a particular {@link WebCmsAsset}.
	 * The preview url is the primary url appended with a request parameter <strong>wcmPreview</strong>.
	 *
	 * @param asset to create the url for
	 * @return url if a primary url was available
	 */
	Optional<String> buildPreviewUrl( WebCmsAsset asset );

	/**
	 * Check if a particular security code allows access to an endpoint.
	 *
	 * @param endpoint     to access
	 * @param securityCode that was used
	 * @return true if allowed
	 */
	boolean isPreviewAllowed( WebCmsEndpoint endpoint, String securityCode );
}
