import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

	private String mFolderPath;
	private static FileOutputStream postingsFile;

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
		String[] dictionary = index.getDictionary();
		Long[] vocabPositions = new Long[dictionary.length];
		Arrays.sort(dictionary);
		vocabPositions = buildVocabFile(folder, dictionary, vocabPositions);
		buildPostingsFile(folder, index, dictionary, vocabPositions);
		buildFileIdMap(folder,index.files);
	}

	private static void buildFileIdMap(String folder, HashMap<Long, String> files) {
		
		try {
			FileOutputStream fileIdMap = new FileOutputStream(new File(folder, "FileIdMap.bin"));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileIdMap));
			for(String f : files.values()){
				writer.write(f);
				writer.newLine();
			}
			writer.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Builds the postings.bin file for the indexed directory, using the given
	 * PositionalInvertedIndex of that directory.
	 */
	private static void buildPostingsFile(String folder, InvertedIndex index, String[] dictionary,
			Long[] vocabPositions) {
		
		try {
			postingsFile = new FileOutputStream(new File(folder, "postings.bin"));
			FileOutputStream vocabTable = new FileOutputStream(new File(folder, "vocabTable.bin"));
//			ByteArrayOutputStream byte_output_stream__postings;
//			ByteArrayOutputStream byte_output_stream__vocabTable;
//			Long postings_file_channel_position = postingsFile.getChannel().position();
			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length).array();
			vocabTable.write(tSize, 0, tSize.length);
			int vocabI = 0;
//			Long size = new Long(99999999);
//			byte_output_stream__postings = new ByteArrayOutputStream(size.intValue());
//			byte_output_stream__vocabTable = new ByteArrayOutputStream(size.intValue());
			for (String s : dictionary) {
				// for each String in dictionary, retrieve its postings.
				
				Long[] documentsForTerm = index.pos_hash_list.getDocuments(s);	
				byte[] vPositionBytes = ByteBuffer.allocate(8).putLong(vocabPositions[vocabI]).array();
//				byte_output_stream__vocabTable.write(vPositionBytes);
				vocabTable.write(vPositionBytes);
//				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postings_file_channel_position+byte_output_stream__postings.size()).array();
				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postingsFile.getChannel().position()).array();
//				byte_output_stream__vocabTable.write(pPositionBytes);
				vocabTable.write(pPositionBytes);
//				byte_output_stream__postings.write(new VariableByteEncodingCLass().encodeNumberToByteArray(new Long(documentsForTerm.length)));
				postingsFile.write(new VariableByteEncodingCLass().encodeNumberToByteArray(new Long(documentsForTerm.length)));
				
				Long lastDocId = new Long(0);
				Long docId;
				for (int i=0; i<documentsForTerm.length;i++) {
					docId = documentsForTerm[i];
//					byte_output_stream__postings.write(new VariableByteEncodingCLass().encodeNumberToByteArray(docId-lastDocId));
					postingsFile.write(new VariableByteEncodingCLass().encodeNumberToByteArray(docId-lastDocId));
					lastDocId = docId;
					Long positions[] = index.pos_hash_list.getPositions(s, new Long(docId));		// add term freq
//					byte_output_stream__postings.write(new VariableByteEncodingCLass().encodeNumberToByteArray(new Long(positions.length)));
					postingsFile.write(new VariableByteEncodingCLass().encodeNumberToByteArray(new Long(positions.length)));
//					byte_output_stream__postings.write(new ScoreCalculator().calculateScores(new Long(positions.length), new Long(documentsForTerm.length), index.pos_hash_list.docInfoArray.get(docId.intValue()),new Long (index.pos_hash_list.docInfoArray.size())));
					postingsFile.write(new ScoreCalculator().calculateScores(new Long(positions.length), new Long(documentsForTerm.length), index.pos_hash_list.docInfoArray.get(docId.intValue()),new Long (index.pos_hash_list.docInfoArray.size())));
					
					Long lastPos = new Long(0);
					for (Long pos : positions) {
//						byte_output_stream__postings.write(new VariableByteEncodingCLass().encodeNumberToByteArray(pos-lastPos));
						postingsFile.write(new VariableByteEncodingCLass().encodeNumberToByteArray(pos-lastPos));
						lastPos = pos;
					}
				}
				System.out.println("calculation done : "+s);
//				postingsFile.write(byte_output_stream__postings.toByteArray());
//				vocabTable.write(byte_output_stream__vocabTable.toByteArray());
				vocabI++;
			}
			System.out.println("posting file writing in progress");
//			postingsFile.write(byte_output_stream__postings.toByteArray());
//			vocabTable.write(byte_output_stream__vocabTable.toByteArray());
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
			int vocabI = 0;
			vocabList = new OutputStreamWriter(new FileOutputStream(new File(folder, "vocab.bin"), false), "ASCII");

			Long vocabPos = new Long(0);
			for (String vocabWord : dictionary) {
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
