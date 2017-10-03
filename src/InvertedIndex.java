import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JTextArea;

public class InvertedIndex {

	protected Map<String, HashMap<Long, String>> pos_dictionary;
	protected HashMap<Long, String> files;
	private long file_count = 0;
	final Path folder_path;

	InvertedIndex(final Path directory) {
		folder_path = directory;
	}
	
	/**
	 * traverse through all files in the directory and calls "buildDictionary" for each of the instance
	 */
	public void indexDirectory() {
		pos_dictionary = new HashMap<String, HashMap<Long, String>>();
		files = new HashMap<>();

		try {
			Files.walkFileTree(folder_path, new SimpleFileVisitor<Path>() {

				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (folder_path.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (file.toString().endsWith(".txt")) {
						buildDictionary(file, file_count, pos_dictionary);
						files.put(file_count, file.getFileName().toString());
						file_count++;
					}
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult visitFileFailed(Path file, IOException e) {

					return FileVisitResult.CONTINUE;
				}
			});
		} catch (Exception ex) {
		}
	}
	
	/**
	 * builds inverted index of the file given in the argument
	 */
	private static void buildDictionary(Path file, long file_number,
			Map<String, HashMap<Long, String>> pos_dictionary) {
		try {
			try (Scanner scan = new Scanner(file)) {
				long word_count = 0;
				while (scan.hasNext()) {
					String word = new Stemmer().processWord(scan.next());
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
		} catch (IOException ex) {}

	}
	
	/**
	 * outputs the vocab word list (stemmed)
	 */
	public void indexPrint(JTextArea result_txt) {
		String vocab_string = "";
		for (Iterator<String> i = pos_dictionary.keySet().iterator(); i.hasNext();)
			vocab_string = vocab_string + "\n" + i.next();
		result_txt.setText(vocab_string);
	}
}
