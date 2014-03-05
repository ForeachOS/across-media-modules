package com.foreach.imageserver.core;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.StoredImageModification;
import com.foreach.imageserver.core.web.interceptors.GlobalVariableInterceptor;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.foreach.imageserver.core", excludeFilters = @ComponentScan.Filter(Configuration.class))
@MapperScan("com.foreach.imageserver.core.data")
public class ImageServerCoreConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public GlobalVariableInterceptor globalVariableInterceptor() {
        return new GlobalVariableInterceptor();
    }

//    @Bean
//    public WebContentInterceptor cachingInterceptor() {
//        WebContentInterceptor interceptor = new WebContentInterceptor();
//        interceptor.setCacheSeconds(0);
//        interceptor.setUseExpiresHeader(true);
//        interceptor.setUseCacheControlHeader(true);
//        interceptor.setUseCacheControlNoStore(true);
//
//        return interceptor;
//    }
//
//    @Bean
//    public UrlBasedViewResolver viewResolver() {
//        UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
//        viewResolver.setViewClass(JstlView.class);
//        viewResolver.setPrefix("/WEB-INF/jsp/");
//        viewResolver.setRedirectContextRelative(true);
//        viewResolver.setSuffix(".jsp");
//        return viewResolver;
//    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public org.apache.ibatis.session.SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setTypeAliases(new Class[]{Application.class, Image.class, StoredImageModification.class});
        return sessionFactory.getObject();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setDataSource(dataSource);
        springLiquibase.setChangeLog("classpath:com/foreach/imageserver/core/liquibase/changelog.xml");
        return springLiquibase;
    }

}