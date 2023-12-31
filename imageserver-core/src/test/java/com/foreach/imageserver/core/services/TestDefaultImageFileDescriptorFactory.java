package com.foreach.imageserver.core.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.config.ServicesConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDefaultImageFileDescriptorFactory
{
	private DefaultImageFileDescriptorFactory defaultImageFileDescriptorFactory;
	private Image image;
	private ImageVariant variantImage;
	private String customRepository;

	private String originalPath = "2018/12/11/09";
	private String variantPath = "2018/12/11/09";

	@BeforeEach
	public void setup() {
		defaultImageFileDescriptorFactory = new DefaultImageFileDescriptorFactory();

		image = new Image();
		image.setId( 650071L );
		image.setExternalId( "650071" );
		image.setOriginalPath( originalPath );
		image.setVariantPath( variantPath );
		image.setTemporaryImage( false );
		image.setImageType( ImageType.PNG );

		variantImage = new ImageVariant();
		variantImage.setOutputType( ImageType.JPEG );

		customRepository = "s3-files";
	}

	@Test
	public void oldImageOriginalPathGivesRightFileDescriptor() {
		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForOriginal( image );

		Assertions.assertEquals( ServicesConfiguration.IMAGESERVER_ORIGINALS_REPOSITORY, fileDescriptor.getRepositoryId() );
		Assertions.assertEquals( "2018/12/11/09", fileDescriptor.getFolderId() );
		Assertions.assertEquals( "imageserver-originals:2018/12/11/09:650071.png", fileDescriptor.getUri() );
		Assertions.assertEquals( "650071.png", fileDescriptor.getFileId() );
	}

	@Test
	public void newImageOriginalPathGivesRightFileDescriptor() {
		image.setOriginalPath( customRepository + ":" + originalPath );
		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForOriginal( image );

		Assertions.assertEquals( "s3-files", fileDescriptor.getRepositoryId() );
		Assertions.assertEquals( "2018/12/11/09", fileDescriptor.getFolderId() );
		Assertions.assertEquals( "s3-files:2018/12/11/09:650071.png", fileDescriptor.getUri() );
		Assertions.assertEquals( "650071.png", fileDescriptor.getFileId() );
	}

	@Test
	public void oldImageVariantPathGivesRightFileDescriptor() {
		ImageContext imageContext = new ImageContext();
		imageContext.setId( 650071L );
		imageContext.setCode( "ONLINE" );

		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setWidth( 300 );
		imageResolution.setHeight( 200 );

		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForVariant( image, imageContext, imageResolution, variantImage );

		Assertions.assertEquals( ServicesConfiguration.IMAGESERVER_VARIANTS_REPOSITORY, fileDescriptor.getRepositoryId() );
		Assertions.assertEquals( "ONLINE/2018/12/11/09", fileDescriptor.getFolderId() );
		Assertions.assertEquals( "imageserver-variants:ONLINE/2018/12/11/09:650071-w300-h200.jpeg", fileDescriptor.getUri() );
		Assertions.assertEquals( "650071-w300-h200.jpeg", fileDescriptor.getFileId() );
	}

	@Test
	public void newImageVariantPathGivesRightFileDescriptor() {
		image.setVariantPath( customRepository + ":" + originalPath );

		ImageContext imageContext = new ImageContext();
		imageContext.setId( 650071L );
		imageContext.setCode( "ONLINE" );

		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setWidth( 300 );
		imageResolution.setHeight( 200 );

		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForVariant( image, imageContext, imageResolution, variantImage );

		Assertions.assertEquals( "s3-files", fileDescriptor.getRepositoryId() );
		Assertions.assertEquals( "ONLINE/2018/12/11/09", fileDescriptor.getFolderId() );
		Assertions.assertEquals( "s3-files:ONLINE/2018/12/11/09:650071-w300-h200.jpeg", fileDescriptor.getUri() );
		Assertions.assertEquals( "650071-w300-h200.jpeg", fileDescriptor.getFileId() );
	}

	@Test
	public void politiek() {
		image.setVariantPath( customRepository + ":" + originalPath );

		ImageContext imageContext = new ImageContext();
		imageContext.setId( 650071L );
		imageContext.setCode( "ONLINE/politiek" );

		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setWidth( 300 );
		imageResolution.setHeight( 200 );

		FileDescriptor fileDescriptor = defaultImageFileDescriptorFactory.createForVariant( image, imageContext, imageResolution, variantImage );

		Assertions.assertEquals( "s3-files", fileDescriptor.getRepositoryId() );
		Assertions.assertEquals( "ONLINE/politiek/2018/12/11/09", fileDescriptor.getFolderId() );
		Assertions.assertEquals( "s3-files:ONLINE/politiek/2018/12/11/09:650071-w300-h200.jpeg", fileDescriptor.getUri() );
		Assertions.assertEquals( "650071-w300-h200.jpeg", fileDescriptor.getFileId() );
	}

}
