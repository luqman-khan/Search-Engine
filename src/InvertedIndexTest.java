/*This is a test class for the positional inverted index feature. 
*/


import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;

public class InvertedIndexTest {
	/**
	 * Initializing test variables.
	 */
	final Path fileTestPath = Paths.get(System.getProperty("user.dir")+"/src/TestCorpus");
	
	/**
	 * Positional inverted index test.
	 */
	@Test
	public void posIndexShouldBeCreated() {
		String ecpected_result = "{tell={3=2,}, save={0=0,, 1=0,}, start={4=4,}, i={2=0,5,}, say={2=4,}, anyth={3=4,}, do={4=2,}, am={2=1,6,}, whatev={2=2,}, miss={4=5,}, the={0=1,, 1=1,}, cheerlead={1=2,}, ever={3=1,}, everybodi={4=6,}, world={0=2,}, anybodi={3=3,}, if={4=0,}, you={2=3,, 4=1,3,}, dont={3=0,}}";
		InvertedIndex indexObject = new InvertedIndex(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		
		String actual_result = indexObject.pos_dictionary.toString();
		assertEquals(ecpected_result,actual_result);
		for (Iterator<String> i = indexObject.pos_dictionary.keySet().iterator(); i.hasNext();) {
			String word = i.next();
			System.out.println("Term :" + word);
			for (Iterator<Long> j = indexObject.pos_dictionary.get(word).keySet().iterator(); j.hasNext();) {
				long fileNum = j.next();
				String file = indexObject.files.get(fileNum);
				System.out.println("Document =" + file + "\t  " + "Position ="
						+ indexObject.pos_dictionary.get(word).get(fileNum));
			}
			System.out.println("\n");
		}
	}

}
