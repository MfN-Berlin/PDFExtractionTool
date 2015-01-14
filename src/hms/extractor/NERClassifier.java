package hms.extractor;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NERClassifier {

	
	//Avoid multiple loading of the classifier
	static AbstractSequenceClassifier<CoreLabel> classifierEnglish;
	static AbstractSequenceClassifier<CoreLabel> classifierGerman;

	public static Map<String, Set<String>> extractNER(String example,
			String Language) {

		
		AbstractSequenceClassifier<CoreLabel> calssifier;
		Map<String, Set<String>> namedEntityMap = new HashMap<String, Set<String>>();

		String serializedClassifier = null;
	
		try {
			if (Language.equals("English")) {
				if(classifierEnglish == null){
					serializedClassifier = "classifiers/english.muc.7class.distsim.crf.ser.gz";
					classifierEnglish = CRFClassifier.getClassifier(serializedClassifier);
				}
				calssifier = classifierEnglish;
	
			} else if (Language.equals("German")) {
				if(classifierGerman == null){
					serializedClassifier = "classifiers/hgc_175m_600.crf.ser.gz";
					classifierGerman = CRFClassifier.getClassifier(serializedClassifier);
				}
				
				calssifier = classifierGerman;
	
			} else {
				return namedEntityMap;
			}

	
			List<Triple<String, Integer, Integer>> triples = calssifier.classifyToCharacterOffsets(example);

			for (Triple<String, Integer, Integer> trip : triples) {

				String clazz = trip.first;
				String ne = example.substring(trip.second(), trip.third);

				if (namedEntityMap.containsKey(clazz)) {

					namedEntityMap.get(clazz).add(ne);
				} else {
					Set<String> neList = new HashSet<String>();
					neList.add(ne);
					namedEntityMap.put(clazz, neList);
				}

			}

		} catch (ClassCastException | ClassNotFoundException | IOException e) {

			e.printStackTrace();
		}

		return namedEntityMap;
	}

	public static void main(String[] args) {
		System.out.println(extractNER("Good morning Albert from Germany","English"));
		System.out.println(extractNER("It rains very often in Germany. Google is great company","English"));
		System.out.println(extractNER("Everywhere in New York told Obama","English"));

		
		System.out.println(extractNER("Guten Tag  Albert aus Deutschland. Du lebst dort seit 2011.","German"));
		System.out.println(extractNER("An der Universität Passau ist Prof. Neumann aktiv","German"));
		System.out.println(extractNER("Seit 2010 arbeitet Martin an seiner Doktorarbeit in Moskau.","German"));
		System.out.println(extractNER("Kennst du Marline, die in Heidelberg wohnt.","German"));
	}
}
