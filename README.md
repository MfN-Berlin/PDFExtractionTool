#Structured Information Extraction from PDF Scientific Articles

The goal of this project is to extracted structured information from scientific articles in PDF format.
The project combines the functionality of several java libraries. In particular, the project uses the following libraries:

+ [Cermine](http://cermine.ceon.pl/index.html) PDF extraction library
+ [JTOPIA](https://github.com/srijiths/jtopia) keyword extraction library
+ Stanford Named Entity Recognition [(NER)](http://nlp.stanford.edu/software/CRF-NER.shtml) library

Given an article in PDF format, first, Cermine is used to extract structured information from the article in XML fomrat according to the [NLM](http://www.nlm.nih.gov/) standard. After that, the language of the article is identified. Next, based on the extracted contents of the document and its language, keyword extraction using JTOPIA is applied. Futhermore, Stanford NER is performed to extract named entity. Finally, the extracted information are provided in XML format which can be consumed directly by Solr for indexing.


## Example Usage

The following code explains how extract structured information stored in a given directory (e.g. /TestDocs). The resulting XML files are stored in the same directory.

```Java
import hms.extractor.FullProcessor;

public class Test {
	
	public static void main(String[] args) {
		FullProcessor fp = new FullProcessor("TestDocs/","TestDocs/");
		fp.startProcessing();
	}
	
}
```
