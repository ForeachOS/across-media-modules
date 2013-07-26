package com.foreach.imageserver.services.transformers;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@org.springframework.stereotype.Component
public class PureJavaImageTransformer implements ImageTransformer
{
	private static final Logger LOG = LoggerFactory.getLogger( PureJavaImageTransformer.class );

	@Override
	public ImageTransformerPriority canExecute( ImageTransformerAction action ) {
		ImageType imageType = action.getImageFile().getImageType();

		if ( imageType == ImageType.JPEG || imageType == ImageType.GIF || imageType == ImageType.PNG ) {
			return ImageTransformerPriority.PREFERRED;
		}
		else if ( imageType == ImageType.SVG || imageType == ImageType.EPS || imageType == ImageType.PDF ) {
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
		InputStream stream = null;
		try {
			stream = action.getImageFile().openContentStream();

			BufferedImage image = ImageIO.read( stream );
			action.setResult( new Dimensions( image.getWidth(), image.getHeight() ) );
		}
		catch ( Exception e ) {
			LOG.error( "Failed to calculate image dimensions {}: {}", action, e );
			throw new ImageModificationException( e );
		}
		finally {
			IOUtils.closeQuietly( stream );
		}
	}

	private void executeModification( ImageModifyAction action ) {
		try {
			ImageFile original = action.getImageFile();
			ImageModifier modifier = action.getModifier();

			BufferedImage bufferedImage = readImage( new MemoryCacheImageInputStream( original.openContentStream() ) );

			bufferedImage = getScaledInstance( bufferedImage, modifier.getWidth(), modifier.getHeight(),
			                                   RenderingHints.VALUE_INTERPOLATION_BILINEAR, false );

			ByteArrayOutputStream os = new ByteArrayOutputStream();

			ImageIO.write( bufferedImage, original.getImageType().getExtension(), os );

			byte[] content = os.toByteArray();

			action.setResult(
					new ImageFile( original.getImageType(), content.length, new ByteArrayInputStream( content ) ) );
		}
		catch ( Exception e ) {
			LOG.error( "exception applying transform {} ", e );
		}
	}

	private static BufferedImage readImage( ImageInputStream is ) throws IOException {
		ImageReader reader = ImageIO.getImageReaders( is ).next();
		try {
			reader.setInput( is );
			ImageReadParam param = reader.getDefaultReadParam();

			ImageTypeSpecifier typeToUse = null;

			for ( Iterator<ImageTypeSpecifier> i = reader.getImageTypes( 0 ); i.hasNext(); ) {
				ImageTypeSpecifier type = i.next();
				if ( type.getColorModel().getColorSpace().isCS_sRGB() ) {
					typeToUse = type;
				}
			}
			if ( typeToUse != null ) {
				param.setDestinationType( typeToUse );
			}

			return reader.read( 0, param );
		}
		finally {
			// guarantee we close the reader here, even if we throw an IOException
			// since readers keep the entire image in the buffer this could lead to out of memory errors otherwise
			reader.dispose();
			// also we intentionally do not close the imageinputstream here, since it was passed to us and maybe it will
			// be used later on.
		}
	}

	private BufferedImage getScaledInstance( BufferedImage img,
	                                         int targetWidth,
	                                         int targetHeight,
	                                         Object interpolationHint,
	                                         boolean preserveAlpha ) {
		boolean hasPossibleAlphaChannel = img.getTransparency() != Transparency.OPAQUE;

		// rescale while ignoring the preserveAlpha flag, otherwise we lose the transparency at this point
		int imageTypeForScaling = hasPossibleAlphaChannel ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage result = img;
		int w = img.getWidth();
		int h = img.getHeight();
		do {
			if ( w > targetWidth ) {
				w = Math.max( w / 2, targetWidth );
			}
			if ( h > targetHeight ) {
				h = Math.max( h / 2, targetHeight );
			}
			BufferedImage tmp = new BufferedImage( w, h, imageTypeForScaling );

			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interpolationHint );
			g2.drawImage( result, 0, 0, w, h, null );
			g2.dispose();
			result = tmp;
		}
		while ( w > targetWidth || h > targetHeight );

		return hasPossibleAlphaChannel && !preserveAlpha ? getFlattenedBufferedImageWithWhiteBG( result ) : result;
	}

	// add white background if we don't want to preserve the alpha channel
	private BufferedImage getFlattenedBufferedImageWithWhiteBG( BufferedImage result ) {
		if ( result.getTransparency() == Transparency.OPAQUE ) {
			// no alpha channel, so no need to transform anything
			return result;
		}
		// create new buffered image with a white background and draw the result onto it
		BufferedImage res = new BufferedImage( result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_RGB );
		Graphics2D graphics = res.createGraphics();
		graphics.setBackground( Color.WHITE );
		graphics.setColor( Color.WHITE );
		graphics.fillRect( 0, 0, result.getWidth(), result.getHeight() );
		graphics.drawImage( result, 0, 0, null );
		graphics.dispose();
		return res;
	}

	@Override
	public int getPriority() {
		return 0;
	}
}
