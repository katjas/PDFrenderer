package com.sun.pdfview.function;

import java.awt.image.BufferedImage;
import java.net.URL;

import org.junit.Test;

import com.sun.pdfview.PDFPage;

public class PreviewGenerationTest {

  // the contained umlaut 'ü' has not been rendered
  @Test
  public void vda541() throws Exception {
    URL inputPdf = PdfUtils.getResource("/vda_541/input.pdf");
    PDFPage page = PdfUtils.loadPage(inputPdf, 0);
    BufferedImage image = PdfUtils.convertPageToImage(page);
    BufferedImage expected = PdfUtils.loadImage(PdfUtils.getResource("/vda_541/expected.png"));
    PdfUtils.assertEqualImages(image, expected);
  }
  
} /* ENDCLASS */
