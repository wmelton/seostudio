package seostudio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class Sitemap {
	public static String generateSitemap(Collection<Result> results) throws IOException {
		Namespace namespace = new Namespace("", "http://www.sitemaps.org/schemas/sitemap/0.9");
		Namespace xsi = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
 
		Element urlset = DocumentHelper.createElement(new QName("urlset", namespace));
		urlset.addAttribute(new QName("schemaLocation", xsi), "http://www.sitemaps.org/schemas/sitemap/0.9 http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd");
		Document document = DocumentHelper.createDocument(urlset);
 
 
		for (Result result : results) {
			if (!result.visited || !result.index)
				continue;
			
			Element url      = DocumentHelper.createElement(new QName("url", namespace));
			Element loc      = DocumentHelper.createElement(new QName("loc", namespace));
			Element priority = DocumentHelper.createElement(new QName("priority", namespace));
 
			loc.setText(result.url);
			if (result.depth == 0) {
				priority.setText("1.0");
			} else {
				priority.setText("0.8");
			}
 
			url.add(loc);
			url.add(priority);
			urlset.add(url);
		}
 
		StringWriter out = new StringWriter();
		new XMLWriter(out, OutputFormat.createCompactFormat()).write(document);
		return out.toString();
	}
	
	public static void writeToFile(String sitemap) throws IOException {
		File f = new File("sitemap.xml");
		f.createNewFile();
		Writer output = new BufferedWriter(new FileWriter(f));
		output.write(sitemap);
	    output.close();
	}
}
