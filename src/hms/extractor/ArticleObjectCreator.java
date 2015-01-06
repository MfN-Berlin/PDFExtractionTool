package hms.extractor;

import hms.languageidentification.TextLanguageIdentifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sree.textbytes.jtopia.Configuration;
import com.sree.textbytes.jtopia.TermDocument;
import com.sree.textbytes.jtopia.TermsExtractor;

/**
 * 
 * @author Hatem Mousselly-Sergieh
 * This class create an Article object from an XML file according to Solr schema 
 *
 */
public class ArticleObjectCreator {

	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private Document document ;
	private String xmlFilePath;
	private XPath xPath;
	private Article article ;
	
	private void init(){
		
		 try {
			factory =  DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			document = builder.parse(new File(this.xmlFilePath));
			xPath =  XPathFactory.newInstance().newXPath();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public ArticleObjectCreator(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
		init();
	}
	
	public Article createArticleFromSolrXML(){
		
		this.article = new Article();
		
		String articleTitle = extractSolrTitle();
		List<String> authorList = extractSolrAuthors();
		List<String> refList = extractSolrReferences();
		String content = extractSolrContents();
		String abstrakt = extractSolrAbstract();
			
		article.setTitle(articleTitle);
		article.setAbstrakt(abstrakt);
		article.setAuthors(authorList);
		article.setContents(content);
//		article.setReferences(refList);
	
		//Identify the language of the article
		
		TextLanguageIdentifier tli = new TextLanguageIdentifier();
		String lang = tli.identifyLanguage(article.getAbstrakt());
		article.setLanguage(lang);
		
		//Extract Keywords
		List<String> keywordList = extractKeywords(article.getContents(), article.getLanguage(), 15);
		article.setExtractedKeywords(keywordList);
		
		return this.article;
	}

	
	public Article createArticleFromCermineXML(){
		
		this.article = new Article();
			
		article.setTitle(extractCermineTitle());
		article.setAbstrakt(extractCermineAbstact());
		article.setAuthors(extractCerminAuthors());
		article.setContents(extractCermineContents());
		article.setReferences(extractCermineReferences());
	
		//Identify the language of the article
		TextLanguageIdentifier tli = new TextLanguageIdentifier();
		String lang = tli.identifyLanguage(article.getAbstrakt());
		article.setLanguage(lang);
		
		//Extract Keywords
		List<String> keywordList = extractKeywords(article.getContents(), article.getLanguage(), 15);
		article.setExtractedKeywords(keywordList);
		
		return this.article;
	}
	
	
	public List<String> extractKeywords(String text, String lang, int topN) {
		
		if(lang.equals("eng")){
			Configuration.setTaggerType("openNLP");// "default" for lexicon POS tagger and "openNLP" for openNLP POS     tagger
			Configuration.setModelFileLocation("jtopia/model/openNLP/en-pos-maxent.bin");

		}
			
		Configuration.setSingleStrength(3);
		Configuration.setNoLimitStrength(2);
		TermsExtractor termExtractor = new TermsExtractor();
		TermDocument termDocument = new TermDocument();
		termDocument = termExtractor.extractTerms(text);
		int maxNrKeyowrds = Math.min(topN,termDocument.getFinalFilteredTerms().size());

		List<String> keywords = new ArrayList<String>();
	
		for(String keyword : termDocument.getFinalFilteredTerms().keySet()) {

			keywords.add(keyword);
			if (keywords.size() == maxNrKeyowrds)
					 return keywords;
			}
		
		return null;

	}
	
	
	
	
	//XML Metadata extracted from solr file
	private String extractSolrAbstract() {
		String abstrakt = null;
		String expression = "/add/doc/field[@name='abstract']";
		try {
			abstrakt = xPath.compile(expression).evaluate(document);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return abstrakt;
	}

	private String extractSolrContents() {
		String contents = null;
		String expression = "/add/doc/field[@name='content']";
		try {
			contents = xPath.compile(expression).evaluate(document);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contents;
	}

	private List<String> extractSolrReferences() {
		
		List<String> refList = new ArrayList<String>();
		String expression = "/add/doc/field[@name='reference']";
		//read a nodelist using xpath
		try {
			NodeList refNodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			for (int i = 0; i < refNodeList.getLength(); i++) {
				
				String ref = refNodeList.item(i).getTextContent();
				refList.add(ref);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return refList;
	}

	private List<String> extractSolrAuthors() {
		List<String> authorList = new ArrayList<String>();
		String expression = "/add/doc/field[@name='author']";
		//read a nodelist using xpath
		try {
			NodeList refNodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			for (int i = 0; i < refNodeList.getLength(); i++) {
				
				String ref = refNodeList.item(i).getTextContent();
				authorList.add(ref);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return authorList;
	}

	private String extractSolrTitle() {
		String articleTitle = null;
		String expression = "/add/doc/field[@name='article-title']";
		try {
			articleTitle = xPath.compile(expression).evaluate(document);
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articleTitle;
	}
	
	
	//Extract Metadata from Cermine XML

	public String extractCermineTitle(){
		
		String articleTitle = null;
		String expression = "/article/front/article-meta/title-group/article-title";
		try {
			articleTitle = xPath.compile(expression).evaluate(document);
			if(articleTitle !=null){
				articleTitle = StringUtils.normalizeSpace(articleTitle);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return articleTitle;
		
	}
	
	public String extractCermineAbstact(){
		
		String abstrakt = null;
		String expression = "/article/front/article-meta/abstract/p";
		try {
			abstrakt = xPath.compile(expression).evaluate(document);
			if(abstrakt != null){
				abstrakt = StringUtils.normalizeSpace(abstrakt);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return abstrakt;
		
	}
	public List<String> extractCerminAuthors() {
		
		List<String> authorList = new ArrayList<String>();
		String expression = "/article/front/article-meta/contrib-group/contrib/string-name";
		//read a nodelist using xpath
		try {
			NodeList refNodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			for (int i = 0; i < refNodeList.getLength(); i++) {
				
				String ref = refNodeList.item(i).getTextContent();
				authorList.add(ref);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return authorList;
	}
	
	public List<ReferencedArticle> extractCermineReferences() {
		
		List<ReferencedArticle> refList = new ArrayList<ReferencedArticle>();
		
		String expression = "/article/back/ref-list/ref/mixed-citation";
		
		//read a nodelist using xpath
		
		try {
			NodeList refNodeList = (NodeList) xPath.compile(expression).evaluate(document, XPathConstants.NODESET);
			
			for (int i = 0; i < refNodeList.getLength(); i++) {
				
				ReferencedArticle refArticle = extractCermineReference(refNodeList.item(i));
				
				
				String ref = refNodeList.item(i).getTextContent();
				if(ref!=null){
					refArticle.setArticleAsString(StringUtils.normalizeSpace(ref));	
				}
				
				refList.add(refArticle);
				
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return refList;
	}
	
	
	public ReferencedArticle extractCermineReference(Node citationNode) {
		
		ReferencedArticle refArticle = new ReferencedArticle();
		
		try {
			refArticle.setAuthors(extractAuthorName(citationNode));
			
			String title =  xPath.compile("./article-title").evaluate(citationNode);
			if(title !=null && !title.equals(""))
				refArticle.setTitle(StringUtils.normalizeSpace(title));
			
			String source =  xPath.compile("./source").evaluate(citationNode);
			if(source !=null && !source.equals(""))
				refArticle.setSource(StringUtils.normalizeSpace(source));
			
			String publisherName=  xPath.compile("./publisher-name").evaluate(citationNode);
			if(publisherName !=null && !publisherName.equals(""))
				refArticle.setPublisherName(StringUtils.normalizeSpace(publisherName));
			
			String volume =  xPath.compile("./volume").evaluate(citationNode);
			if(volume !=null && !volume.equals(""))
				refArticle.setVolume(volume);
			
			String year =  xPath.compile("./year").evaluate(citationNode);
			if(year !=null && !year.equals(""))
				refArticle.setYear(year);
			
			String lpage =  xPath.compile("./lpage").evaluate(citationNode);
			if(lpage !=null && !lpage.equals(""))
				refArticle.setLpage(lpage);
			
			String fpage =  xPath.compile("./fpage").evaluate(citationNode);
			if(fpage !=null && !fpage.equals(""))
				refArticle.setFpage(fpage);
			
			String issue =  xPath.compile("./issue").evaluate(citationNode);
			if(issue !=null && !issue.equals(""))
				refArticle.setIssue(issue);
			
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return refArticle;
				
	}

	//Extract author name from mixed-citation node
	private List<String> extractAuthorName(Node citationNode)	throws XPathExpressionException {
		
		List<String> authorList = new ArrayList<String>();
		NodeList refNodeList = (NodeList) xPath.compile("./string-name").evaluate(citationNode, XPathConstants.NODESET);
		for (int i = 0; i < refNodeList.getLength(); i++) {
			String authorName = StringUtils.normalizeSpace(refNodeList.item(i).getTextContent());
			authorList.add(authorName);
			
		}

		return authorList;
	}
	
	
	
	public String extractCermineContents() {
		String contents = null;
		String expression = "/article/body";
		try {
			contents = xPath.compile(expression).evaluate(document);
			if(contents !=null){
				contents = StringUtils.normalizeSpace(contents);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contents;
	}
	
	
	/**
	 * Generate SOLR conform XML file from an article object
	 */
	public void writeArticleAsSolrXML(String targetFilePath){
		
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("add");
			doc.appendChild(rootElement);
			
			Element docElement = doc.createElement("doc");
			rootElement.appendChild(docElement);
			
			
			Element fileLocField = doc.createElement("field");
			fileLocField.setAttribute("name", "source-file");
			fileLocField.appendChild(doc.createTextNode(this.xmlFilePath));
			docElement.appendChild(fileLocField);
			
			
			Element langField = doc.createElement("field");
			langField.setAttribute("name", "language");
			langField.appendChild(doc.createTextNode(this.article.getLanguage()));
			docElement.appendChild(langField);
			
			
			Element keywordsField = doc.createElement("field");
			keywordsField.setAttribute("name", "auto-keywords");
			keywordsField.appendChild(doc.createTextNode(this.article.getExtractedKeywords().toString().replace("[", "").replace("]", "")));
			docElement.appendChild(keywordsField);
			
			
			Element articleTitleField = doc.createElement("field");
			articleTitleField.setAttribute("name", "article-title");
			articleTitleField.appendChild(doc.createTextNode(this.article.getTitle()));
			docElement.appendChild(articleTitleField);
			
			
			Element abstractTitleField = doc.createElement("field");
			abstractTitleField.setAttribute("name", "abstract");
			abstractTitleField.appendChild(doc.createTextNode(this.article.getAbstrakt()));
			docElement.appendChild(abstractTitleField);
			
		
			
			
			for(String author : this.article.getAuthors()){
				Element authorField = doc.createElement("field");
				authorField.setAttribute("name", "author");
				authorField.appendChild(doc.createTextNode(author));
				docElement.appendChild(authorField);
				
			}
			
			Element contentField = doc.createElement("field");
			contentField.setAttribute("name", "content");
			contentField.appendChild(doc.createTextNode(this.article.getContents()));
			docElement.appendChild(contentField);
			
			for(ReferencedArticle refArcticle : this.article.getReferences()){
				Element refField = doc.createElement("field");
				refField.setAttribute("name", "reference");
				refField.appendChild(doc.createTextNode(refArcticle.getArticleAsString()));
				docElement.appendChild(refField);
				
			}
			
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			
			 FileOutputStream fos = new FileOutputStream(new File(targetFilePath));
	         StreamResult result = new StreamResult(fos);
	     
			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);
	 
			transformer.transform(source, result);
	 
			System.out.println("File saved!");
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	
	public static void main(String[] args) {
		
//		ArticleObjectCreator aoc = new ArticleObjectCreator("./xml/transformed/Liris-5791.xml");
//		
//		String title = aoc.extractSolrTitle();
//		String abstakt = aoc.extractSolrAbstract();
//		String content = aoc.extractSolrContents();
//		System.out.println(title);
//		System.out.println(abstakt);
//		System.out.println(content);
//		System.out.println(aoc.extractSolrReferences());
//		System.out.println(aoc.extractSolrAuthors());
//		System.out.println(aoc.extractKeywords(content, "English", 10));
		
		
		ArticleObjectCreator aoc3 = new ArticleObjectCreator("./temp/tempCermine.xml");
//		System.out.println(aoc3.extractCermineTitle());
		System.out.println(aoc3.extractCermineAbstact());
//		System.out.println(aoc3.extractCerminAuthors());
		aoc3.extractCermineReferences();
//		System.out.println(aoc3.extractCermineReferences());
//		System.out.println(aoc3.extractCermineContents());
		
		
		
				
		
	}
}
