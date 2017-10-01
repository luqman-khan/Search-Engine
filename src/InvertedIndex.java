import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
//import java.util.Arrays;
import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JTextPane;

public class InvertedIndex {

	private Map<String, HashMap<Long, String>> pos_dictionary;
	private HashMap<Long, String> files;
	private long file_count = 0;
	final Path filePath;

	InvertedIndex(final Path directory) {
		filePath = directory;
	}

	public void indexDirectory(final Path directory) {
		pos_dictionary = new HashMap<String, HashMap<Long, String>>();
		files = new HashMap<>();

		try {
			// go through each .txt file in the working directory

			System.out.println(directory.toString());

			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					// make sure we only process the current working directory
					if (directory.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					// only process .txt files
					if (file.toString().endsWith(".txt")) {
						buildDictionary(file, file_count, pos_dictionary);
						files.put(file_count, file.getFileName().toString());
						file_count++;
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception ex) {
		}
	}

	private static void buildDictionary(Path file, long file_number,
			Map<String, HashMap<Long, String>> pos_dictionary) {
		try {
			try (Scanner scan = new Scanner(file)) {
				long word_count = 0;
				while (scan.hasNext()) {
					// read one word at a time; process and add it to
					// dictionary.

					String word = processWord(scan.next());
					if (word.length() > 0) {
						if (!pos_dictionary.containsKey(word)) {
							HashMap<Long, String> pos_hash = new HashMap<Long, String>();
							pos_dictionary.put(word, pos_hash);
						}
						if (!pos_dictionary.get(word).containsKey(file_number)) {
							pos_dictionary.get(word).put(file_number, "");
						}
						pos_dictionary.get(word).put(file_number,
								pos_dictionary.get(word).get(file_number) + word_count + ",");
						word_count++;
					}
				}
			}
		} catch (IOException ex) {
		}

	}

	private static String processWord(String next) {
		return next.replaceAll("\\W", "").toLowerCase();
	}

	public void printResults(String input, JTextPane result_txt) {
		String input_terms[] = new String[input.length()];
		input_terms = getInputTerms(input);
		System.out.println("Length of input terms" + input_terms.length);
		HashMap<Long, Long> query_result = new HashMap<>();
		if (input_terms.length > 0)
			query_result = searchQuery(input_terms);

		System.out.println("Length of input query result" + query_result.size());
		if (query_result.size() > 0) {
			for (Iterator<Long> i = query_result.keySet().iterator(); i.hasNext();) {
				long file_number = i.next();
				result_txt.setText(result_txt.getText() + files.get(file_number) + "\n");
			}
		}
		else{
			result_txt.setText("NO RESULT FOUND.......\n");
		}
	}

	private HashMap<Long, Long> searchQuery(String[] input_terms) {
		HashMap<Long, Long> file_intersection = new HashMap<Long, Long>();
		for (int i = 0; i < input_terms.length; i++) {
			if (pos_dictionary.containsKey(input_terms[i])) {
				System.out.println(" term - files" + pos_dictionary.get(input_terms[i]).keySet());
				for (Iterator<Long> j = pos_dictionary.get(input_terms[i]).keySet().iterator(); j.hasNext();) {
					long file_number = j.next();
					System.out.println("Union FIle" + files.get(file_number));
					if (!file_intersection.containsKey(file_number))
						file_intersection.put(file_number, new Long(1));
					else
						file_intersection.put(file_number, file_intersection.get(file_number) + 1);
				}
			}
		}
		for (Iterator<Long> j = file_intersection.keySet().iterator(); j.hasNext();) {
			long file_number = j.next();
			if (file_intersection.get(file_number) < input_terms.length)
				j.remove();
		}
		return file_intersection;
	}

	private String[] getInputTerms(String input) {
		String input_terms[] = new String[input.length()];
		input_terms = input.split("\\s+");
		for (int i = 0; i < input_terms.length; i++) {
			input_terms[i] = processWord(input_terms[i]);
		}
		return input_terms;
	}

}
