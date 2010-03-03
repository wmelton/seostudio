package seostudio;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

public class Crawler {

	static {
		System.setProperty("http.agent", "MasterBranch");
	}
	
	private Map<String, Result> results;
	private Map<Integer, Set<String>> levels;
	private String regexp;
	private int indexedPages;
	private int indexNofollowPages;
	private int connectionErrors;
	private int seoErrors;
	
	public Crawler(String url, String regexp) {
		results = new HashMap<String, Result>();
		levels  = new HashMap<Integer, Set<String>>();
		indexedPages = 0;
		indexNofollowPages = 0;
		connectionErrors = 0;
		found(url, 0);
		this.regexp = regexp;
	}
	
	private void found(String url, int level) {
		Result r = results.get(url);
		if(r != null) {
			r.links++;
			return;
		}
		r = new Result();
		r.url = url;
		r.depth = level;
		results.put(url, r);
		
		Set<String> urls = levels.get(level);
		if(urls == null) {
			urls = new HashSet<String>();
			levels.put(level, urls);
		}
		urls.add(url);
	}

	public void browse(int depth) {
		Set<String> urls = levels.get(depth);
		if(urls == null) return;
		
		for (String url : urls) {
			Result r = results.get(url);
			browse(r);
			r.visited = true;
			results.put(url, r);
		}
		
	}

	public void browse(Result r) {
		try {
			String url = r.url;
			TagNode tag = fetchUrl(url);
			
			TagNode title = tag.findElementByName("title", true);
			if(title != null)
				r.title = title.getText().toString();
			
			TagNode h1 = tag.findElementByName("h1", true);
			if(h1 != null)
				r.h1 = title.getText().toString();
			
			// index and follow by default are true
			r.index = true;
			r.follow = true;
			TagNode[] meta = tag.getElementsByName("meta", true);
			for (TagNode t : meta) {
				if("robots".equals(t.getAttributeByName("name"))) {
					String s = t.getAttributeByName("content");
					if(s != null) {
						if(s.contains("noindex"))
							r.index = false;
						if(s.contains("nofollow"))
							r.follow = false;
					}
				} else if("description".equals(t.getAttributeByName("name"))) {
					String s = t.getAttributeByName("content");
					r.description = s;
				} else if("keywords".equals(t.getAttributeByName("name"))) {
					String s = t.getAttributeByName("content");
					r.keywords = s;
				}
			}
			
			if (r.index)
				indexedPages += 1;
			
			if (r.index && !r.follow)
				indexNofollowPages += 1;
			
			if (r.title == null || r.title.isEmpty())
				r.appendSeoError("Title must be setted.");
			
			if (r.index) {
				if (r.description == null || r.description.isEmpty())
					r.appendSeoError("Description should be setted");
				else if (r.description.length() > 160)
					r.appendSeoError("Description is too long, description length = " + r.description.length());
			}
			
			if (r.seoError != null && !r.seoError.isEmpty())
				seoErrors += 1;
				
			if(!r.follow) return;
			
			TagNode[] a = tag.getElementsByName("a", true);

			URI uri = URI.create(url);
			for (TagNode t : a) {
				String href = t.getAttributeByName("href");
				if(href == null) continue;

				if(href.endsWith("#"))
					href = href.substring(0, href.length()-1);
				try {
					URI found = URI.create(href);
					if(!found.isAbsolute())
						href = uri.resolve(href).toString();
				} catch(Exception e) {
					System.err.println(e.getMessage());
				}
				href = href.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"");
				href = URLDecoder.decode(href, "UTF-8");
				
				if(href.matches(regexp)) {
					found(href, r.depth+1);
				}
			}
		} catch(Exception e) {
			r.error = "" + e.getMessage();
			connectionErrors += 1;
		}
	}
	
	public static TagNode fetchUrl(String url) throws Exception {
		HtmlCleaner cleaner = new HtmlCleaner();
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		if (conn.getResponseCode() != 200)
			throw new Exception("Response code was: "+conn.getResponseCode()+" while fetching URL: "+url);

		InputStream in = conn.getInputStream();
		TagNode tag = cleaner.clean(in);
		return tag;
	}
	
	public Map<String, Result> getResults() {
		return Collections.unmodifiableMap(results);
	}
	
	public int getIndexedPages() {
		return indexedPages;
	}
	
	public int getIndexedNoFollowPages() {
		return indexNofollowPages;
	}
	
	public int getConnectionErrors() {
		return connectionErrors;
	}
	
	public int getSeoErrors() {
		return seoErrors;
	}

	public static void main(String[] args) throws Exception {
		Crawler c = new Crawler("http://localhost:9000/", "http://localhost:9000(.*?)");
		c.browse(0);
		c.browse(1);
		
		for (Map.Entry<String, Result> entry : c.results.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
			System.out.println();
		}
	}

}
