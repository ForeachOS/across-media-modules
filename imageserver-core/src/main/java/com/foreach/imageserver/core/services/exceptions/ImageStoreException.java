package com.foreach.imageserver.core.services.exceptions;

public class ImageStoreException extends RuntimeException
{
	public ImageStoreException( Throwable cause ) {
		super( cause );
	}

	public ImageStoreException( String cause ) {
		super( cause );
	}
}
