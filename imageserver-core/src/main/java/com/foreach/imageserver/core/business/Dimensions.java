package com.foreach.imageserver.core.business;

import com.foreach.imageserver.math.AspectRatio;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * A simple object to specify the actual dimensions of an Image.
 * <p/>
 * Not to be confused with the ImageResolution, which specifies a permitted output resolution which may be unrestricted
 * for a certain dimension.
 */
@Embeddable
@Getter
@Setter
public class Dimensions
{
	public static final Dimensions EMPTY = new Dimensions();

	private int width;
	private int height;

	public Dimensions( int width, int height ) {
		this.width = width;
		this.height = height;
	}

	public Dimensions() {
	}

	public AspectRatio fetchAspectRatio() {
		return new AspectRatio( width, height );
	}

	public boolean fitsIn( Dimensions boundaries ) {
		return getWidth() <= boundaries.getWidth() && getHeight() <= boundaries.getHeight();
	}

	/**
	 * Will calculate the unknown dimensions according to the boundaries specified.
	 * Any unknown dimensions will be scaled according to the aspect ratio of the boundaries.
	 */
	public Dimensions normalize( Dimensions boundaries ) {
		Dimensions normalized = new Dimensions( width, height );

		AspectRatio originalAspectRatio = boundaries.fetchAspectRatio();

		if ( width == 0 && height == 0 ) {
			normalized.setWidth( boundaries.getWidth() );
			normalized.setHeight( boundaries.getHeight() );
		}
		else if ( height == 0 ) {
			normalized.setHeight( originalAspectRatio.calculateHeightForWidth( width ) );
		}
		else if ( width == 0 ) {
			normalized.setWidth( originalAspectRatio.calculateWidthForHeight( height ) );
		}

		return normalized;
	}

	/**
	 * Will verify and modify the dimensions so that they match the aspect ratio.
	 * The largest dimension according to aspect ratio is kept.
	 */
	public Dimensions normalize( AspectRatio aspectRatio ) {
		Dimensions normalized = new Dimensions( width, height );

		if ( !normalized.fetchAspectRatio().equals( aspectRatio ) ) {
			if ( aspectRatio.getNumerator() > aspectRatio.getDenominator() ) {
				normalized.setHeight( aspectRatio.calculateHeightForWidth( normalized.getWidth() ) );
			}
			else {
				normalized.setWidth( aspectRatio.calculateWidthForHeight( normalized.getHeight() ) );
			}
		}

		return normalized;
	}

	/**
	 * Will downscale the dimensions to fit in the boundaries if they are larger.
	 */
	public Dimensions scaleToFitIn( Dimensions boundaries ) {
		Dimensions normalized = normalize( boundaries );
		Dimensions scaled = new Dimensions( normalized.getWidth(), normalized.getHeight() );

		AspectRatio aspectRatio = normalized.fetchAspectRatio();

		if ( !normalized.fitsIn( boundaries ) ) {
			if ( normalized.fetchAspectRatio().isLargerOnWidth() ) {
				scaled.setWidth( boundaries.getWidth() );
				scaled.setHeight( aspectRatio.calculateHeightForWidth( boundaries.getWidth() ) );
			}
			else {
				scaled.setHeight( boundaries.getHeight() );
				scaled.setWidth( aspectRatio.calculateWidthForHeight( boundaries.getHeight() ) );
			}

			if ( !scaled.fitsIn( boundaries ) ) {
				// Reverse the side as scaling basis, we made the wrong decision
				if ( normalized.fetchAspectRatio().isLargerOnWidth() ) {
					scaled.setHeight( boundaries.getHeight() );
					scaled.setWidth( aspectRatio.calculateWidthForHeight( boundaries.getHeight() ) );
				}
				else {
					scaled.setWidth( boundaries.getWidth() );
					scaled.setHeight( aspectRatio.calculateHeightForWidth( boundaries.getWidth() ) );

				}
			}
		}

		return scaled;
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		Dimensions that = (Dimensions) o;

		return Objects.equals( this.height, that.height ) && Objects.equals( this.width, that.width );
	}

	@Override
	public int hashCode() {
		int result = width;
		result = 31 * result + height;
		return result;
	}
}
