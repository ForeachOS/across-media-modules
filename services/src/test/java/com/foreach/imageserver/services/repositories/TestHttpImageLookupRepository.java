package com.foreach.imageserver.services.repositories;

import com.foreach.imageserver.services.DummyWebServer;
import com.foreach.imageserver.services.ImageTestData;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestHttpImageLookupRepository
{
	private static final DummyWebServer webServer = new DummyWebServer();

	private ImageLookupRepository imageLookupRepository;

	@Before
	public void before() {
		imageLookupRepository = new HttpImageLookupRepository();
	}

	@BeforeClass
	public static void startWebServer() throws Exception {
		webServer.start();
	}

	@AfterClass
	public static void shutdownWebServer() throws Exception {
		webServer.stop();
	}

	@Test
	public void validURI() {
		assertTrue( imageLookupRepository.isValidURI( "http://www.google.be:80/" ) );
		assertTrue( imageLookupRepository.isValidURI( "https://www.foreach.com/sdfqsdfsd?test=boe&kipe=jio" ) );
	}

	@Test
	public void invalidURI() {
		assertFalse( imageLookupRepository.isValidURI( "somerandomstring" ) );
	}

	@Test
	public void imageNotFoundStatusCode() {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( webServer.notFoundUrl() );
		assertEquals( RepositoryLookupStatus.NOT_FOUND, lookupResult.getStatus() );
	}

	@Test
	public void permissionDeniedStatusCode() {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( webServer.permissionDeniedUrl() );
		assertEquals( RepositoryLookupStatus.ACCESS_DENIED, lookupResult.getStatus() );
	}

	@Test
	public void errorStatusCode() {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( webServer.errorUrl() );
		assertEquals( RepositoryLookupStatus.ERROR, lookupResult.getStatus() );
	}

	@Test
	public void getImages() throws Exception {
		for ( ImageTestData image : ImageTestData.values() ) {
			getValidImage( image );
		}
	}

	private void getValidImage( ImageTestData imageTestData ) throws Exception {
		RepositoryLookupResult lookupResult = imageLookupRepository.fetchImage( webServer.imageUrl( imageTestData ) );

		assertEquals( RepositoryLookupStatus.SUCCESS, lookupResult.getStatus() );
		assertEquals( imageTestData.getImageType(), lookupResult.getImageType() );
		assertNotNull( lookupResult.getContent() );
		assertTrue( IOUtils.contentEquals( getClass().getResourceAsStream( imageTestData.getResourcePath() ),
		                                   lookupResult.getContent() ) );
	}
}
