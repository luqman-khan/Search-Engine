import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
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
	private long total_docs = 0;

	InvertedIndex(final Path directory) {
		folder_path = directory;
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
					total_docs = file_count;

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

	protected long[] calculateWdt(HashList pos_hash_list) {
		String[] terms = getDictionary();
		long a[] = null;
		double wdt = 0;
		 
		for (int i = 0; i < terms.length; i++) {// for each term in corpus
			System.out.println("term is  " + terms[i]);
			Long[] term_freq_array = pos_hash_list.getTermFreq(terms[i]);
			a = new long[term_freq_array.length / 2];
			System.out.println("term freq array *******" + Arrays.asList(term_freq_array));
			// for each tftd in term_freq_array
			for (int j = 1; j < term_freq_array.length; j += 2) {
//				double wdt = (1 + Math.log(term_freq_array[j])) * 100;
//				multiply by 100 to save the two decimals after point
				long wdtLong = (new Double((1 + Math.log(term_freq_array[j])) * 100)).longValue();
				
				for (int m = 0; m < a.length; m++) {
					
					
					if (term_freq_array[j] > 0)
						a[m] = wdtLong;
					else
						a[m] = 1;
				}
//				System.out.println("Wdt is ******* " + wdtLong);
			}
		}
//		 pos_hash_list.getDocuments();
		System.out.println("building long term freq array *******************");
		 for(int l=0;l<a.length;l++){
		 System.out.print("term array **************"+a[l]+",");}
		return a;

	}

	// private double getTFTD(HashList pos_hash_list) {
	// double tftd = 0;
	// for(int i = 0; ;i++){
	//
	// }
	// }

	// private void calculateWqt(HashList pos_hash_list){
	// String [] terms = getDictionary();
	// for(int i =0;i<getDictionary().length;i++){
	// //System.out.println(terms[i]);
	// for(int j = 0;j<pos_hash_list.getDocuments(terms[i]).length;j++){
	// double wdt = Math.log10(pos_hash_list.term_freq_pos);
	// pos_hash_list.
	// }
	// }
	// //pos_hash_list.getDocuments();
	// }
	// private void calculateLdt(HashList pos_hash_list){
	// String [] terms = getDictionary();
	// for(int i =0;i<getDictionary().length;i++){
	// //System.out.println(terms[i]);
	// for(int j = 0;j<pos_hash_list.getDocuments(terms[i]).length;j++){
	// double wdt = Math.log10(pos_hash_list.term_freq_pos);
	// pos_hash_list.
	// }
	// }
	// //pos_hash_list.getDocuments();
	// }
	/**
	 * builds inverted index of the file given in the argument
	 */
	private void buildDictionary(Path file, long file_number) {
		try {
			try (Scanner scan = new Scanner(file)) {
				long word_count = 0;
				while (scan.hasNext()) {
					String word = scan.next();
					if (word.length() > 0) {
						String[] word_array = word.trim().split("[-|@]");
						for (String i : word_array) {
							if (!i.isEmpty() && !i.equals("[ ]+")) {
								i = new Stemmer().processWord(i);
								pos_hash_list.add(file_number, i, word_count);
								word_count++;
							}
						}
					}
				}
			}
		} catch (IOException ex) {
		}

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
		// TODO Auto-generated method stub
		Set<String> terms_set = this.pos_hash_list.keySet();
		String[] terms = terms_set.toArray(new String[terms_set.size()]);
		return terms;
	}
}
