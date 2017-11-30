
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.*;

public class DiskInvertedIndex {

	private RandomAccessFile mVocabList;
	private RandomAccessFile mPostings;
	private long[] mVocabTable;
	public final static int MASK = 0xff;

	// Opens a disk inverted index that was constructed in the given path.
	public DiskInvertedIndex(String path) {
		try {
			mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
			mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
			mVocabTable = readVocabTable(path);
			// mFileNames = readFileNames(path);
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		}
	}
	
	private static int getIntValue(Byte a_byte) {
		return a_byte & MASK;
	}
	private static Long[] getNextVB(RandomAccessFile myFile,Long seekPosition){
		List<Byte> bufferList = new ArrayList<>();
		try {
			Byte buffer = myFile.readByte();
			while(getIntValue(buffer)<128){
				bufferList.add(buffer);
				seekPosition++;
				myFile.seek(seekPosition);
				buffer = myFile.readByte();
			}
			bufferList.add(buffer);
			Long nextTerm = new VariableByteEncodingCLass().decodeNumber(bufferList.toArray(new Byte[bufferList.size()]));
			Long returnItemArray[] = {nextTerm,seekPosition};
			return returnItemArray;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	

	private static PostingData readDocumentScoreFromFile(RandomAccessFile postings, Long[] postingsPosition) {
		try {
			PostingData pd = new PostingData();
			
			ArrayList<Long> vbePostingsList = new ArrayList<>();
//			HashMap<Long,Double[]> docIdScoreMap = new HashMap<>();
			Long nextTermData[];
			
			// seek to the position in the file where the postings start.
			Long seekPosition = postingsPosition[0];	// document frequency
			postings.seek(seekPosition);
			nextTermData = getNextVB(postings,seekPosition);	// returns next term and seek position
			Long doc_freq = nextTermData[0];
			seekPosition = nextTermData[1];seekPosition++;
			Long doc_id,pos_freq;
			Double score[];
			byte[] byteScore;
			Long last_doc_id = new Long(0);
			for(int i=0;i<doc_freq;i++){					// implement difference
				nextTermData = getNextVB(postings,seekPosition);		//get document id
				doc_id = nextTermData[0]+last_doc_id;
				seekPosition = nextTermData[1];
				last_doc_id = doc_id;
				vbePostingsList.add(doc_id);
				seekPosition++;
				
				nextTermData = getNextVB(postings,seekPosition);		//get the term frequency
				pos_freq = nextTermData[0];
				seekPosition = nextTermData[1];
				vbePostingsList.add(pos_freq);
				
				seekPosition++;			//move to term rank for the document
				score = new Double[4];
				for(int j=0;j<4;j++){
					postings.seek(seekPosition);
					byteScore = new byte[8];
					postings.read(byteScore, 0, byteScore.length);
					score[j] = ByteBuffer.wrap(byteScore).getDouble();
					seekPosition+=8;
				}
				pd.docIdScoreHash.put(doc_id, score);				// adding rank
//				docIdScoreMap.put(doc_id, score);
//				byte[] vbePositions = new byte[(int) (postingsPosition[1] - seekPosition)];
				Long last_position = new Long(0);
				for(int j=0; j<pos_freq;j++){							// adding all postings
					nextTermData = getNextVB(postings,seekPosition);
					Long pos = last_position+nextTermData[0];
					if(!pd.docIdPositionsHash.containsKey(doc_id)){
						ArrayList<Long> a = new ArrayList<>();
						a.add(pos);
						pd.docIdPositionsHash.put(doc_id, a);
						
					} else {
						pd.docIdPositionsHash.get(doc_id).add(pos);
					}
					last_position=pos;
					seekPosition = nextTermData[1];
					seekPosition++;
				}
				
			}
			return pd;
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}
	public PostingData getPostings(String term) {
		Long[] postingsPosition = binarySearchVocabulary(term);
		if (postingsPosition[0] >= 0) {
			return readDocumentScoreFromFile(mPostings, postingsPosition);
		}
		return null;
	}
	
	// Locates the byte position of the postings for the given term.
	private Long[] binarySearchVocabulary(String term) {
		// do a binary search over the vocabulary, using the vocabTable and the
		// file vocabList.
		int i = 0, j = mVocabTable.length / 2 - 1;
		Long ans[] = { new Long(-1) };
		while (i <= j) {
			try {
				int m = (i + j) / 2;
				long vListPosition = mVocabTable[m * 2];
				int termLength;
				if (m == mVocabTable.length / 2 - 1) {
					termLength = (int) (mVocabList.length() - mVocabTable[m * 2]);
				} else {
					termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
				}
				mVocabList.seek(vListPosition);

				byte[] buffer = new byte[termLength];
				mVocabList.read(buffer, 0, termLength);

				String fileTerm = new String(buffer, "ASCII");
				int compareValue = term.compareTo(fileTerm);
				if (compareValue == 0) {
					// found it!
					Long[] localAns = { new Long(mVocabTable[m * 2 + 1]), new Long(mVocabTable[m * 2 + 3]) };
					return (localAns);
				} else if (compareValue < 0) {
					j = m - 1;
				} else {
					i = m + 1;
				}
			} catch (IOException ex) {
				System.out.println(ex.toString());
			}
		}

		return ans;
	}

	// Reads the file vocabTable.bin into memory.
	private static long[] readVocabTable(String indexName) {
		try {
			long[] vocabTable;

			RandomAccessFile tableFile = new RandomAccessFile(new File(indexName, "vocabTable.bin"), "r");

			byte[] byteBuffer = new byte[4];
			tableFile.read(byteBuffer, 0, byteBuffer.length);

			int tableIndex = 0;
			vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
			byteBuffer = new byte[8];

			while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { 
				vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
				tableIndex++;
			}
			tableFile.close();
			return vocabTable;
		} catch (FileNotFoundException ex) {
			System.out.println(ex.toString());
		} catch (IOException ex) {
			System.out.println(ex.toString());
		}
		return null;
	}

	public int getTermCount() {
		return mVocabTable.length / 2;
	}

}
