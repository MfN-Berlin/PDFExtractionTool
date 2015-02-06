package hms.extractor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

/**
 * Main class that extracts structured information from a collection of PDF
 * article stored in given directory. The extracted information are written in
 * XML format which can be directly consumed by Solr for indexing purpose.
 * 
 * @author Hatem Mousselly-Sergieh
 *
 */

public class FullProcessor {

	private String articleDir;
	private String outputDir;

	public FullProcessor() {
		super();
	}

	public FullProcessor(String articleDir, String outputDir) {
		super();
		this.articleDir = articleDir;
		this.outputDir = outputDir;
	}

	public String getArticleDir() {
		return articleDir;
	}

	public void setArticleDir(String articleDir) {
		this.articleDir = articleDir;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDir) {
		this.outputDir = outputDir;
	}

	
	public void startProcessing() {

		File dir = new File(this.articleDir);

		List<File> files = (List<File>) FileUtils.listFiles(dir,TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);

		int totalNrFiles = files.size();

		int counter = 0;

		System.out.println(totalNrFiles + " has been found...");

		for (File file : files) {

			String pdfFilePath;
			try {
				pdfFilePath = file.getCanonicalPath();

				if (!pdfFilePath.endsWith(".pdf"))
					continue;

				String pdfFileName = file.getName();

				String xmlFileName = this.outputDir+ pdfFileName.replace(".pdf", "_cermine.xml");

				String solrXmlFileName = this.outputDir+ pdfFileName.replace(".pdf", "_solr.xml");

				counter++;

				//Ignore already processed documents
				if (new File(solrXmlFileName).exists()) {

					System.out.println("Skipping: " + pdfFilePath);
					continue;
				}

				System.out.println("Handling: " + pdfFilePath + " " + counter+ " /" + totalNrFiles);

				CerminePdfExtractor.extractCermineXML(pdfFilePath, xmlFileName);

				ArticleObjectCreator aoc = new ArticleObjectCreator(xmlFileName);

				aoc.createArticleFromCermineXML();

				aoc.writeArticleAsSolrXML(pdfFilePath, solrXmlFileName);

				new File(xmlFileName).delete();
			} catch (IOException e) {
	
				e.printStackTrace();
			}
		}

	}
	
	//Example usage
	public static void main(String[] args) {
		FullProcessor fp = new FullProcessor("TestDocs/","TestDocs/");
		fp.startProcessing();
	}


}
