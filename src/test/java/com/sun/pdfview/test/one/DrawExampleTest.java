package com.sun.pdfview.test.one;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.w3c.dom.Element;

import com.sun.imageio.plugins.jpeg.JPEGImageWriter;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFParser;

public class DrawExampleTest extends TestCase
{
	File file;
	File outputDir;

	public DrawExampleTest(File file, File outputDir)
	{
		super("test" /* this is test method name */);
		this.file = file;
		this.outputDir = outputDir;
	}
	
	public void test() throws Exception
	{
		System.out.println("==>" + getName());
		BufferedImage image = createImage(file);
		
		File outputFile = new File(outputDir, StringUtils.removeEndIgnoreCase(file.getName(), ".pdf") + ".jpg");
		OutputStream os = null;
		try
		{
			os = new BufferedOutputStream(new FileOutputStream(outputFile));
			saveAsJPEG(image, os, 2, null);
		}
		finally
		{
			IOUtils.closeQuietly(os);
		}
		FileUtils.copyFileToDirectory(file, outputDir);
		
		System.out.println("<==" + getName());
	}
	
	@Override
	public String getName()
	{
		return file.getName();
	}
	
	public BufferedImage createImage(File file) throws IOException 
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		FileChannel channel = raf.getChannel();
		ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

		PDFParser.setSuppressSetErrorStackTrace(false);
		
		PDFFile pdffile = new PDFFile(buf);

		// draw the first page to an image
		PDFPage page = pdffile.getPage(1);
		
		int width = 2000;
		int height = (int)(width * (page.getHeight() / page.getWidth()));
		
		//generate the image
		Image image = page.getImage(
							width, height, //width & height
							new Rectangle(0, 0, (int)page.getBBox().getWidth(), (int)page.getBBox().getHeight()), // clip rect
							null, // null for the ImageObserver
							true, // fill background with white
							true  // block until drawing is done
					);

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = result.createGraphics();
		//g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
		g2.drawImage(image, 0, 0, width, height, null);
		g2.dispose();
		
		return result;
	}


	private void saveAsJPEG(BufferedImage image, OutputStream outputStream, float jpgCompression, String jpgFlag) throws IOException
	{
		// Image writer
		JPEGImageWriter imageWriter = (JPEGImageWriter)ImageIO.getImageWritersBySuffix("jpeg").next();
		ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
		imageWriter.setOutput(ios);

		// and metadata
		IIOMetadata imageMetaData = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image), null);

		if (jpgFlag != null)
		{

			int dpi = 96;
			try
			{
				dpi = Integer.parseInt(jpgFlag);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			Element tree = (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
			Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
			jfif.setAttribute("Xdensity", Integer.toString(dpi));
			jfif.setAttribute("Ydensity", Integer.toString(dpi));
		}

		JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
		if (jpgCompression >= 0 && jpgCompression <= 1)
		{
			jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
			jpegParams.setCompressionQuality(jpgCompression);
		}
		
		imageWriter.write(imageMetaData, new IIOImage(image, null, null), jpegParams);
		ios.close();
		imageWriter.dispose();

	}

}
