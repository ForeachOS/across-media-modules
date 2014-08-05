package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.imageserver.core.ImageServerCoreModuleSettings;
import com.foreach.imageserver.core.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

/**
 * @author Arne Vandamme
 */
@Configuration
public class ServicesConfiguration
{
	@Autowired
	private Environment environment;

	@Bean
	@Exposed
	public ImageService imageService() {
		return new ImageServiceImpl();
	}

	@Bean
	@Exposed
	public ImageTransformService imageTransformService() {
		return new ImageTransformServiceImpl(
				environment.getProperty( ImageServerCoreModuleSettings.TRANSFORMERS_CONCURRENT_LIMIT, Integer.class,
				                         10 ) );
	}

	@Bean
	@Exposed
	public ContextService contextService() {
		return new ContextServiceImpl();
	}

	@Bean
	public ImageStoreService imageStoreService() throws IOException {
		return new ImageStoreServiceImpl( environment );
	}

	@Bean
	public CropGenerator cropGenerator() {
		return new CropGeneratorImpl();
	}
}