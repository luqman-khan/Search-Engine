import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import java.util.Properties;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.LongComparator;
import jdbm.helper.StringComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;

/**
 * Writes an inverted indexing of a directory to disk.
 */
public class IndexWriter {

	private String mFolderPath;
	static String DATABASE = "CorpusIndex";
    static String VOCAB_BTREE_NAME = "vocab";

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
	}

	/**
	 * Builds the postings.bin file for the indexed directory, using the given
	 * PositionalInvertedIndex of that directory.
	 */
	private static void buildPostingsFile(String folder, InvertedIndex index, String[] dictionary,
			Long[] vocabPositions) {
		
		RecordManager recman;
	    long          recid;
	    Tuple         tuple = new Tuple();
	    TupleBrowser  browser;
	    BTree         vocabTree,postingTree,vocabTableTree;
	    Properties    props  = new Properties();
		FileOutputStream postingsFile = null;
		
		try {
			 // open database and setup an object cache
            recman = RecordManagerFactory.createRecordManager( DATABASE, props );

            // try to reload an existing B+Tree
            recid = recman.getNamedObject( VOCAB_BTREE_NAME );
            if ( recid != 0 ) {
                vocabTree = BTree.load( recman, recid );
                System.out.println( "Reloaded existing BTree with " + vocabTree.size()
                                    + " famous people." );
            } else {
            	vocabTree = BTree.createInstance( recman, new LongComparator() );
                recman.setNamedObject( VOCAB_BTREE_NAME, vocabTree.getRecid() );
                System.out.println( "Created a new empty BTree" );
            }
            
            
            
			postingsFile = new FileOutputStream(new File(folder, "postings.bin"));
			FileOutputStream vocabTable = new FileOutputStream(new File(folder, "vocabTable.bin"));

			byte[] tSize = ByteBuffer.allocate(4).putInt(dictionary.length).array();
			vocabTable.write(tSize, 0, tSize.length);
			int vocabI = 0;
			for (String s : dictionary) {
				// for each String in dictionary, retrieve its postings.
				Long[] postings = index.pos_hash_list.getDocumentsScore(s);					//b+
				for(int i=0; i<dictionary.length; i++){
					vocabTree.insert( new Long(i),dictionary[i], false );
	            }
				byte[] vPositionBytes = ByteBuffer.allocate(8).putLong(vocabPositions[vocabI]).array();
				vocabTable.write(vPositionBytes, 0, vPositionBytes.length); 	//write vocab pointer to the table

				byte[] pPositionBytes = ByteBuffer.allocate(8).putLong(postingsFile.getChannel().position()).array();
				vocabTable.write(pPositionBytes, 0, pPositionBytes.length);	//write posting pointer to the table

				byte[] docFreqBytes = ByteBuffer.allocate(8).putLong(postings.length/2).array();
				postingsFile.write(docFreqBytes, 0, docFreqBytes.length);
				
				Long lastDocId = new Long(0);
				Long docId;
				for (int i=0; i<postings.length;i+=2) {
					docId = postings[i];
					byte[] docIdBytes = ByteBuffer.allocate(8).putLong(docId - lastDocId).array();
					postingsFile.write(docIdBytes, 0, docIdBytes.length);
					lastDocId = docId;

					Long positions[] = index.pos_hash_list.getPositions(s, new Long(docId));

					byte[] posFreqBytes = ByteBuffer.allocate(8).putLong(positions.length).array();
					postingsFile.write(posFreqBytes, 0, posFreqBytes.length);
					byte[] docRankBytes = ByteBuffer.allocate(8).putLong(postings[i+1]).array(); 
					postingsFile.write(docRankBytes, 0, docRankBytes.length); // write rank

					Long lastPos = new Long(0);
					for (Long pos : positions) {
						byte[] posBytes = ByteBuffer.allocate(8).putLong(pos - lastPos).array();
						postingsFile.write(posBytes, 0, posBytes.length);
						lastPos = pos;
					}
				}
				System.out.println("calculation done : "+s);
				vocabI++;
			}
			recman.commit();
			browser = vocabTree.browse();
            while ( browser.getNext( tuple ) ) {
                print( tuple );
            }
            
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
	
	static void print( Tuple tuple ) {
        Long postings = (Long) tuple.getValue();
        System.out.println(Arrays.asList(postings) );
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
