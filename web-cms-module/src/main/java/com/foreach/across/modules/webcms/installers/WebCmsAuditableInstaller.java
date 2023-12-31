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
import com.foreach.across.modules.hibernate.installers.AuditableSchemaInstaller;
import org.springframework.core.annotation.Order;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
@Order(2)
@Installer(description = "Adds auditing columns to core tables", version = 7)
public class WebCmsAuditableInstaller extends AuditableSchemaInstaller
{
	@Override
	protected Collection<String> getTableNames() {
		return Arrays.asList( "wcm_asset", "wcm_type", "wcm_component", "wcm_object_type_link", "wcm_object_asset_link", "wcm_domain", "wcm_menu" );
	}
}
