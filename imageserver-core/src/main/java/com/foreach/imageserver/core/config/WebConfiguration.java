package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.imageserver.client.ImageRequestHashBuilder;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.annotations.ImageServerController;
import com.foreach.imageserver.core.controllers.*;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartResolver;

import java.util.Optional;

/**
 * @author Arne Vandamme
 */
@Configuration
public class WebConfiguration
{
	public static final String IMAGE_REQUEST_HASH_BUILDER = "serverImageRequestHashBuilder";

	@Autowired
	private Environment environment;

	@Autowired
	private ImageServerCoreModuleSettings settings;

	/**
	 * Separate handlerMapping that allows its own interceptor collection (for reasons of performance).
	 */
	@Bean
	@Exposed
	public PrefixingRequestMappingHandlerMapping imageServerHandlerMapping() {
		return new PrefixingRequestMappingHandlerMapping(
				environment.getProperty( ImageServerCoreModuleSettings.ROOT_PATH, "" ),
				new AnnotationClassFilter( ImageServerController.class, true )
		);
	}

	@Bean
	@Exposed
	public MultipartResolver multipartResolver( AcrossContextBeanRegistry acrossContextBeanRegistry ) {
		//TODO: fix MultipartResolverConfiguration, it does not find it in DispatcherServlet.initMultipartResolver() ?
		Optional<MultipartResolver>
				m = acrossContextBeanRegistry.findBeanOfTypeFromModule( AcrossWebModule.NAME, MultipartResolver.class );
		return m.get();
	}

	@Bean
	public ImageLoadController imageLoadController() {
		return new ImageLoadController( accessToken() );
	}

	@Bean
	public ImageDeleteController imageDeleteController() {
		return new ImageDeleteController( accessToken() );
	}

	@Bean
	public ImageModificationController imageModificationController() {
		return new ImageModificationController( accessToken() );
	}

	@Bean
	public ImageResolutionController imageResolutionController() {
		return new ImageResolutionController( accessToken() );
	}

	@Bean
	public ImageStreamingController imageStreamingController() {
		ImageStreamingController imageStreamingController =
				new ImageStreamingController( accessToken(), settings.isStrictMode() );
		imageStreamingController.setMaxCacheAgeInSeconds(
				environment.getProperty( ImageServerCoreModuleSettings.MAX_BROWSER_CACHE_SECONDS, Integer.class, 60 ) );

		imageStreamingController.setAkamaiCacheMaxAge(
				environment.getProperty( ImageServerCoreModuleSettings.AKAMAI_CACHE_MAX_AGE, String.class, "" ) );

		imageStreamingController.setProvideStackTrace(
				environment.getProperty( ImageServerCoreModuleSettings.PROVIDE_STACKTRACE, Boolean.class, false ) );

		return imageStreamingController;
	}

	@Bean(name = IMAGE_REQUEST_HASH_BUILDER)
	@Primary
	@ConditionalOnExpression("!${" + ImageServerCoreModuleSettings.STRICT_MODE + ":false} && '${" + ImageServerCoreModuleSettings.MD5_HASH_TOKEN + ":}'.length() > 0")
	public ImageRequestHashBuilder serverImageRequestHashBuilder() {
		return ImageRequestHashBuilder.md5(
				environment.getRequiredProperty( ImageServerCoreModuleSettings.MD5_HASH_TOKEN )
		);
	}

	private String accessToken() {
		return environment.getProperty( ImageServerCoreModuleSettings.ACCESS_TOKEN, "azerty" );
	}
}
