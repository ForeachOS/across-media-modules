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

package webapps.admin.application.config;

import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.webcms.domain.article.WebCmsArticle;
import com.foreach.across.modules.webcms.domain.component.WebCmsComponentType;
import com.foreach.across.modules.webcms.domain.component.config.WebCmsObjectComponentViewsConfiguration;
import com.foreach.across.modules.webcms.domain.publication.WebCmsPublicationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import webapps.admin.application.ui.SectionComponentMetadata;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Configuration
class AdminUiConfiguration implements EntityConfigurer
{
	@Autowired
	void enableComponents( WebCmsObjectComponentViewsConfiguration componentViewsConfiguration ) {
		componentViewsConfiguration.enable( WebCmsPublicationType.class );
		componentViewsConfiguration.enable( WebCmsComponentType.class );
		componentViewsConfiguration.enable( WebCmsArticle.class );
	}

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( WebCmsComponentType.class ).show();

		entities.create()
		        .entityType( SectionComponentMetadata.class, true )
		        .properties( props -> props.property( "shortTitle" ).order( -50 ) );
	}
}