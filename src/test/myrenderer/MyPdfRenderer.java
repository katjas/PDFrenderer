package test.myrenderer;

import javax.swing.JFrame;

public class MyPdfRenderer {
	
	public static String fileName = "..."; // set file path of file to test hee
	public static int pageIndex = 1; // the page to be displayed
	
	public static void main(String[] args) {
		JFrame myFrame = new JFrame("PDFRenderer");
		PDFDisplay pdfDisplay = new PDFDisplay(fileName, pageIndex);		
		myFrame.add(pdfDisplay);
		myFrame.setSize(700, 1000);
		myFrame.setVisible(true);
	}
}
