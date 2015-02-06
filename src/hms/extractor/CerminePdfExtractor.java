package hms.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import pl.edu.icm.cermine.PdfNLMContentExtractor;
import pl.edu.icm.cermine.exception.AnalysisException;

public class CerminePdfExtractor {

	
	/**
	 * Extract the contents of an article in PDF form. The extracted data are structured according 
	 * to the Cermine XML schema
	 * @param pdfFilePath Path to the pdf file
	 * @param xmlOutputPath Path to the extracted xml file
	 */
	public static void extractCermineXML(String pdfFilePath, String xmlOutputPath){
		try {
			
			PdfNLMContentExtractor	extractor = new PdfNLMContentExtractor();
			extractor.setExtractText(true);
			extractor.setExtractMetadata(true);
			InputStream inputStream = new FileInputStream(pdfFilePath);
			Element result = extractor.extractContent(inputStream);

			XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
			outputter.output(result, new FileOutputStream(new File(xmlOutputPath)));
		} catch (AnalysisException | IOException e) {
			e.printStackTrace();
		}
		

	}
	
	/**
	 * Extract the contents of an article in PDF form. The extracted data are structured according 
	 * to the Cermine XML schema and then converted using XSLT into Solr XML format
	 * @param pdfFilePath The path of the input PDF document
	 * @param cermine2SolrXSLTFilePath Path to the XSLT transformation file
	 * @param xmlOutputPath Output in Solr XML format
	 */
	public static void extractSolrXML(String pdfFilePath, String cermine2SolrXSLTFilePath, String xmlOutputPath){
		
		String tempCermineFile = "temp/tempCermine.xml" ;
		
		extractCermineXML(pdfFilePath, tempCermineFile);
		
		
    	Stylizer.trasform(cermine2SolrXSLTFilePath, tempCermineFile, xmlOutputPath);
    	
//    	new File(tempCermineFile).delete();
    	
    	
	}
	

}
