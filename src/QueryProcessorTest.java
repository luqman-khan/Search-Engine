//
///*This is a test class for AND, OR, queries, and NEAR operator and phrase query features. 
//*/
//
//import static org.junit.Assert.*;
//
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.HashMap;
//import java.util.Iterator;
//
//import javax.swing.JTextArea;
//
////import org.junit.Ignore;
//import org.junit.Test;
//
//public class QueryProcessorTest {
//	/**
//	 * Initializing test variables.
//	 */
//	final Path file_test_path = Paths.get(System.getProperty("user.dir")+"\\src\\TestCorpus");
//	final String and_search_test = "whatever you";
//	final String or_search_test = "whatever + you";
//	final String near_search_test = "you near/2 missing";
//	final String phrase_search_test = "\"Save the world\"";
//	String[] input_terms;
//
//	/**
//	 * Test AND.
//	 */
////	 @Ignore
//	@Test
//	public void andQueryTest() {
////		System.out.println("Testing AND query!!");
////		QueryProcessor indexObject = new QueryProcessor(file_test_path);
////		indexObject.inverted_index.indexDirectory();
////		HashMap<Long, Long> testDictionary = indexObject.andQuery(and_search_test);
////		printTestResults(testDictionary, indexObject);
//
//	}
//
//	/**
//	 * Test OR.
//	 */
////	@Ignore
//	@Test
//	public void orQueryTest() {
////		System.out.println("Testing OR query!!");
////		QueryProcessor indexObject = new QueryProcessor(file_test_path);
////		indexObject.inverted_index.indexDirectory();
////		HashMap<Long, Long> testDictionary = indexObject.orQuery(or_search_test);
////		printTestResults(testDictionary, indexObject);
//
//	}
//
//	/**
//	 * Test NEAR/k.
//	 */
////	@Ignore
//	@Test
//	public void nearQueryTest() {
//		System.out.println("Testing NEAR operator!!");
//		QueryProcessor indexObject = new QueryProcessor(file_test_path);
//		JTextArea result_txt = new JTextArea();
//		indexObject.inverted_index.indexDirectory();
//		indexObject.processQuery(near_search_test, result_txt);
//		System.out.println(result_txt.getText());
//		assertEquals("Doc4.txt\n", result_txt.getText());
//	}
//
//	/**
//	 * Test phrase.
//	 */
////	@Ignore
//	@Test
//	public void phraseQueryTest() {
//		System.out.println("Testing phrase query!!");
//		QueryProcessor indexObject = new QueryProcessor(file_test_path);
//		JTextArea result_txt = new JTextArea();
//		indexObject.inverted_index.indexDirectory();
//		indexObject.processQuery(phrase_search_test, result_txt);
//		System.out.println(result_txt.getText());
//		assertEquals("Doc0.txt\n", result_txt.getText());
//	}
//
//	/**
//	 * Printing results.
//	 */
//	public void printTestResults(HashMap<Long, Long> testDictionary, QueryProcessor indexObject) {
//		for (Iterator<Long> i = testDictionary.keySet().iterator(); i.hasNext();) {
//			long fileNum = i.next();
//			String file = indexObject.inverted_index.files.get(fileNum);
//			System.out.println("Document Name = " + file);
//		}
//		System.out.println("\n");
//	}
//}
