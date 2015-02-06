package hms.extractor;

import hms.languageidentification.TextLanguageIdentifier;
import hms.languageidentification.util.CollectionsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

import org.apache.commons.io.FileUtils;
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
	
	/**
	 * Initialize an XML DOM Object.
	 */
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

	/**
	 * To create an Article object, structured information extracted using Cermine library are read first.
	 * @param xmlFilePath The path of Cermine XML file.
	 */
	public ArticleObjectCreator(String xmlFilePath) {
		this.xmlFilePath = xmlFilePath;
		init();
	}

	
	/**
	 * Based on the metadata extracted using Cermine, an Article object is created.
	 * The metadata are furthered enriched with information:
	 * 1- about the language in which the article was written
	 * 2- keywords extracted from the article contents and based on the identified language
	 * 3- named entities extracted from the article contents and based on the identified language 
	 * @return The final Article object
	 */
	public Article createArticleFromCermineXML(){
		
		this.article = new Article();
			
		article.setTitle(extractCermineTitle());
		article.setAbstrakt(extractCermineAbstact());
		article.setAuthors(extractCerminAuthors());
		article.setContents(extractCermineContents());
		article.setReferences(extractCermineReferences());
	
		//Identify the language of the article
		TextLanguageIdentifier tli = new TextLanguageIdentifier();
		String lang = "eng"; //default language
		if(article.getAbstrakt().length() > 300){
			lang = tli.identifyLanguage(article.getAbstrakt());
		}
		else if(article.getContents().length() > 800){ 
			lang = tli.identifyLanguage(article.getContents().substring(0, 800));
		}

		//TODO just for the scanned literature case
		Set<String> acceptedLangSet = new HashSet<String>();
		
		acceptedLangSet.add("English");
		acceptedLangSet.add("German");
		acceptedLangSet.add("French");
		
		if(!acceptedLangSet.contains(lang) ){
			
			lang = "English";

		}
		
		article.setLanguage(lang);
		

		if(article.getContents()!=null){
			//Extract Keywords
			List<String> keywordList = KeywordExtractor.extractKeywords(article.getContents(), article.getLanguage(), 15);
			article.setExtractedKeywords(keywordList);
			
			//Extract NamedEntities
			Map<String, Set<String>> namedEntities = NERClassifier.extractNER(article.getContents(),article.getLanguage());
			article.setNamedEntities(namedEntities);
			
		}
		
		
		return this.article;
	}
	
	
	
	//**** Extract metadata from Cermine XML. Cermine extract the metadata of PDF documents and output them in XML  
	//     format according to the NLM standard (http://dtd.nlm.nih.gov/) ********************//

	
	/**
	 * Extract article title using XPath from NLM XML 
	 * @return Title of the article
	 */
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
	
	/**
	 * Extract article abstract using XPath from NLM XML 
	 * @return Abstract of the article
	 */
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
	
	/**
	 * Extract author's list using XPath from NLM XML 
	 * @return List of article's authors
	 */
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
	
	
	/**
	 * Extract the complete contents of the article form the Cermine XML file
	 * @return Article contents
	 */
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
	 * Extract the list of referenced articles using XPath from NLM XML 
	 * @return List of referenced articles
	 */
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
	
	/**
	 * Extract detailed information about the cited articles from the Cermine XML.
	 * The methods expect a MixedCitation node as input and creates a ReferencedArticle object which contains
	 * information about the authors of the cited article, the year of publication, the volume,...
	 * @param citationNode MixedCiation XML Node
	 * @return ReferencedArticle object
	 */
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

	/**
	 * Extract authors list for a cited article.
	 * @param citationNode The mixed citation node
	 * @return Author's list
	 * @throws XPathExpressionException
	 */
	private List<String> extractAuthorName(Node citationNode)	throws XPathExpressionException {
		
		List<String> authorList = new ArrayList<String>();
		NodeList refNodeList = (NodeList) xPath.compile("./string-name").evaluate(citationNode, XPathConstants.NODESET);
		for (int i = 0; i < refNodeList.getLength(); i++) {
			String authorName = StringUtils.normalizeSpace(refNodeList.item(i).getTextContent());
			authorList.add(authorName);
			
		}

		return authorList;
	}
	

	
	
	/**
	 * Generate an XML representation for an article object. The XML conforms to Solr indexing requirements.
	 * For each metadata entry an XML elements is generated according to the following syntax:
	 * <field name="filedName">value</field> for example for the title on an article the following XML element is generated:
	 * <field name="article-title">title of the article</field>
	 * For multi-valued metadata like authors multiple XML elements are generated, e.g.:
	 * <field name="author">first author</field>
	 * <field name="author">second author</field>
	 * @param orgPDFFilePath The location of the original PDF document from which the metadata are extracted
	 * @param targetFilePath The location of the target XML file  
	 */
	public void writeArticleAsSolrXML(String orgPDFFilePath, String targetFilePath){
		
		try {
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("add");
			doc.appendChild(rootElement);
			
			Element docElement = doc.createElement("doc");
			rootElement.appendChild(docElement);
			
			Element idField = doc.createElement("field");
			idField.setAttribute("name", "id");
			idField.appendChild(doc.createTextNode(orgPDFFilePath));
			docElement.appendChild(idField);
			
			
			Element fileLocField = doc.createElement("field");
			fileLocField.setAttribute("name", "source-file");
			fileLocField.appendChild(doc.createTextNode(orgPDFFilePath));
			docElement.appendChild(fileLocField);
			
			
			Element xmlLocField = doc.createElement("field");
			xmlLocField.setAttribute("name", "xml-file");
			xmlLocField.appendChild(doc.createTextNode(this.xmlFilePath));
			docElement.appendChild(xmlLocField);
			
			
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
			String title = this.article.getTitle();
//			TODO Just for the literature scan
			if(title == null || title.equals("")){
				title = orgPDFFilePath.substring(orgPDFFilePath.lastIndexOf("_")+1, orgPDFFilePath.lastIndexOf("."));
				title = StringUtils.normalizeSpace(title);
			}
			articleTitleField.appendChild(doc.createTextNode(title));
			docElement.appendChild(articleTitleField);
			
			
			Element abstractTitleField = doc.createElement("field");
			abstractTitleField.setAttribute("name", "abstract");
			abstractTitleField.appendChild(doc.createTextNode(this.article.getAbstrakt()));
			docElement.appendChild(abstractTitleField);
			
		
			//TODO Just for the literature scan
			if(this.article.getAuthors() == null || this.article.getAuthors().size() == 0){
				String fileName = orgPDFFilePath.substring(orgPDFFilePath.lastIndexOf("\\")+1, orgPDFFilePath.lastIndexOf("."));
				String author = fileName.substring(0, fileName.indexOf("_"));
				author = StringUtils.normalizeSpace(author).replace("&", ", ");
				Element authorField = doc.createElement("field");
				authorField.setAttribute("name", "author");
				authorField.appendChild(doc.createTextNode(author));
				docElement.appendChild(authorField);
			}
			
			else{
				for(String author : this.article.getAuthors()){
					Element authorField = doc.createElement("field");
					authorField.setAttribute("name", "author");
					authorField.appendChild(doc.createTextNode(author));
					docElement.appendChild(authorField);
					
				}
			}
			
			for(String namedEntityType : this.article.getNamedEntities().keySet()){
				
				Set<String> namedEntityList = this.article.getNamedEntities().get(namedEntityType);
				
				for(String namedEntity : namedEntityList){
					
					Element namedEntityField = doc.createElement("field");
					if(namedEntityType.equals("I-PER"))
						namedEntityType = NERTypes.PERSON;
					else if(namedEntityType.equals("I-LOC")){
						namedEntityType = NERTypes.LOCATION;
					}
					else if(namedEntityType.equals("I-ORG")){
						namedEntityType = NERTypes.ORGANIZATION;

					}
					else if(namedEntityType.equals("I-MISC")){
						continue;
					}
					namedEntityField.setAttribute("name", namedEntityType);
					namedEntityField.appendChild(doc.createTextNode(namedEntity));
					docElement.appendChild(namedEntityField);
				}
			
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
		
		//Example
		//1-First extract metadata from a PDF article using Cermine extractor
		String inputPDF = "TestDocs/paper7_cameraready.pdf";
		String cermineXML = "TestDocs/paper7_cameraready.xml";
		CerminePdfExtractor.extractCermineXML(inputPDF, cermineXML);
		
		ArticleObjectCreator aoc = new ArticleObjectCreator(cermineXML);
		aoc.createArticleFromCermineXML();
		aoc.writeArticleAsSolrXML(inputPDF, "TestDocs/paper7_cameraready_solr.xml");
		
		

	}
}
