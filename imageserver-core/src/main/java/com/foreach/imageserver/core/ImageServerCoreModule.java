package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.core.database.SchemaConfiguration;
import com.foreach.across.core.filters.AnnotationBeanFilter;
import com.foreach.across.core.installers.AcrossSequencesInstaller;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.hibernate.provider.*;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.imageserver.core.config.ImageSchemaConfiguration;
import com.foreach.imageserver.core.installers.DefaultDataInstaller;
import com.foreach.imageserver.core.installers.Image404Installer;
import com.foreach.imageserver.core.installers.InitialSchemaInstaller;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

@AcrossDepends(required = { AcrossWebModule.NAME, AcrossHibernateJpaModule.NAME, FileManagerModule.NAME })
public class ImageServerCoreModule extends AcrossModule implements HibernatePackageConfigurer
{
	public static final String NAME = "ImageServerCoreModule";
	private final SchemaConfiguration schemaConfiguration = new ImageSchemaConfiguration();

	@SuppressWarnings("unchecked")
	public ImageServerCoreModule() {
		setExposeFilter( new AnnotationBeanFilter( true, true, Exposed.class ) );
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "ImageServer core module";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		/*contextConfigurers.add(
				new AnnotatedClassConfigurer(
						ServicesConfiguration.class,
						RepositoriesConfiguration.class,
						WebConfiguration.class,
						LocalImageServerClientConfiguration.class
				)
		);*/
		contextConfigurers.add( ComponentScanConfigurer.forAcrossModule( ImageServerCoreModule.class ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				DefaultDataInstaller.class,
				AcrossSequencesInstaller.class,
				InitialSchemaInstaller.class,
				Image404Installer.class
		};
	}

	/**
	 * Configures the package provider associated with this implementation.
	 *
	 * @param hibernatePackage HibernatePackageRegistry.
	 */
	@Override
	public void configureHibernatePackage( HibernatePackageRegistry hibernatePackage ) {
		if ( StringUtils.equals( AcrossHibernateJpaModule.NAME, hibernatePackage.getName() ) ) {
			hibernatePackage.add( new HibernatePackageProviderComposite(
					new PackagesToScanProvider( "com.foreach.imageserver.core.business" ),
					new TableAliasProvider( schemaConfiguration.getTables() ) ) );
		}
	}
}
