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

public class PdfExtractor {

	
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
	public static void extractSolrXML(String docPath, String cermine2SolrXSLTFilePath, String xmlOutputPath){
		
		String tempCermineFile = "temp/tempCermine.xml" ;
		
		extractCermineXML(docPath, tempCermineFile);
		
		
    	Stylizer.trasform(cermine2SolrXSLTFilePath, tempCermineFile, xmlOutputPath);
    	
//    	new File(tempCermineFile).delete();
    	
    	
	}
	

}
