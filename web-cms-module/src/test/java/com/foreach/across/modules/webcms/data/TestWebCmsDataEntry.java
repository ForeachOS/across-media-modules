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

package com.foreach.across.modules.webcms.data;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Arne Vandamme
 * @since 0.0.1
 */
public class TestWebCmsDataEntry
{
	@Test(expected = IllegalArgumentException.class)
	public void dataMustNotBeNull() {
		new WebCmsDataEntry( "publicationKey", null );
	}

	@Test(expected = IllegalArgumentException.class)
	public void dataMustBeMapOrCollection() {
		new WebCmsDataEntry( "publicationKey", "some data" );
	}

	@Test
	public void dataWithoutParentKey() {
		Map<String, String> data = new HashMap<>();
		data.put( "1", "2" );

		WebCmsDataEntry entry = new WebCmsDataEntry( "mykey", data );
		assertEquals( "mykey", entry.getKey() );
		assertNull( entry.getParentKey() );
		assertEquals( data, entry.getMapData() );
	}

	@Test
	public void dataWithParentKey() {
		Map<String, String> data = new HashMap<>();
		data.put( "1", "2" );

		WebCmsDataEntry entry = new WebCmsDataEntry( "mykey", "parentKey", data );
		assertEquals( "mykey", entry.getKey() );
		assertEquals( "parentKey", entry.getParentKey() );
		assertEquals( data, entry.getMapData() );
	}
}
