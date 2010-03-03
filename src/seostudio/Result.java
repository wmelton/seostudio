package seostudio;

public class Result {
	
	public String url;
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
	public String seoError;
	
	@Override
	public String toString() {
		return "Result [\n\tdescription=" + description + ", \n\terror=" + error
				+ ", \n\tfollow=" + follow + ", \n\th1=" + h1 + ", \n\tindex=" + index
				+ ", \n\tkeywords=" + keywords + ", \n\tlevel=" + depth + ", \n\ttitle="
				+ title + "]";
	}
	
	public void appendSeoError(String e) {
		if (seoError == null)
			seoError = new String();
		if (!seoError.isEmpty())
			seoError = seoError + " ";
		seoError = seoError + e;
	}

}

