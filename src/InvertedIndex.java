import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class InvertedIndex {

	// protected Map<String, HashMap<Long, String>> pos_dictionary;
	protected HashList pos_hash_list = new HashList();
	protected HashMap<Long, String> files;
	private long file_count = 0;
	final Path folder_path;
	protected ByteArrayOutputStream byte_output_stream_docWeights = new ByteArrayOutputStream();
	
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
					return FileVisitResult.CONTINUE;
				}

				public FileVisitResult visitFileFailed(Path file, IOException e) {
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
					System.out.println("DOcWeights writing in progress");
					mdocWeights.write(byte_output_stream_docWeights.toByteArray());
					mdocWeights.close();
					return super.postVisitDirectory(arg0, arg1);
				}

			});
			// pos_hash_list.print();

		} catch (Exception ex) {
		}
	}

	protected void calculateDocAvgToken() {
		for(DocumentInfo di : pos_hash_list.docInfoArray){
//			System.out.println("calc avg token : "+di.token_count+" and "+pos_hash_list.total_term_count+" after div : "+new Double(new Double(di.token_count)/new Double(pos_hash_list.total_term_count)));
			di.avg_term_count = new Double(new Double(di.token_count)/new Double(pos_hash_list.total_term_count));
		}
	}

	/**
	 * builds inverted index of the file given in the argument
	 */
	private void buildDictionary(Path file, long file_number) {
		try {
			HashMap<String, Long> termFreq = new HashMap<>();
			DocumentInfo docInfo;
			long word_count, char_count;
			try (Scanner scan = new Scanner(file)) {
				word_count = 0;
				char_count = 0;
				while (scan.hasNext()) {
					String word = scan.next();

					if (word.length() > 0) {
						String[] word_array = word.trim().split("[-|@]");
						for (String i : word_array) {
							if (!i.isEmpty() && !i.equals("\\s+") && i!=null && i!="") {
								i = new Stemmer().processWord(i);
								if(termFreq.containsKey(i))
									termFreq.put(i, termFreq.get(i)+1);
								else
									termFreq.put(i, new Long(1));
								pos_hash_list.add(file_number, i, word_count);
								word_count++;
								char_count+=word.length();
							}
						}
					}
				}
			}
			pos_hash_list.total_term_count += termFreq.size();
//			System.out.println("current total term count: "+pos_hash_list.total_term_count);
//			System.out.println(termFreq);
			Double ld = calculateLD(termFreq);
//			System.out.println("**********************************"+file_number+","+ld);
			
			docInfo = new DocumentInfo();
			docInfo.ld = ld;
			docInfo.word_count = word_count;
			docInfo.token_count = new Long(termFreq.size());
			docInfo.byteSize = ((char_count+word_count) * 2) + 45;
			docInfo.avgTf = termFreq.values().stream().mapToDouble(Number::doubleValue).sum()/termFreq.size();
//			docInfo.avg_term_count = new Double(1);
			pos_hash_list.docInfoArray.add(docInfo);
			System.out.println("for document : "+file_number);
			byte []ldBytes = ByteBuffer.allocate(8).putDouble(ld).array();
			byte_output_stream_docWeights.write(ldBytes);
			
		} catch (IOException ex) { 
		}
	}

	private Double calculateLD(HashMap<String, Long> termFreq) {
		Collection<Long> alltermFreq = termFreq.values();
		Double ld = new Double(0);
		for(Long i : alltermFreq){
			Double termFreq_val = (1+Math.log(new Double(i)));
			ld+= (termFreq_val*termFreq_val);
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
