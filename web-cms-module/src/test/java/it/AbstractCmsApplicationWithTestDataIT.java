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

package it;

import com.foreach.across.modules.webcms.domain.image.connector.WebCmsImageConnector;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import modules.test.CmsTestModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.mock;

/**
 * Base class for all integration tests using the same application context with a same backing db.
 *
 * @author Arne Vandamme
 * @since 0.0.1
 */
@ExtendWith(SpringExtension.class)
@AcrossWebAppConfiguration(classes = AbstractCmsApplicationWithTestDataIT.Config.class)
public abstract class AbstractCmsApplicationWithTestDataIT extends AbstractMockMvcTest
{
	@AcrossTestConfiguration
	protected static class Config extends DynamicDataSourceConfigurer
	{
		@Bean
		CmsTestModule cmsTestModule() {
			return new CmsTestModule();
		}

		@Bean
		WebCmsImageConnector webCmsImageConnector() {
			return mock( WebCmsImageConnector.class );
		}
	}
}
