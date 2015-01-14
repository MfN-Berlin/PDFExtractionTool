package hms.extractor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;



public class FullProcessor {

	private static final String CERMINE_SOLR_XSLT = "./xml/MappingMapTosolrinput.xslt";
	private static final String PDF_ARTICLE_PATH = "./TestDocs/me.pdf" ; 
	private static final String XML_ARTICLE_PATH = "./xml/transformed/me.xml" ; 
			
	private static final String SOLR_XML_ARTICLE_PATH = "./xml/transformed/meTansformed.xml" ; 

	
	private static final String EXAMPLE_PDF_FILE_DIR = "C:/Resources/Literatur-Scans/"; // "./MutlilingDocs/"; // "C:/Users/localadmin/AppData/Local/Mendeley Ltd/Mendeley Desktop/ThesisBackgraound/"; // "./TestDocs/";
	private static final String PROCESSED_EXAMPLE_PDF_FILE_DIR = "C:/Resources/Literatur-Scans-XML/"; //"C:/Users/localadmin/AppData/Local/Mendeley Ltd/Mendeley Desktop/ThesisBackgroundXML/"; 
//	
//	private static final String EXAMPLE_PDF_FILE_DIR = "TestDocs/"; 
//	private static final String PROCESSED_EXAMPLE_PDF_FILE_DIR =  "TestDocsProcessed/";
	
	
	public static void main(String[] args) throws IOException {
		
		
		File dir = new File(EXAMPLE_PDF_FILE_DIR);
		
		List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		
		int totalNrFiles = files.size();
		int counter = 0;
		
		System.out.println(totalNrFiles + " has been found...");
		
		for (File file : files) {
		
			String pdfFilePath = file.getCanonicalPath();
			
			if(!pdfFilePath.endsWith(".pdf"))
				continue;
		
			String pdfFileName = file.getName();
			
		
			
			String xmlFileName = PROCESSED_EXAMPLE_PDF_FILE_DIR+pdfFileName.replace(".pdf", "_cermine.xml");
			
			String solrXmlFileName = PROCESSED_EXAMPLE_PDF_FILE_DIR+pdfFileName.replace(".pdf", "_solr.xml");
			
			counter++;
			
			if(new File(solrXmlFileName).exists()){
			
				System.out.println("Skipping: " + pdfFilePath);
				continue;
			}
			
			System.out.println("Handling: " + pdfFilePath +  " " + counter + " /" + totalNrFiles);
			
			PdfExtractor.extractCermineXML(pdfFilePath, xmlFileName);
			
			ArticleObjectCreator aoc = new ArticleObjectCreator(xmlFileName);
			
			aoc.createArticleFromCermineXML();
			
			aoc.writeArticleAsSolrXML(pdfFilePath,solrXmlFileName);
			
			new File(xmlFileName).delete();
		}
		
		
		
		
//		PdfExtractor.extractSolrXML(PDF_ARTICLE_PATH,CERMINE_SOLR_XSLT, XML_ARTICLE_PATH);
//		System.out.println(article);
	}
	
	
}
