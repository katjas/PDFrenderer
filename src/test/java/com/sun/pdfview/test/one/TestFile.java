package com.sun.pdfview.test.one;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

public class TestFile //extends DrawExampleTest
{
	@Test
	public void test() throws Exception
	{
		File file = getResourceAsFile("com/sun/pdfview/test/bugs/1-1.pdf");
		File outputDir = new File("D:/Temp/output");
		(new DrawExampleTest(file, outputDir)).test();
	}

	private File getResourceAsFile(String resourcePath) throws Exception
	{
		URL url = getClass().getClassLoader().getResource(resourcePath);
		File result = new File(url.toURI());
		return result;
	}	
}
