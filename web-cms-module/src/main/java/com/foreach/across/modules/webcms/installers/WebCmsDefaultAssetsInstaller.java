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

package com.foreach.across.modules.webcms.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.modules.webcms.domain.domain.config.WebCmsMultiDomainConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "web-cms-module.default-data.assets", name = "enabled", havingValue = "true", matchIfMissing = true)
@Installer(description = "Install default assets for a simple website", phase = InstallerPhase.AfterModuleBootstrap, version = 18)
public class WebCmsDefaultAssetsInstaller extends AbstractWebCmsDataInstaller
{
	private final WebCmsMultiDomainConfiguration multiDomainConfiguration;

	@Override
	protected void registerResources( List<String> locations ) {
		locations.add( "classpath:installers/WebCmsModule/default-types.yml" );

		if ( multiDomainConfiguration.isDisabled() ) {
			locations.add( "classpath:installers/WebCmsModule/default-assets.yml" );
		}
	}
}
