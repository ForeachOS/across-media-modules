package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import org.apache.commons.io.IOUtils;
import org.im4java.core.IMOperation;
import org.im4java.core.IdentifyCmd;
import org.im4java.core.Info;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.Pipe;
import org.im4java.process.ProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;

@Component
public class ImageMagickImageTransformer implements ImageTransformer
{
	private static final int GS_DENSITY = 600;
	private static final Logger LOG = LoggerFactory.getLogger( ImageMagickImageTransformer.class );

	private final boolean ghostScriptEnabled;

	@Autowired
	public ImageMagickImageTransformer( @Value("${transformer.imagemagick.path}") String imageMagickPath,
	                                    @Value("${transformer.imagemagick.ghostscript}") boolean ghostScriptEnabled ) {
		this.ghostScriptEnabled = ghostScriptEnabled;

		ProcessStarter.setGlobalSearchPath( new File( imageMagickPath ).getAbsolutePath() );
	}

	@Override
	public ImageTransformerPriority canExecute( ImageTransformerAction action ) {
		ImageType imageType = action.getImageFile().getImageType();

		if ( ( imageType == ImageType.EPS || imageType == ImageType.PDF ) && !ghostScriptEnabled ) {
			return ImageTransformerPriority.UNABLE;
		}

		return ImageTransformerPriority.FALLBACK;
	}

	@Override
	public void execute( ImageTransformerAction action ) {
		if ( action instanceof ImageModifyAction ) {
			executeModification( (ImageModifyAction) action );
		}
		else if ( action instanceof ImageCalculateDimensionsAction ) {
			calculateDimensions( (ImageCalculateDimensionsAction) action );
		}
	}

	private void calculateDimensions( ImageCalculateDimensionsAction action ) {
		ImageFile imageFile = action.getImageFile();

		InputStream stream = null;
		try {
			stream = imageFile.openContentStream();

			if ( imageFile.getImageType() == ImageType.EPS ) {
				IdentifyCmd identifyCmd = new IdentifyCmd();

				IMOperation op = new IMOperation();
				op.density( GS_DENSITY );
				op.addImage( "-" );

				ArrayListOutputConsumer output = new ArrayListOutputConsumer();
				identifyCmd.setInputProvider( new Pipe( stream, null ) );
				identifyCmd.setOutputConsumer( output );
				identifyCmd.run( op );

				String[] wxh = output.getOutput().get( 0 ).split( " " )[2].split( "x" );

				action.setResult( new Dimensions( Integer.valueOf( wxh[0] ), Integer.valueOf( wxh[1] ) ) );
			}
			else {
				Info info = new Info( "-", stream, true );
				action.setResult( new Dimensions( info.getImageWidth(), info.getImageHeight() ) );
			}
		}
		catch ( Exception e ) {
			LOG.error( "Failed to get image dimensions {}: {}", action, e );
			throw new ImageModificationException( e );
		}
		finally {
			IOUtils.closeQuietly( stream );
		}
	}

	private void executeModification( ImageModifyAction action ) {
	}

	@Override
	public int getPriority() {
		return -1;
	}
}
