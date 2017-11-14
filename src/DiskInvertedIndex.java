
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;

public class DiskInvertedIndex {

   private String mPath;
   private RandomAccessFile mVocabList;
   private RandomAccessFile mPostings;
   private long[] mVocabTable;

   // Opens a disk inverted index that was constructed in the given path.
   public DiskInvertedIndex(String path) {
      try {
         mPath = path;
         mVocabList = new RandomAccessFile(new File(path, "vocab.bin"), "r");
         mPostings = new RandomAccessFile(new File(path, "postings.bin"), "r");
         mVocabTable = readVocabTable(path);
//         mFileNames = readFileNames(path);
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
   }

   private static Long[] readPostingsFromFile(RandomAccessFile postings, 
    Long postingsPosition) {
      try {
         // seek to the position in the file where the postings start.
         postings.seek(postingsPosition);
         
         // read the 8 bytes for the document frequency
         byte[] byteDocFreq = new byte[8];
         postings.read(byteDocFreq, 0, byteDocFreq.length);
         // use ByteBuffer to convert the 4 bytes into an int.
         Long documentFrequency = ByteBuffer.wrap(byteDocFreq).getLong();
         
         List <Long> DocIdScoreList = new ArrayList<Long>();
         Long seekPosition = postingsPosition;
//         System.out.print("[ "+documentFrequency+" , ");
//         for(int i=0; i<15;i++){
//        	 seekPosition += 8;
//        	 byte[] x = new byte[8];
//             postings.read(x, 0, x.length);
//             System.out.print(ByteBuffer.wrap(x).getLong()+" , ");
//         }
//         System.out.println(" ]");
         
         seekPosition = postingsPosition+8; // seek to first doc id
         Long previousDocId = new Long(0);
         for(int i = 0; i<documentFrequency; i++){
        	 postings.seek(seekPosition);
        	 byte[] docId = new byte[8];
             postings.read(docId, 0, docId.length);
             DocIdScoreList.add(previousDocId+ByteBuffer.wrap(docId).getLong());
             previousDocId += ByteBuffer.wrap(docId).getLong();
             seekPosition += 8; //seek to next term frequency
             postings.seek(seekPosition);
             byte[] posFreq = new byte[8];
             postings.read(posFreq, 0, posFreq.length);
             
             seekPosition += ByteBuffer.wrap(posFreq).getLong()*8; //seek through the positions to next document id
             
             seekPosition += 8; //seek to rank
             byte[] score = new byte[8];
             postings.read(score, 0, score.length);
             DocIdScoreList.add(ByteBuffer.wrap(score).getLong());
             seekPosition += 8; //seek to next doca id
             
             
         }
         System.out.println(DocIdScoreList);
         return DocIdScoreList.toArray(new Long[DocIdScoreList.size()]);
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }

// Reads and returns a list of document IDs that contain the given term.
   public Long[] getDocuments(String term) {
      Long postingsPosition = binarySearchVocabulary(term);
      if (postingsPosition >= 0) {
         return readPostingsFromFile(mPostings, postingsPosition);
      }
      return new Long[0];
   }
   
   
   private static Long[] readPositionsFromFile(RandomAccessFile postings, 
    Long postingsPosition, Long document) {
      try {
    	  List <Long> positionList = new ArrayList<Long>();
    	  
    	// seek to the position in the file where the postings start.
          postings.seek(postingsPosition);
          
          // read the 8 bytes for the document frequency
          byte[] byteDocFreq = new byte[8];
          postings.read(byteDocFreq, 0, byteDocFreq.length);
        		 
          // use ByteBuffer to convert the 4 bytes into an int.
          Long documentFrequency = ByteBuffer.wrap(byteDocFreq).getLong();
                   
          Long seekPosition = postingsPosition;
          
          Long previousDocId = new Long(0);
          for(int i = 0; i<documentFrequency; i++){ // seek to the start of that document posting in the array
        	  seekPosition+=8; // seek to the document id
        	  postings.seek(seekPosition);
        	  byte[] docId = new byte[8];
              postings.read(docId, 0, docId.length);
        	  if(document.equals(previousDocId+ByteBuffer.wrap(docId).getLong()))
        		  break;
        	  previousDocId+=ByteBuffer.wrap(docId).getLong();
        	  seekPosition+=8;	//move to term frequency position
        	  postings.seek(seekPosition);
        	  byte[] termFreq = new byte[8];
              postings.read(termFreq, 0, termFreq.length);
              seekPosition+=8; // move to rank
              seekPosition+=(8*ByteBuffer.wrap(termFreq).getLong());
          }    																// check which doc id it stops
          seekPosition+=8;	//move to term frequency position
          postings.seek(seekPosition);
    	  byte[] byteTermFreq = new byte[8];
          postings.read(byteTermFreq, 0, byteTermFreq.length);
          Long termFreq = ByteBuffer.wrap(byteTermFreq).getLong();
          seekPosition+=8;	//move to term rank
          seekPosition+=8; // move to first term position
          Long previousPosition = new Long(0);
          for(int i=0; i<termFreq; i++){	// decode gap and fill the position into position array
        	  postings.seek(seekPosition);
        	  byte[] byteTermPosition = new byte[8];
              postings.read(byteTermPosition, 0, byteTermPosition.length);
        	  positionList.add(previousPosition+ByteBuffer.wrap(byteTermPosition).getLong());
        	  previousPosition += ByteBuffer.wrap(byteTermPosition).getLong();
        	  seekPosition+=8;
          }
          
          return positionList.toArray(new Long[positionList.size()]);
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }
   
   
   
// Reads and returns a list of positions for the document that contain the given term.
   public Long[] getPositions(String term, Long docId) { // this should be real docid
      Long postingsPosition = binarySearchVocabulary(term);
      if (postingsPosition >= 0) {
         return readPositionsFromFile(mPostings, postingsPosition, docId);
      }
      return null;
   }
   
   // Locates the byte position of the postings for the given term.
   private long binarySearchVocabulary(String term) {
      // do a binary search over the vocabulary, using the vocabTable and the file vocabList.
      int i = 0, j = mVocabTable.length / 2 - 1;
      while (i <= j) {
         try {
            int m = (i + j) / 2;
            long vListPosition = mVocabTable[m * 2];
            int termLength;
            if (m == mVocabTable.length / 2 - 1) {
               termLength = (int)(mVocabList.length() - mVocabTable[m*2]);
            }
            else {
               termLength = (int) (mVocabTable[(m + 1) * 2] - vListPosition);
            }
            mVocabList.seek(vListPosition);

            byte[] buffer = new byte[termLength];
            mVocabList.read(buffer, 0, termLength);
            
            String fileTerm = new String(buffer, "ASCII");
            int compareValue = term.compareTo(fileTerm);
            if (compareValue == 0) {
               // found it!
               return mVocabTable[m * 2 + 1];
            }
            else if (compareValue < 0) {
               j = m - 1;
            }
            else {
               i = m + 1;
            }
         }
         catch (IOException ex) {
            System.out.println(ex.toString());
         }
      }
      return -1;
   }

   // Reads the file vocabTable.bin into memory.
   private static long[] readVocabTable(String indexName) {
      try {
         long[] vocabTable;
         
         RandomAccessFile tableFile = new RandomAccessFile(
          new File(indexName, "vocabTable.bin"),
          "r");
         
         byte[] byteBuffer = new byte[4];
         tableFile.read(byteBuffer, 0, byteBuffer.length);
        
         int tableIndex = 0;
         vocabTable = new long[ByteBuffer.wrap(byteBuffer).getInt() * 2];
         byteBuffer = new byte[8];
         
         while (tableFile.read(byteBuffer, 0, byteBuffer.length) > 0) { // while we keep reading 4 bytes
            vocabTable[tableIndex] = ByteBuffer.wrap(byteBuffer).getLong();
            tableIndex++;
         }
         tableFile.close();
         return vocabTable;
      }
      catch (FileNotFoundException ex) {
         System.out.println(ex.toString());
      }
      catch (IOException ex) {
         System.out.println(ex.toString());
      }
      return null;
   }

   public int getTermCount() {
      return mVocabTable.length / 2;
   }

	public Paths getFileNames() {
		// TODO Auto-generated method stub
		return null;
	}
}
