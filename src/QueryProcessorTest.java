import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTextPane;

import org.junit.Test;

public class QueryProcessorTest {
	final Path fileTestPath = Paths.get("Z://code_repository//Search-Engine-master//src//TestCorpus");
	final String andQuery = "whatever you"; // example query for AND, Doc2.txt
	final String orQuery = "whatever + you"; // example query for OR, Doc2.txt
	final String nearOpQuery = "you near/2 mssing"; // example query for near operator, Doc4.txt
	final String phraseQuery = "whatever you say"; // example query for phrase, Doc2.txt
	String [] input_terms;
	
	@Test
	public void andQueryTest() {
		System.out.println("Testing AND query!!");
		QueryProcessor indexObject = new QueryProcessor(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		Map<String, HashMap<Long, String>> testDictionary = indexObject.andQuery(andQuery);
		printTestResults(testDictionary,indexObject);
		
	}
	
	@Test
	public void orQueryTest() {
		System.out.println("Testing OR query!!");
		QueryProcessor indexObject = new QueryProcessor(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		Map<String, HashMap<Long, String>> testDictionary = indexObject.orQuery(orQuery);
		printTestResults(testDictionary,indexObject);
	}
	
	@Test
	public void nearQueryTest() {
		System.out.println("Testing NEAR operator!!");
		QueryProcessor indexObject = new QueryProcessor(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		Map<String, HashMap<Long, String>> testDictionary = indexObject.nearOperatorQuery(nearOpQuery);
		printTestResults(testDictionary,indexObject);
	}
	
	@Test
	public void phraseQueryTest() {
		System.out.println("Testing phrase query!!");
		QueryProcessor indexObject = new QueryProcessor(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		Map<String, HashMap<Long, String>> testDictionary = indexObject.phraseQuery(phraseQuery);
		printTestResults(testDictionary,indexObject);
	}
	
	public void printTestResults(Map<String, HashMap<Long, String>> testDictionary,QueryProcessor indexObject){
		for (Iterator<String> i = testDictionary.keySet().iterator(); i.hasNext();) {
			String word = i.next();
			System.out.println("Term :" + word);
			for (Iterator<Long> j = testDictionary.get(word).keySet().iterator(); j.hasNext();) {
				long fileNum = j.next();
				String file = indexObject.files.get(fileNum);
				System.out.println("Document =" + file + "\t  " + "Position ="
						+ testDictionary.get(word).get(fileNum));
			}
			System.out.println("\n");
		}
	}


}
