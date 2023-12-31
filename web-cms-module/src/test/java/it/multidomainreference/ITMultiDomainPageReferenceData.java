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

package it.multidomainreference;

import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpoint;
import com.foreach.across.modules.webcms.domain.asset.WebCmsAssetEndpointRepository;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomain;
import com.foreach.across.modules.webcms.domain.domain.WebCmsDomainRepository;
import com.foreach.across.modules.webcms.domain.page.WebCmsPage;
import com.foreach.across.modules.webcms.domain.page.repositories.WebCmsPageRepository;
import com.foreach.across.modules.webcms.domain.url.WebCmsUrl;
import it.AbstractMultiDomainCmsApplicationWithTestDataIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

public class ITMultiDomainPageReferenceData extends AbstractMultiDomainCmsApplicationWithTestDataIT
{
	@Autowired
	private WebCmsPageRepository pageRepository;

	@Autowired
	private WebCmsDomainRepository domainRepository;

	@Autowired
	private WebCmsAssetEndpointRepository assetEndpointRepository;

	@Test
	void faqPageForeachNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain ).orElse( null );
		assertNotNull( page );
	}

	@Test
	void faqPageForeachBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain ).orElse( null );
		assertNotNull( page );
	}

	@Test
	void faqPageForeachDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/faq", domain ).orElse( null );
		assertNotNull( page );
	}

	@Test
	void cafePageNlShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain ).orElse( null );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (NL)", page.getTitle() );
	}

	@Test
	void cafePageBeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain ).orElse( null );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (BE)", page.getTitle() );
	}

	@Test
	void cafePageDeShouldHaveBeenImported() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/cafe", domain ).orElse( null );
		assertNotNull( page );
		assertEquals( "Foreach Cafe (DE)", page.getTitle() );
	}

	@Test
	void myPageBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-page", domain ).orElse( null );
		assertNull( page );
	}

	@Test
	void myPageNlShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-page", domain ).orElse( null );
		assertNotNull( page );
		assertEquals( "My Page (NL)", page.getTitle() );
	}

	@Test
	void myOtherPageBeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain ).orElse( null );
		assertNull( page );
	}

	@Test
	void myOtherPageDeShouldHaveBeenDeleted() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "de-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain ).orElse( null );
		assertNull( page );
	}

	@Test
	void myOtherPageNlShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "nl-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/my-other-page", domain ).orElse( null );
		assertNotNull( page );
		assertEquals( "My Other Page (NL)", page.getTitle() );
	}

	@Test
	@Transactional
	void pageWithUrlShouldHaveBeenImported() {
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/page-with-url", WebCmsDomain.NONE ).orElse( null );
		assertNotNull( page );
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, WebCmsDomain.NONE ).orElse( null );
		assertEquals( 3, endpoint.getUrls().size() );
		WebCmsUrl url = endpoint.getUrlWithPath( "/page-with-url" ).orElse( null );
		assertNotNull( url );
		assertTrue( url.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), url.getHttpStatus() );

		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertFalse( testUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), testUrl.getHttpStatus() );

		WebCmsUrl myOtherUrl = endpoint.getUrlWithPath( "/my-other-url" ).orElse( null );
		assertNotNull( myOtherUrl );
		assertFalse( myOtherUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 301 ), myOtherUrl.getHttpStatus() );
	}

	@Test
	@Transactional
	void otherPageWithUrlShouldHaveItsSinglePrimaryChangedAndLocked() {
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/other-page-with-url", WebCmsDomain.NONE ).orElse( null );
		assertNotNull( page );
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, WebCmsDomain.NONE ).orElse( null );
		assertEquals( 1, endpoint.getUrls().size() );

		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/other-test-url" ).orElse( null );
		assertNotNull( testUrl );
		assertTrue( testUrl.isPrimary() );
		assertTrue( testUrl.isPrimaryLocked() );
		assertEquals( HttpStatus.valueOf( 200 ), testUrl.getHttpStatus() );

		WebCmsUrl myOtherUrl = endpoint.getUrlWithPath( "/to-be-deleted" ).orElse( null );
		assertNull( myOtherUrl );
	}

	@Test
	@Transactional
	void pageWithUrlBeShouldHaveBeenImportedAndExtended() {
		WebCmsDomain domain = domainRepository.findOneByDomainKey( "be-foreach" ).orElse( null );
		WebCmsPage page = pageRepository.findOneByCanonicalPathAndDomain( "/page-with-url-be", domain ).orElse( null );
		assertNotNull( page );
		WebCmsAssetEndpoint endpoint = assetEndpointRepository.findOneByAssetAndDomain( page, domain ).orElse( null );
		assertEquals( 2, endpoint.getUrls().size() );
		WebCmsUrl url = endpoint.getUrlWithPath( "/page-with-url-be" ).orElse( null );
		assertNotNull( url );
		assertTrue( url.isPrimary() );
		assertEquals( HttpStatus.valueOf( 200 ), url.getHttpStatus() );

		WebCmsUrl testUrl = endpoint.getUrlWithPath( "/test-url-be" ).orElse( null );
		assertNotNull( testUrl );
		assertFalse( testUrl.isPrimary() );
		assertEquals( HttpStatus.valueOf( 301 ), testUrl.getHttpStatus() );

		WebCmsUrl myOtherUrl = endpoint.getUrlWithPath( "/to-be-deleted" ).orElse( null );
		assertNull( myOtherUrl );
	}
}
