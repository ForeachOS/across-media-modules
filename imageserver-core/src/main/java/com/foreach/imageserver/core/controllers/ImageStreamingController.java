package com.foreach.imageserver.core.controllers;

import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.rest.request.ViewImageRequest;
import com.foreach.imageserver.core.rest.response.ViewImageResponse;
import com.foreach.imageserver.core.rest.services.ImageRestService;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import com.foreach.imageserver.logging.LogHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ImageServerController
public class ImageStreamingController
{
	public static final String VIEW_PATH = "/view";
	public static final String RENDER_PATH = "/api/image/render";

	private static final Logger LOG = LoggerFactory.getLogger( ImageStreamingController.class );

	// explicit logging of requested resolutions and images that do not exist
	private static final Logger LOG_RESOLUTION_NOT_FOUND = LoggerFactory.getLogger( ImageResolution.class );
	private static final Logger LOG_IMAGE_NOT_FOUND = LoggerFactory.getLogger( Image.class );

	public static final String AKAMAI_EDGE_CONTROL_HEADER = "Edge-Control";
	public static final String AKAMAI_CACHE_MAX_AGE = "!no-store, cache-maxage=";
	public static final String AKAMAI_NO_STORE = "no-store";

	@Autowired
	private ImageRestService imageRestService;

	private String accessToken;
	private boolean provideStackTrace = false;
	private int maxCacheAgeInSeconds = 30;

	private String akamaiCacheMaxAge = "";

	public ImageStreamingController( String accessToken ) {
		this.accessToken = accessToken;
	}

	public void setAccessToken( String accessToken ) {
		this.accessToken = accessToken;
	}

	public void setProvideStackTrace( boolean provideStackTrace ) {
		this.provideStackTrace = provideStackTrace;
	}

	public void setMaxCacheAgeInSeconds( int maxCacheAgeInSeconds ) {
		this.maxCacheAgeInSeconds = maxCacheAgeInSeconds;
	}

	public void setAkamaiCacheMaxAge( String akamaiCacheMaxAge ) {
		this.akamaiCacheMaxAge = akamaiCacheMaxAge;
	}

	@RequestMapping(value = RENDER_PATH, method = RequestMethod.GET)
	public void render( @RequestParam(value = "token", required = true) String accessToken,
	                    @RequestParam(value = "iid", required = true) String externalId,
	                    ImageModificationDto imageModificationDto,
	                    ImageVariantDto imageVariantDto,
	                    HttpServletResponse response ) {

		if ( !this.accessToken.equals( accessToken ) ) {
			error( response, HttpStatus.FORBIDDEN, "Access denied." );
		}

		ViewImageRequest renderImageRequest = new ViewImageRequest();
		renderImageRequest.setExternalId( externalId );
		renderImageRequest.setImageModificationDto( imageModificationDto );
		renderImageRequest.setImageVariantDto( imageVariantDto );

		ViewImageResponse renderImageResponse = imageRestService.renderImage( renderImageRequest );

		if ( renderImageResponse.isImageDoesNotExist() ) {
			error( response, HttpStatus.NOT_FOUND, "No such image." );
		}
		else if ( renderImageResponse.isFailed() ) {
			error( response, HttpStatus.NOT_FOUND, "Could not create variant." );
		}
		else {
			renderImageSource( renderImageResponse.getImageSource(), response );
		}
	}

	@RequestMapping(value = VIEW_PATH, method = RequestMethod.GET)
	public void view( @RequestParam(value = "iid", required = true) String externalId,
	                  @RequestParam(value = "context", required = true) String contextCode,
	                  ImageResolutionDto imageResolutionDto,
	                  ImageVariantDto imageVariantDto,
	                  HttpServletResponse response ) {
		// TODO Make sure we only rely on objects that can be long-term cached for retrieving the image.

		try {
			ViewImageRequest viewImageRequest = new ViewImageRequest();
			viewImageRequest.setExternalId( externalId );
			viewImageRequest.setContext( contextCode );
			viewImageRequest.setImageResolutionDto( imageResolutionDto );
			viewImageRequest.setImageVariantDto( imageVariantDto );

			ViewImageResponse viewImageResponse = imageRestService.viewImage( viewImageRequest );

			if ( viewImageResponse.isImageDoesNotExist() ) {
				LOG_IMAGE_NOT_FOUND.error( externalId );
				error( response, HttpStatus.NOT_FOUND, "No such image." );
			}
			else if ( viewImageResponse.isContextDoesNotExist() ) {
				error( response, HttpStatus.NOT_FOUND, "No such context." );
			}
			else if ( viewImageResponse.isResolutionDoesNotExist() ) {
				LOG_RESOLUTION_NOT_FOUND.error(imageResolutionDto.getWidth() + "x" + imageResolutionDto.getHeight());
				error( response, HttpStatus.NOT_FOUND, "No such resolution." );
			}
			else if ( viewImageResponse.isOutputTypeNotAllowed() ) {
				error( response, HttpStatus.NOT_FOUND, "Requested output type is not allowed." );
			}
			else if ( viewImageResponse.isFailed() ) {
				error( response, HttpStatus.NOT_FOUND, "Could not create variant." );
			}
			else {
				renderImageSource( viewImageResponse.getImageSource(), response );
			}

			// fail-safe to avoid that stack traces are shown when an unexpected exception occurs
		}
		catch ( Exception e ) {
			// log the exception context and either send a clean error (in production) or rethrow the exception (anywhere else)
			LOG.error(
					"Retrieving image variant caused exception - ImageStreamingController#view: externalId={}, contextCode={}, imageResolutionDto={}, imageVariantDto={}",
					externalId, contextCode, LogHelper.flatten( imageResolutionDto ),
					LogHelper.flatten( imageVariantDto ), e );
			if ( provideStackTrace ) {
				throw e;
			}
			else {
				error( response, HttpStatus.INTERNAL_SERVER_ERROR, "Error encountered while retrieving variant." );
			}
		}
	}

	private void renderImageSource( StreamImageSource imageSource, HttpServletResponse response ) {
		response.setStatus( HttpStatus.OK.value() );
		response.setContentType( imageSource.getImageType().getContentType() );

		if ( maxCacheAgeInSeconds > 0 ) {
			response.setHeader( "Cache-Control", String.format( "max-age=%d", maxCacheAgeInSeconds ) );
		}
		if (  akamaiCacheMaxAge != "") {
			response.setHeader( AKAMAI_EDGE_CONTROL_HEADER, AKAMAI_CACHE_MAX_AGE + akamaiCacheMaxAge );
		}

		try (InputStream imageStream = imageSource.getImageStream()) {
			try (OutputStream responseStream = response.getOutputStream()) {
				IOUtils.copy( imageStream, responseStream );
			}
		}
		catch ( IOException ioe ) {
			error( response, HttpStatus.INTERNAL_SERVER_ERROR, ioe.getMessage() );
		}
	}

	private void error( HttpServletResponse response, HttpStatus status, String errorMessage ) {
		response.setStatus( status.value() );
		response.setContentType( "text/plain" );
		response.setHeader( "Cache-Control", "no-cache" );
		response.setHeader( AKAMAI_EDGE_CONTROL_HEADER, AKAMAI_NO_STORE );
		try (ByteArrayInputStream bis = new ByteArrayInputStream( errorMessage.getBytes() )) {
			IOUtils.copy( bis, response.getOutputStream() );
		}
		catch ( IOException e ) {
			LOG.error( "Failed to write error message to output stream: errorMessage={}", errorMessage, e );
		}
	}
}
