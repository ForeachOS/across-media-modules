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

package com.foreach.across.modules.webcms.domain.endpoint.web.controllers;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.endpoint.WebCmsEndpoint;
import com.foreach.across.modules.webcms.domain.endpoint.web.WebCmsEndpointContextResolver;
import com.foreach.across.modules.webcms.domain.endpoint.web.context.ConfigurableWebCmsEndpointContext;
import com.foreach.across.modules.webcms.domain.redirect.WebCmsRemoteEndpoint;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

import static junit.framework.TestCase.assertNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Sander Van Loock
 * @since 0.0.1
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestWebCmsEndpointCondition
{
	@Mock
	private ConfigurableWebCmsEndpointContext context;

	@Mock
	private WebCmsEndpointContextResolver resolver;

	private WebCmsAssetEndpoint endpoint;
	private WebCmsUrl url;

	@BeforeEach
	public void setUp() throws Exception {
		endpoint = WebCmsAssetEndpoint.builder().build();
		endpoint.setDomain( WebCmsDomain.builder().domainKey( "some-domain" ).build() );

		url = WebCmsUrl.builder().httpStatus( HttpStatus.I_AM_A_TEAPOT ).build();

		when( context.isResolved() ).thenReturn( true );
		when( context.getEndpoint() ).thenReturn( endpoint );
		when( context.getUrl() ).thenReturn( url );

	}

	@Test
	public void emptyConditionContent() throws Exception {
		WebCmsEndpointCondition condition = new WebCmsEndpointCondition( context, resolver );

		Collection<?> actualContent = condition.getContent();

		assertEquals( 3, actualContent.size() );

	}

	@Test
	public void stringInfix() throws Exception {
		WebCmsEndpointCondition condition = new WebCmsEndpointCondition( context, resolver );
		assertEquals( " && ", condition.getToStringInfix() );
	}

	@Test
	public void statusHasPreferenceOverSeries() throws Exception {
		WebCmsEndpointCondition conditionWithSeries = condition( HttpStatus.Series.SUCCESSFUL );
		WebCmsEndpointCondition conditionWithStatus = condition( HttpStatus.OK );

		int actual = conditionWithSeries.compareTo( conditionWithStatus, new MockHttpServletRequest() );
		int actualReversed = conditionWithStatus.compareTo( conditionWithSeries, new MockHttpServletRequest() );

		assertEquals( -1, actual );
		assertEquals( 1, actualReversed );
	}

	@Test
	public void moreStatusesHasPreference() throws Exception {
		WebCmsEndpointCondition conditionWithStatus = condition( HttpStatus.OK );
		WebCmsEndpointCondition conditionWithTwoStatus = condition( HttpStatus.OK, HttpStatus.NO_CONTENT );

		int actual = conditionWithStatus.compareTo( conditionWithTwoStatus, new MockHttpServletRequest() );

		assertEquals( -1, actual );
	}

	@Test
	public void moreSeriesHasPreference() throws Exception {
		WebCmsEndpointCondition conditionWithSeries = condition( HttpStatus.Series.SUCCESSFUL );
		WebCmsEndpointCondition conditionWithTwoSeries = condition( HttpStatus.Series.SUCCESSFUL, HttpStatus.Series.INFORMATIONAL );

		int actual = conditionWithSeries.compareTo( conditionWithTwoSeries, new MockHttpServletRequest() );

		assertEquals( -1, actual );
	}

	@Test
	public void subclassHasPreferenceOverParent() throws Exception {
		WebCmsEndpointCondition conditionWithParent = condition( WebCmsAssetEndpoint.class );
		WebCmsEndpointCondition conditionWithSubClass = condition( DummyWebCmsPageEndpoint.class );

		int actual = conditionWithParent.compareTo( conditionWithSubClass, new MockHttpServletRequest() );

		assertEquals( -1, actual );
	}

	@Test
	public void equalConditions() throws Exception {
		WebCmsEndpointCondition conditionOne = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.ACCEPTED },
		                                                  new HttpStatus.Series[] { HttpStatus.Series.CLIENT_ERROR } );
		WebCmsEndpointCondition conditionTwo = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.NO_CONTENT },
		                                                  new HttpStatus.Series[] { HttpStatus.Series.SERVER_ERROR } );

		int actual = conditionOne.compareTo( conditionTwo, new MockHttpServletRequest() );

		assertEquals( 0, actual );
	}

	@Test
	public void resolveContextWhenNotYetResolved() throws Exception {
		when( context.isResolved() ).thenReturn( false );

		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.I_AM_A_TEAPOT }, new HttpStatus.Series[0] );
		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		verify( resolver, times( 1 ) ).resolve( context, request );
	}

	@Test
	public void matchConditionWithEndpointAndStatus() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.I_AM_A_TEAPOT, HttpStatus.ALREADY_REPORTED },
		                                               new HttpStatus.Series[0] );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNotNull( actualCondition );
		verifyCondition( actualCondition, WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.I_AM_A_TEAPOT }, new HttpStatus.Series[0] );
	}

	@Test
	public void matchConditionWithEndpointAndSeries() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class, new HttpStatus[0],
		                                               new HttpStatus.Series[] { HttpStatus.Series.CLIENT_ERROR, HttpStatus.Series.REDIRECTION } );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNotNull( actualCondition );
		verifyCondition( actualCondition, WebCmsAssetEndpoint.class, new HttpStatus[0], new HttpStatus.Series[] { HttpStatus.Series.CLIENT_ERROR }
		);
	}

	@Test
	public void dontMatchIfWrongEndpoint() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsRemoteEndpoint.class, new HttpStatus[] { HttpStatus.I_AM_A_TEAPOT }, new HttpStatus.Series[0] );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNull( actualCondition );
	}

	@Test
	public void dontMatchIfWrongSeries() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class, new HttpStatus[0], new HttpStatus.Series[] { HttpStatus.Series.SUCCESSFUL } );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNull( actualCondition );
	}

	@Test
	public void dontMatchIfWrongStatus() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.NO_CONTENT }, new HttpStatus.Series[0] );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNull( actualCondition );
	}

	@Test
	public void matchOnlyWithEndpoint() throws Exception {
		WebCmsEndpointCondition condition = condition( WebCmsAssetEndpoint.class );

		HttpServletRequest request = new MockHttpServletRequest();
		WebCmsEndpointCondition actualCondition = condition.getMatchingCondition( request );

		assertNotNull( actualCondition );
		verifyCondition( actualCondition, WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.I_AM_A_TEAPOT }, new HttpStatus.Series[0] );
	}

	@Test
	public void combineTwoConditions() throws Exception {
		WebCmsEndpointCondition conditionOne = condition( WebCmsAssetEndpoint.class,
		                                                  new HttpStatus[] { HttpStatus.INTERNAL_SERVER_ERROR, HttpStatus.NOT_IMPLEMENTED },
		                                                  new HttpStatus.Series[] { HttpStatus.Series.SUCCESSFUL, HttpStatus.Series.REDIRECTION,
		                                                                            HttpStatus.Series.CLIENT_ERROR } );

		WebCmsEndpointCondition conditionTwo = condition( WebCmsAssetEndpoint.class,
		                                                  new HttpStatus[] { HttpStatus.NOT_IMPLEMENTED, HttpStatus.MOVED_PERMANENTLY },
		                                                  new HttpStatus.Series[] { HttpStatus.Series.CLIENT_ERROR } );

		WebCmsEndpointCondition actual = conditionOne.combine( conditionTwo );
		assertNotNull( actual );
		verifyCondition( actual, WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.MOVED_PERMANENTLY, HttpStatus.NOT_IMPLEMENTED },
		                 new HttpStatus.Series[] { HttpStatus.Series.CLIENT_ERROR } );
	}

	@Test
	public void combineEmtpyAndFullCondition() throws Exception {
		WebCmsEndpointCondition conditionOne = new WebCmsEndpointCondition( context, resolver );

		WebCmsEndpointCondition conditionTwo = condition( WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.NO_CONTENT },
		                                                  new HttpStatus.Series[] { HttpStatus.Series.SERVER_ERROR } );

		WebCmsEndpointCondition actual = conditionOne.combine( conditionTwo );
		assertNotNull( actual );
		verifyCondition( actual, WebCmsAssetEndpoint.class, new HttpStatus[] { HttpStatus.NO_CONTENT },
		                 new HttpStatus.Series[] { HttpStatus.Series.SERVER_ERROR } );
	}

	@Test
	public void combineSubclassEndpointTypes() throws Exception {
		WebCmsEndpointCondition conditionOne = condition( WebCmsEndpoint.class );

		WebCmsEndpointCondition conditionTwo = condition( DummyWebCmsPageEndpoint.class );

		WebCmsEndpointCondition actual = conditionOne.combine( conditionTwo );
		assertNotNull( actual );
		verifyCondition( actual, DummyWebCmsPageEndpoint.class, new HttpStatus[0], new HttpStatus.Series[0] );
	}

	@Test
	public void invalidCombineDueToDifferentTypes() throws Exception {
		WebCmsEndpointCondition conditionOne = condition( WebCmsAssetEndpoint.class );
		WebCmsEndpointCondition conditionTwo = condition( WebCmsRemoteEndpoint.class );

		boolean exceptionThrown = false;
		try {
			conditionOne.combine( conditionTwo );
		}
		catch ( InvalidWebCmsConditionCombination e ) {
			String message = String.format( "A condition with endpoint type %s and type %s cannot be merged", WebCmsAssetEndpoint.class,
			                                WebCmsRemoteEndpoint.class );
			assertEquals( message, e.getLocalizedMessage() );
			exceptionThrown = true;
		}
		assertTrue( exceptionThrown );
	}

	private WebCmsEndpointCondition condition( HttpStatus.Series... series ) {
		return condition( WebCmsEndpoint.class, new HttpStatus[0], series );
	}

	private WebCmsEndpointCondition condition( HttpStatus... statuses ) {
		return condition( WebCmsEndpoint.class, statuses, new HttpStatus.Series[0] );
	}

	private WebCmsEndpointCondition condition( Class<? extends WebCmsEndpoint> endpointType,
	                                           HttpStatus[] statuses,
	                                           HttpStatus.Series[] series ) {
		WebCmsEndpointCondition condition = new WebCmsEndpointCondition( context, resolver );

		Map<String, Object> values = new HashMap<>();
		values.put( "value", endpointType );
		values.put( "status", statuses );
		values.put( "series", series );

		AnnotatedElement annotatedElement = mock( AnnotatedElement.class );
		when( annotatedElement.getDeclaredAnnotations() )
				.thenReturn( new Annotation[] { AnnotationUtils.synthesizeAnnotation( values, WebCmsEndpointMapping.class, null ) } );
		condition.setAnnotatedElement( annotatedElement );

		return condition;
	}

	private WebCmsEndpointCondition condition( Class<? extends WebCmsEndpoint> webCmsPageEndpointClass ) {
		return condition( webCmsPageEndpointClass, new HttpStatus[0], new HttpStatus.Series[0] );
	}

	private void verifyCondition( WebCmsEndpointCondition condition,
	                              Class<? extends WebCmsEndpoint> expectedEndpointClass,
	                              HttpStatus[] expectedStatuses,
	                              HttpStatus.Series[] expectedSeries ) {
		List<?> content = (List<?>) condition.getContent();
		assertEquals( expectedEndpointClass, content.get( 0 ) );
		List<HttpStatus> statusesAsList = Arrays.asList( (HttpStatus[]) content.get( 1 ) );
		assertEquals( expectedStatuses.length, statusesAsList.size(), "Actual statuses do not match expected" );
		for ( HttpStatus status : expectedStatuses ) {
			assertTrue( statusesAsList.contains( status ) );
		}
		List<HttpStatus.Series> seriesAsList = Arrays.asList( (HttpStatus.Series[]) content.get( 2 ) );
		assertEquals( expectedSeries.length, seriesAsList.size(), "Actual series do not match expected" );
		for ( HttpStatus.Series serie : expectedSeries ) {
			assertTrue( seriesAsList.contains( serie ) );
		}
	}

	class DummyWebCmsPageEndpoint extends WebCmsEndpoint
	{

	}
}
