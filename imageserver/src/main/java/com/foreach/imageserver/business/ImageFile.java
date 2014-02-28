package com.foreach.imageserver.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageFile
{
	private final ImageType imageType;
	private final long fileSize;
	private final File physicalFile;
	private final InputStream content;

	public ImageFile( ImageType imageType, File physicalFile ) {
		this( imageType, physicalFile, physicalFile.length() );
	}

	public ImageFile( ImageType imageType, File physicalFile, long fileSize ) {
		this.physicalFile = physicalFile;
		this.imageType = imageType;
		this.fileSize = fileSize;
		this.content = null;
	}

	public ImageFile( ImageType imageType, long fileSize, InputStream content ) {
		this.imageType = imageType;
		this.fileSize = fileSize;
		this.content = content;
		this.physicalFile = null;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public long getFileSize() {
		return fileSize;
	}

	public InputStream openContentStream() throws IOException {
		return content != null ? content : new FileInputStream( physicalFile );
	}

	protected File getPhysicalFile() {
		return physicalFile;
	}

	@Override
	public String toString() {
		return "ImageFile{" +
				"imageType=" + imageType +
				", physicalFile=" + physicalFile +
				'}';
	}
}