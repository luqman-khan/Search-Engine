import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

	private String mFolderPath;

	/**
	 * Constructs an IndexWriter object which is prepared to index the given
	 * folder.
	 */
	public IndexWriter(String folderPath) {
		mFolderPath = folderPath;
	}

	/**
	 * Builds and writes an inverted index to disk. Creates three files:
	 * vocab.bin, containing the vocabulary of the corpus; postings.bin,
	 * containing the postings list of document IDs; vocabTable.bin, containing
	 * a table that maps vocab terms to postings locations
	 */
	public void buildIndex(InvertedIndex index) {
		buildIndexForDirectory(index, mFolderPath);
	}

	/**
	 * Builds the normal PositionalInvertedIndex for the folder.
	 */
	private static void buildIndexForDirectory(InvertedIndex index, String folder) {
		// at this point, "index" contains the in-memory inverted index
		// now we save the index to disk, building three files: the postings
		// index,
		// the vocabulary list, and the vocabulary table.

		// the array of terms
		String[] dictionary = index.getDictionary();
		// an array of positions in the vocabulary file
		Long[] vocabPositions = new Long[dictionary.length];
//		System.out.println("Here is memory vocab list " + index.pos_hash_list.keySet());
		Arrays.sort(dictionary);
		vocabPositions = buildVocabFile(folder, dictionary, vocabPositions);
//		System.out.println("my vocab positions : " + Arrays.asList(vocabPositions));
//		try {
//			new DataInputStream(System.in).readLine();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		buildPostingsFile(folder, index, dictionary, vocabPositions);
//		System.out.println("after building poting file");
//		try {
//			new DataInputStream(System.in).readLine();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	/**
	 * Builds the postings.bin file for the indexed directory, using the given
	 * PositionalInvertedIndex of that directory.
	 */
	private static void buildPostingsFile(String folder, InvertedIndex index, String[] dictionary,
			Long[] vocabPositions) {
		FileOutputStream postingsFile = null;
		try {
			postingsFile = new FileOutputStream(new File(folder, "postings.bin"));

			// simultaneously build the vocabulary table on disk, mapping a term
			// index to a
			// file location in the postings file.
			FileOutputStream vocabTable = new FileOutputStream(new File(folder, "vocabTable.bin"));

			// the first thing we must write to the vocabTable file is the
			// number of vocab terms.
			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length).array();
			vocabTable.write(tSize, 0, tSize.length);
			int vocabI = 0;
			ArrayList<Byte> postingArrayList = new ArrayList<>();
			ArrayList<Byte> vocabTableArrayList = new ArrayList<>();
			for (String s : dictionary) {
				// for each String in dictionary, retrieve its postings.
				Long[] postings = index.pos_hash_list.getDocumentsScore(s);
				byte[] vPositionBytes = ByteBuffer.allocate(8).putLong(vocabPositions[vocabI]).array();
				vocabTable.write(vPositionBytes, 0, vPositionBytes.length); 	//write vocab pointer to the table
//				for(byte b : vPositionBytes)
//					vocabTableArrayList.add(b);

				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postingsFile.getChannel().position()).array();
				vocabTable.write(pPositionBytes, 0, pPositionBytes.length);	//write posting pointer to the table
//				for(byte b : pPositionBytes)
//					vocabTableArrayList.add(b);

				byte[] docFreqBytes = ByteBuffer.allocate(8).putLong(postings.length/2).array();
				postingsFile.write(docFreqBytes, 0, docFreqBytes.length);
//				for(byte b : docFreqBytes)
//					postingArrayList.add(b);
				
				
				Long lastDocId = new Long(0);
				// int docBytePointer = docFreqBytes.length;
				// System.out.print(s+" : ");
				Long docId;
				for (int i=0; i<postings.length;i+=2) {
					docId = postings[i];
					// System.out.print(docId - lastDocId+" : ");
					byte[] docIdBytes = ByteBuffer.allocate(8).putLong(docId - lastDocId).array();
					postingsFile.write(docIdBytes, 0, docIdBytes.length);
//					for(byte b : docIdBytes)
//						postingArrayList.add(b);
					lastDocId = docId;

					Long positions[] = index.pos_hash_list.getPositions(s, new Long(docId));

					byte[] posFreqBytes = ByteBuffer.allocate(8).putLong(positions.length).array();
					postingsFile.write(posFreqBytes, 0, posFreqBytes.length);
//					for(byte b : posFreqBytes)
//						postingArrayList.add(b);]
					byte[] docRankBytes = ByteBuffer.allocate(8).putLong(postings[i+1]).array(); 
					postingsFile.write(docRankBytes, 0, docRankBytes.length); // write rank
//					for(byte b : docRankBytes)
//						postingArrayList.add(b);

					// System.out.print("[ ");

					Long lastPos = new Long(0);
					for (Long pos : positions) {
						// System.out.print(pos-lastPos+" , ");
						byte[] posBytes = ByteBuffer.allocate(8).putLong(pos - lastPos).array();
						postingsFile.write(posBytes, 0, posBytes.length);
						
//						for(byte b : posBytes)
//							postingArrayList.add(b);
						lastPos = pos;
					}
					// System.out.println(" ]");
				}
				System.out.println("calculation done : "+s);
				vocabI++;
			}
			
//			byte [] vocabTableByteBuffer = new byte[vocabTableArrayList.size()];
//			for(int i=0;i<vocabTableArrayList.size();i++)
//				vocabTableByteBuffer[i]=vocabTableArrayList.get(i);
//			
//			byte [] postingByteBuffer = new byte[postingArrayList.size()];
//			for(int i=0;i<postingArrayList.size();i++)
//				postingByteBuffer[i]=postingArrayList.get(i);
//			
//			vocabTable.write(vocabTableByteBuffer, 0, vocabTableByteBuffer.length);
//			postingsFile.write(postingByteBuffer, 0, postingByteBuffer.length);
//			vocabTable.flush();
//			postingsFile.flush();
			vocabTable.close();
			postingsFile.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		} finally {
			try {
				postingsFile.close();
			} catch (IOException ex) {
			}
		}
	}

	private static Long[] buildVocabFile(String folder, String[] dictionary, Long[] vocabPositions) {
		OutputStreamWriter vocabList = null;
		try {
			// first build the vocabulary list: a file of each vocab word
			// concatenated together.
			// also build an array associating each term with its byte location
			// in this file.
			int vocabI = 0;
			vocabList = new OutputStreamWriter(new FileOutputStream(new File(folder, "vocab.bin"), false), "ASCII");

			Long vocabPos = new Long(0);
			for (String vocabWord : dictionary) {
				// for each String in dictionary, save the byte position where
				// that term will start in the vocab file.
				vocabPositions[vocabI] = vocabPos;
				vocabList.write(vocabWord); // then write the String
				vocabI++;
				vocabPos += vocabWord.length();
			}
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (UnsupportedEncodingException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		} finally {
			try {
				vocabList.close();
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}
		return vocabPositions;
	}
}
