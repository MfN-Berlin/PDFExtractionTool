package hms.extractor;

import java.util.List;

public class Article {
	
	private String title ;
	private String abstrakt;
	private List<String> authors;
	private List<ReferencedArticle> references;
	private String contents;
	private String dateOfPublication;
	private String publisher;
	private List<String> predefinedKeywords;
	private List<String> extractedKeywords;
	private String language;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAbstrakt() {
		return abstrakt;
	}
	public void setAbstrakt(String abstrakt) {
		this.abstrakt = abstrakt;
	}
	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	public List<ReferencedArticle> getReferences() {
		return references;
	}
	public void setReferences(List<ReferencedArticle> references) {
		this.references = references;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	public String getDateOfPublication() {
		return dateOfPublication;
	}
	public void setDateOfPublication(String dateOfPublication) {
		this.dateOfPublication = dateOfPublication;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public List<String> getPredefinedKeywords() {
		return predefinedKeywords;
	}
	public void setPredefinedKeywords(List<String> predefinedKeywords) {
		this.predefinedKeywords = predefinedKeywords;
	}
	public List<String> getExtractedKeywords() {
		return extractedKeywords;
	}
	public void setExtractedKeywords(List<String> extractedKeywords) {
		this.extractedKeywords = extractedKeywords;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	
	@Override
	public String toString() {
		
		return this.title + "\n" + this.language + "\n" + this.abstrakt + "\n Keywords: \n" + extractedKeywords;
	}
	
}
