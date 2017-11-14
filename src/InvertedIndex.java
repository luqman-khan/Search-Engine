import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JTextArea;

public class InvertedIndex {

	// protected Map<String, HashMap<Long, String>> pos_dictionary;
	HashList pos_hash_list = new HashList();
	protected HashMap<Long, String> files;
	private long file_count = 0;
	final Path folder_path;
	
	FileOutputStream mdocWeights;

	InvertedIndex(final Path directory) {
		folder_path = directory;
		try {
			mdocWeights = new FileOutputStream(new File(folder_path.toString(), "docWeights.bin"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * traverse through all files in the directory and calls "buildDictionary"
	 * for each of the instance
	 */
	public void indexDirectory() {
		// pos_dictionary = new HashMap<String, HashMap<Long, String>>();
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
						buildDictionary(file, file_count);
						files.put(file_count, file.getFileName().toString());
						file_count++;
					}
					pos_hash_list.total_docs = file_count;
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}

			});
			// pos_hash_list.print();

		} catch (Exception ex) {
		}
	}

	/**
	 * builds inverted index of the file given in the argument
	 */
	private void buildDictionary(Path file, long file_number) {
		try {
			HashMap<String, Long> wdt = new HashMap<>();;
			try (Scanner scan = new Scanner(file)) {
				long word_count = 0;
				while (scan.hasNext()) {
					String word = scan.next();
					if (word.length() > 0) {
						String[] word_array = word.trim().split("[-|@]");
						for (String i : word_array) {
							if (!i.isEmpty() && !i.equals("[ ]+")) {
								i = new Stemmer().processWord(i);
								if(wdt.containsKey(i))
									wdt.put(i, wdt.get(i)+1);
								else
									wdt.put(i, new Long(1));
								pos_hash_list.add(file_number, i, word_count);
								word_count++;
							}
						}
					}
				}
			}
			Double ld = calculateLD(wdt);
			pos_hash_list.docWeightsArray.add(ld);
			byte []ldBytes = ByteBuffer.allocate(8).putDouble(ld).array();
			mdocWeights.write(ldBytes,0, ldBytes.length);
			mdocWeights.close();
		} catch (IOException ex) {
		}
	}

	private Double calculateLD(HashMap<String, Long> wdt) {
		Collection<Long> allWdt = wdt.values();
		Double ld = new Double(0);
		for(Long i : allWdt){
			ld+=i^2;
		}
		return Math.sqrt(ld);
	}

	/**
	 * outputs the vocab word list (stemmed)
	 */
	public String indexPrint() {
		String vocab_string = "";
		for (Iterator<String> i = pos_hash_list.keySet().iterator(); i.hasNext();)
			vocab_string = vocab_string + "\n" + i.next();
		return vocab_string;
	}

	public String[] getDictionary() {
		Set<String> terms_set = this.pos_hash_list.keySet();
		String[] terms = terms_set.toArray(new String[terms_set.size()]);
		return terms;
	}
}
