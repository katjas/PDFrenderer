package com.sun.pdfview.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PdfUtils {

  private static Rectangle getBounds(PDFPage page) {
    assertNotNull(page);
    return new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
  }
  
  public static void assertEqualImages(BufferedImage actual, BufferedImage expected) {
    assertNotNull(actual);
    assertNotNull(expected);
    assertEquals(actual.getWidth(), expected.getWidth());
    assertEquals(actual.getHeight(), expected.getHeight());
    for (int y = 0; y < actual.getHeight(); y++) {
      for (int x = 0; x < actual.getWidth(); x++) {
        assertEquals(actual.getRGB(x, y), expected.getRGB(x, y));
      }
    }
  }
  
  public static BufferedImage loadImage(byte[] data) {
    assertNotNull(data);
    try {
      return ImageIO.read(new ByteArrayInputStream(data));
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static BufferedImage loadImage(URL resource) {
    return loadImage(loadResource(resource));
  }

  public static BufferedImage convertPageToImage(PDFPage page) {
    assertNotNull(page);
    Rectangle bounds = getBounds(page);
    Image     image  = page.getImage(bounds.width, bounds.height, bounds, null, true, true);
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    } else {
      BufferedImage result   = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
      Graphics      graphics = result.createGraphics();
      graphics.drawImage(image, 0, 0, null);
      graphics.dispose();
      return result;
    }
  }
  
  public static byte[] loadResource(InputStream instream) {
    try {
      ByteArrayOutputStream byteout = new ByteArrayOutputStream();
      byte[] buffer = new byte[16384];
      int read = instream.read(buffer);
      while (read != -1) {
        if (read > 0) {
          byteout.write(buffer, 0, read);
        }
        read = instream.read(buffer);
      }
      return byteout.toByteArray();
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }
  
  public static byte[] loadFile(File file) {
    assertNotNull(file);
    try {
      InputStream instream = new FileInputStream(file);
      try {
        return loadResource(instream);
      } finally {
        instream.close();
      }
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static byte[] loadResource(URL url) {
    assertNotNull(url);
    try {
      InputStream instream = url.openStream();
      try {
        return loadResource(instream);
      } finally {
        instream.close();
      }
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }
  
  public static PDFFile loadPDF(File file) {
    assertNotNull(file);
    try {
      return new PDFFile(ByteBuffer.wrap(loadFile(file)));
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static PDFFile loadPDF(URL resource) {
    assertNotNull(resource);
    try {
      return new PDFFile(ByteBuffer.wrap(loadResource(resource)));
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static PDFPage loadPage(File file, int page) {
    assertNotNull(file);
    try {
      PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(loadFile(file)));
      assertTrue((page >= 0) && (page < pdfFile.getNumPages()));
      PDFPage result = pdfFile.getPage(page);
      assertNotNull(result);
      return result;
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static PDFPage loadPage(URL resource, int page) {
    assertNotNull(resource);
    try {
      PDFFile pdfFile = new PDFFile(ByteBuffer.wrap(loadResource(resource)));
      assertTrue((page >= 0) && (page < pdfFile.getNumPages()));
      PDFPage result = pdfFile.getPage(page);
      assertNotNull(result);
      return result;
    } catch (Exception ex) {
      fail(ex.getLocalizedMessage());
      return null;
    }
  }

  public static URL getResource(String path) {
    assertNotNull(path);
    URL result = PdfUtils.class.getResource(path);
    assertNotNull(result);
    return result;
  }
  
} /* ENDCLASS */
