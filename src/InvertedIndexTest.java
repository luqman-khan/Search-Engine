
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;

public class InvertedIndexTest {

	final Path fileTestPath = Paths.get("Z://code_repository//Search-Engine-master//src//TestCorpus");
	@Test
	public void posIndexShouldBeCreated() {
		// fail("Not yet implemented");
		InvertedIndex indexObject = new InvertedIndex(fileTestPath);
		indexObject.indexDirectory(fileTestPath);
		// System.out.println(indexObject.pos_dictionary);
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
