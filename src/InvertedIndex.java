import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import javax.swing.JTextPane;

public class InvertedIndex {

	private String[] mTermArray;
	private String[] mFileArray;
	private boolean[][] mIndex;
	final Path filePath;

	InvertedIndex(final Path directory) {
		filePath = directory;
	}

	/**
	 * Indexes all .txt files in the specified directory. First builds a
	 * dictionary of all terms in those files, then builds a boolean
	 * term-document matrix as the index.
	 * 
	 * @param directory
	 *            the Path of the directory to index.
	 */
	public void indexDirectory(final Path directory) {
		// will need a data structure to store all the terms in the document
		// HashSet: a hashtable structure with constant-time insertion; does not
		// allow duplicate entries; stores entries in unsorted order.

		final HashSet<String> dictionary = new HashSet<>();
		final HashSet<String> files = new HashSet<>();

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
						buildDictionary(file, dictionary);
						files.add(file.getFileName().toString());
					}
					return FileVisitResult.CONTINUE;
				}

				// don't throw exceptions if files are locked/other errors occur
				public FileVisitResult visitFileFailed(Path file, IOException e) {

					return FileVisitResult.CONTINUE;
				}
			});

			// convert the dictionaries to sorted arrays, so we can use binary
			// search for finding indices.
			mTermArray = dictionary.toArray(new String[0]);
			mFileArray = files.toArray(new String[0]);

			Arrays.sort(mTermArray);
			Arrays.sort(mFileArray);

			// construct the term-document matrix. docs are rows, terms are
			// cols.
			mIndex = new boolean[files.size()][dictionary.size()];

			// walk back through the files -- a second time!!
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (directory.equals(dir)) {
						return FileVisitResult.CONTINUE;
					}
					return FileVisitResult.SKIP_SUBTREE;
				}

				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

					if (file.toString().endsWith(".txt")) {
						// add entries to the index matrix for this file
						indexFile(file, mIndex, mTermArray, mFileArray);
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

	private static void buildDictionary(Path file, HashSet<String> dictionary) {
		try {
			try (Scanner scan = new Scanner(file)) {
				while (scan.hasNext()) {
					// read one word at a time; process and add it to
					// dictionary.

					String word = processWord(scan.next());
					if (word.length() > 0) {
						dictionary.add(word);
					}
				}
			}
		} catch (IOException ex) {
		}

	}

	private static void indexFile(Path file, boolean[][] index, String[] dictArray, String[] fileArray) {

		// get the row# for this file in the index matrix.
		int fileRow = Arrays.binarySearch(fileArray, file.getFileName().toString());

		try {
			// read one word at a time; process and update the matrix.
			try (Scanner scan = new Scanner(file)) {
				while (scan.hasNext()) {
					String word = processWord(scan.next());
					if (word.length() > 0) {
						int wordCol = Arrays.binarySearch(dictArray, word);

						index[fileRow][wordCol] = true;
					}
				}
			}
		} catch (IOException ex) {
		}

	}

	private static String processWord(String next) {
		return next.replaceAll("\\W", "").toLowerCase();
	}

	/**
	 * Prints the result of the indexing, as a matrix of 0 and 1 entries.
	 * Warning: prints a lot of text :).
	 */
	public void printResults(String input, JTextPane result_txt) {
		int wNdx = -1;
		for (int i = 0; i < mTermArray.length; i++) {
			if (mTermArray[i].equalsIgnoreCase(input)) {
				wNdx = i;
				break;
			}
		}
		if (wNdx >= 0) {
			int fNdx = 0;
			for (String file : mFileArray) {
				if (mIndex[fNdx][wNdx])
					result_txt.setText(result_txt.getText() + file + "\n");
				fNdx++;
			}
		} else {
			result_txt.setText("NO SUCH WORD FOUND...");
		}
	}

}
