package com.foreach.imageserver.core.services.exceptions;

public class CropOutsideOfImageBoundsException extends RuntimeException
{
	public CropOutsideOfImageBoundsException( String cause ) {
		super( cause );
	}

	public CropOutsideOfImageBoundsException( Throwable cause ) {
		super( cause );
	}
}
