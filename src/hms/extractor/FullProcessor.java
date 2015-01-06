package hms.extractor;

import java.io.File;

public class FullProcessor {

	private static final String CERMINE_SOLR_XSLT = "./xml/MappingMapTosolrinput.xslt";
	private static final String PDF_ARTICLE_PATH = "./TestDocs/me.pdf" ; 
	private static final String XML_ARTICLE_PATH = "./xml/transformed/me.xml" ; 
			
	private static final String SOLR_XML_ARTICLE_PATH = "./xml/transformed/meTansformed.xml" ; 

	
	private static final String EXAMPLE_PDF_FILE_DIR = "C:\\Users\\localadmin\\Google Drive\\MfN\\SemanticDataIntgeration\\"; // "./TestDocs/";
	private static final String PROCESSED_EXAMPLE_PDF_FILE_DIR = "C:\\Users\\localadmin\\Google Drive\\MfN\\SemanticDataIntgerationProcessed\\"; 
	
	public static void main(String[] args) {
		
		String[] pdfFileNames = new File(EXAMPLE_PDF_FILE_DIR).list();
		
		for(String pdfFileName : pdfFileNames){
		
			System.out.println("Handling: " + pdfFileName);
			
			String xmlFileName = PROCESSED_EXAMPLE_PDF_FILE_DIR+pdfFileName.replace(".pdf", "_cermine.xml");
			
			String solrXmlFileName = PROCESSED_EXAMPLE_PDF_FILE_DIR+pdfFileName.replace(".pdf", "_solr.xml");

			if(new File(solrXmlFileName).exists()){
			
				System.out.println("Skipping: " + pdfFileName);
				continue;
			}
			
			PdfExtractor.extractCermineXML(EXAMPLE_PDF_FILE_DIR+pdfFileName, xmlFileName);
			
			ArticleObjectCreator aoc = new ArticleObjectCreator(xmlFileName);
			
			aoc.createArticleFromCermineXML();
			
			aoc.writeArticleAsSolrXML(solrXmlFileName);
		}
		
		
		
		
//		PdfExtractor.extractSolrXML(PDF_ARTICLE_PATH,CERMINE_SOLR_XSLT, XML_ARTICLE_PATH);
//		System.out.println(article);
	}
	
	
}
