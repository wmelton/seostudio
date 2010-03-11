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

import java.util.ArrayList;
import java.util.List;

public class Result {
	
	public String url;
	public String contentType;
	public int depth;
	public String error;
	public String title;
	public String description;
	public String keywords;
	public String h1;
	public boolean index;
	public boolean follow;
	public boolean visited;
	public int links;
	public List<String> seoErrors = new ArrayList<String>();
	
	@Override
	public String toString() {
		return "Result [\n\tdescription=" + description + ", \n\terror=" + error
				+ ", \n\tfollow=" + follow + ", \n\th1=" + h1 + ", \n\tindex=" + index
				+ ", \n\tkeywords=" + keywords + ", \n\tlevel=" + depth + ", \n\ttitle="
				+ title + "]";
	}
	
	public void appendSeoError(String e) {
		seoErrors.add(e);
	}

}

