/*
 * Copyright 2010 Seostudio team
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package seostudio;

import java.io.InputStream;
import java.io.InputStreamReader;
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
	private int visitedPages;
	
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
			visitedPages += 1;
			String url = r.url;
			TagNode tag = fetchUrl(r, url);
			if(tag == null) return;
			
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
					r.appendSeoError("No meta description");
				else if (r.description.length() < 80)
					r.appendSeoError("Meta description too short " + r.description.length()+" characters");
				else if (r.description.length() > 160)
					r.appendSeoError("Meta description too long. " + r.description.length()+" characters");
			}
			
			if (!r.seoErrors.isEmpty()) {
				seoErrors++;
//				System.out.println(r.url + " - " + r.seoError);
			}
			if(!r.follow) return;
			
			TagNode[] a = tag.getElementsByName("a", true);
			
			URI uri = URI.create(url);
			for (TagNode t : a) {
				String href = t.getAttributeByName("href");
				
				if(href!= null && href.endsWith("#"))
					href = href.substring(0, href.length()-1);
				
				if(href == null || href.isEmpty()) continue;
				
				String rel = t.getAttributeByName("rel");
				if("nofollow".equals(rel))
					continue;
				

				
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
			e.printStackTrace();
		}
	}
	
	public static TagNode fetchUrl(Result r, String url) throws Exception {
		HtmlCleaner cleaner = new HtmlCleaner();
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		if (conn.getResponseCode() != 200)
			throw new Exception("Response code was: "+conn.getResponseCode()+" while fetching URL: "+url);

		String contentType = conn.getHeaderField("content-type");
		r.contentType = contentType;
		if(!contentType.contains("text/html"))
			return null;
		
		String charset = "iso-8859-1";
		if(contentType != null) {
			int i = contentType.indexOf("charset=");
			if(i > 0) {
				charset = contentType.substring(i+"charset=".length()).trim();
			}
		}
		
		InputStream in = conn.getInputStream();
		InputStreamReader reader = new InputStreamReader(in, charset);
		TagNode tag = cleaner.clean(reader);
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
	
	public int getVisitedPages() {
		return visitedPages;
	}

}
