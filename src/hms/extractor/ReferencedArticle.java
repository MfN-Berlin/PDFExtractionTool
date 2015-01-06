package hms.extractor;

import java.util.List;

public class ReferencedArticle {
	
	private List<String> authors ;
	private String title ;
	private String source ;
	private String publisherName ;
	private String volume;
	private String year;
	private String fpage;
	private String lpage;
	private String issue;
	private String articleAsString;
	
	public String getPublisherName() {
		return publisherName;
	}
	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}
	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}

	

	
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getFpage() {
		return fpage;
	}
	public void setFpage(String fpage) {
		this.fpage = fpage;
	}
	public String getLpage() {
		return lpage;
	}
	public void setLpage(String lpage) {
		this.lpage = lpage;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getArticleAsString() {
		return articleAsString;
	}
	public void setArticleAsString(String articleAsString) {
		this.articleAsString = articleAsString;
	}
	@Override
	public String toString() {
		
		String authorStr = authors.size()==0?"":authors.toString().replace("[", "").replace("]", "")+ ". ";
		String titleStr = title==null?"":title+ ". ";
		String sourceStr = source==null?"":source+ ". ";
		String fpageStr = fpage==null?"":": " +String.valueOf(fpage);
		String lpageStr = lpage==null?"":"-" +String.valueOf(lpage)+". " ;
		String yearStr = year==null?"": "(" +String.valueOf(year)+ ")";
		String issueStr = issue==null?"":String.valueOf(issue)+ ". ";
		String volumeStr = volume==null?"":String.valueOf(volume) + ".";
		
		
		String res = authorStr + titleStr  + sourceStr  + yearStr + fpageStr+  lpageStr + issueStr  + volumeStr ;  
		
		return res;
	}
	

}
