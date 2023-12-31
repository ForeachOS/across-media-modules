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

package modules.test.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.core.installers.InstallerRunCondition;
import com.foreach.across.modules.webcms.installers.AbstractWebCmsDataInstaller;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Order(2)
@Installer(description = "Installs test data", runCondition = InstallerRunCondition.AlwaysRun, phase = InstallerPhase.AfterModuleBootstrap)
public class TestDataInstaller extends AbstractWebCmsDataInstaller
{
	@Override
	protected void registerResources( List<String> locations ) {
		locations.add( "classpath:installers/test-components.yml" );
		locations.add( "classpath:installers/test-pages.yml" );
		locations.add( "classpath:installers/test-mappings.yml" );
		locations.add( "classpath:installers/test-articles.yml" );
	}
}
