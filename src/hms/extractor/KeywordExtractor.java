package hms.extractor;

import hms.languageidentification.util.CollectionsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sree.textbytes.jtopia.Configuration;
import com.sree.textbytes.jtopia.TermDocument;
import com.sree.textbytes.jtopia.TermsExtractor;

public class KeywordExtractor {
	

	/**
	 * Extract keywords from a given text
	 * @param text
	 * @param lang
	 * @param topN
	 * @return
	 */
	public static List<String> extractKeywords(String text, String lang, int topN) {
		 List<String> finalKeywords = new ArrayList<String>();

		 
		 
		if(text != null && text.length() >= 100){
	
			Configuration.setTaggerType("Stanford");// "default" for lexicon POS tagger and "openNLP" for openNLP POS     tagger
			
			switch (lang) {
			case "English":
				Configuration.setModelFileLocation("jtopia/model/stanford/wsj-0-18-bidirectional-distsim.tagger");
				break;
			case "French":
				Configuration.setModelFileLocation("jtopia/model/stanford/french.tagger");
				break;
			case "Spanish":
				Configuration.setModelFileLocation("jtopia/model/stanford/wsj-0-18-bidirectional-distsim.tagger");
				break;
			case "German":
				Configuration.setModelFileLocation("jtopia/model/stanford/german-fast.tagger");
				break;
			case "Arabic":
				Configuration.setModelFileLocation("jtopia/model/stanford/arabic.tagger");
				break;
			default:
				break;
			}
			
			Configuration.setSingleStrength(3);
			Configuration.setNoLimitStrength(2);
		
			TermsExtractor termExtractor = new TermsExtractor();
			TermDocument termDocument = new TermDocument();
			termDocument = termExtractor.extractTerms(text);
		
			int maxNrKeyowrds = Math.min(topN,termDocument.getFinalFilteredTerms().size());
	
			
			Map<String, Integer> termFrqMap = new HashMap<String, Integer>();
			 
			 
			 Map<String, ArrayList<Integer>> keywordFrqLenMap = termDocument.getFinalFilteredTerms();
			 
			 for(String keyword :keywordFrqLenMap.keySet()) {
				 
				 int keywordFrq = keywordFrqLenMap.get(keyword).get(0);
				 termFrqMap.put(keyword, keywordFrq);
				 
			 }
			 
			 Map<String, Integer> sortedTermFrqMap = CollectionsUtil.sortUsingComparator(termFrqMap, false);
			 
			
		
			
			 for(String keyword : sortedTermFrqMap.keySet()) {
	
				finalKeywords.add(keyword);
				
				if (finalKeywords.size() == maxNrKeyowrds)
				
					return finalKeywords;
				}
		}
		
		return finalKeywords;

	}

}
