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

package com.foreach.across.modules.webcms.domain.menu;

import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import com.foreach.across.modules.hibernate.id.AcrossSequenceGenerator;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.concurrent.NotThreadSafe;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Corresponds to a {@link com.foreach.across.modules.web.menu.Menu} item, used with a {@link com.foreach.across.modules.web.menu.PathBasedMenuBuilder}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@NotThreadSafe
@Entity
@Table(name = "wcm_menu_item")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class WebCmsMenuItem extends SettableIdBasedEntity<WebCmsMenu>
{
	@Id
	@GeneratedValue(generator = "seq_wcm_menu_item_id")
	@GenericGenerator(
			name = "seq_wcm_menu_item_id",
			strategy = AcrossSequenceGenerator.STRATEGY,
			parameters = {
					@org.hibernate.annotations.Parameter(name = "sequenceName", value = "seq_wcm_menu_item_id"),
					@org.hibernate.annotations.Parameter(name = "allocationSize", value = "1")
			}
	)
	private Long id;

	/**
	 * Menu this item belongs to.
	 */
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "menu_id")
	private WebCmsMenu menu;

	/**
	 * Path of the menu item in the entire menu tree.
	 * The path determines the parent item.
	 */
	@Column(name = "menu_path")
	@NotBlank
	@Length(max = 255)
	private String path;

	/**
	 * Title of the menu item.
	 */
	@Column
	@NotBlank
	@Length(max = 255)
	private String title;

	/**
	 * URL the menu item should point to.
	 */
	@Column
	@Length(max = 500)
	private String url;

	/**
	 * Optional page this item links to.  If no explicit url is set on the item, the page url will be used instead.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "page_id")
	private WebCmsPage linkedPage;

	/**
	 * Order index of the menu item (under its parent).
	 */
	@Column(name = "sort_index")
	private int sortIndex;

	/**
	 * Should the item render as a group or not.  A group will have its url ignored and not render as a separate selectable item.
	 */
	@Column(name = "is_group")
	private boolean group;
}
