package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.repositories.ImageRepository;
import com.foreach.imageserver.core.repositories.ImageRepositoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoriesConfiguration
{
	@Bean
	public ImageRepository imageRepository() {
		return new ImageRepositoryImpl();
	}
}
