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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Arne Vandamme
 * @since 0.0.3
 */
public abstract class AbstractMockMvcTest
{
	@Autowired
	protected MockMvc mockMvc;

	protected Html html( String path ) {
		return html( get( path ) );
	}

	private Html html( RequestBuilder requestBuilder ) {
		try {
			return new Html( Jsoup.parse( mockMvc.perform( requestBuilder )
			                                     .andExpect( status().isOk() )
			                                     .andReturn()
			                                     .getResponse()
			                                     .getContentAsString() ) );
		}
		catch ( Exception e ) {
			throw new RuntimeException( e );
		}
	}

	protected void assertEqualsIgnoreWhitespace( String expected, String actual ) {
		assertEquals( unifyWhitespace( expected ), unifyWhitespace( actual ) );
	}

	@SneakyThrows
	protected void getAndExpect( String path, String content ) {
		assertEquals(
				content,
				mockMvc.perform( get( path ) )
				       .andExpect( status().isOk() )
				       .andReturn()
				       .getResponse()
				       .getContentAsString()
		);
	}

	private String unifyWhitespace( String value ) {
		return value.replaceAll( "\\s+<", "<" ).replaceAll( ">\\s+", ">" ).replaceAll( "\\s{2,}", " " );
	}

	/**
	 * Helper that wraps a Jsoup document.
	 */
	@RequiredArgsConstructor
	protected final class Html
	{
		@Getter
		private final Document document;

		public void assertElementHasText( String text, String selector ) {
			assertEquals( text, document.select( selector ).text() );
		}

		public void assertElementHasHTML( String html, String selector ) {
			assertEqualsIgnoreWhitespace( html, document.select( selector ).html() );
		}

		public void assertElementIsEmpty( String selector ) {
			val element = document.select( selector ).get( 0 );
			assertEquals( "", element.html().trim() );
		}

		public void assertNoElement( String selector ) {
			assertTrue( document.select( selector ).isEmpty() );
		}
	}
}
