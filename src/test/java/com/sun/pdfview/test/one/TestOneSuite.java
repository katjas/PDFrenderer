package com.sun.pdfview.test.one;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestOneSuite
{
	public static Test suite() throws Exception
	{
		TestSuite suite = new TestSuite("com/sun/pdfview/test/one");
		File outputDir = new File("d:/temp/output");
		List<File> files = getPdfFilesFromClasspath("com/sun/pdfview/test/one");
		for (File f : files)
		{
			suite.addTest(new DrawExampleTest(f, outputDir));
		}
		return suite;
	}
	
	private static List<File> getPdfFilesFromClasspath(String resourcePath) throws Exception
	{
		List<File> result = new ArrayList<File>(); 
		Enumeration<URL> roots = TestOneSuite.class.getClassLoader().getResources(resourcePath);
		while (roots.hasMoreElements())
		{
			URL url = roots.nextElement();
			File dir = new File(url.toURI());
			if (!dir.isDirectory())
			{
				System.out.println("" + url + " is not a directory");
				continue;
			}
			File files[] = dir.listFiles(
					new FileFilter()
					{
						@Override
						public boolean accept(File file)
						{
							return file.isFile() && file.getName().toLowerCase().endsWith(".pdf");
						}
						
					}
			);
			result.addAll(Arrays.asList(files));
		}
		return Collections.unmodifiableList(result);
	}
}
