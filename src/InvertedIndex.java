import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class InvertedIndex {

	protected  Map<String, HashMap<Long, String>> pos_dictionary;
	protected HashMap<Long, String> files;
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
		} catch (IOException ex) {
		}

	}
}
