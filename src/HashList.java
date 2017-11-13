import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

public class HashList {

	HashMap<String, ArrayList<Long>> posting_list;
	private int doc_id_pos, term_freq_pos;

	HashList() {
		posting_list = new HashMap<>();
	}

	protected void add(Long doc_id, String term, long position) {

		if (!this.posting_list.containsKey(term))
			this.posting_list.put(term, new ArrayList<Long>());

		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		if (testList.isEmpty()) {
			testList.add(new Long(2));// last doc id
			testList.add(new Long(1));// store doc frequence in second position
			testList.add(doc_id);// doc id
			testList.add(new Long(0)); // term freq
			testList.add(new Long(1)); // term rank wdt
		}

		// if(this.posting_list.get(term).isEmpty()){
		// this.posting_list.get(term).add(new Long(2));//last doc id
		// this.posting_list.get(term).add(new Long(1));// store doc frequence
		// in second position
		// this.posting_list.get(term).add(doc_id);//doc id
		// this.posting_list.get(term).add(new Long(0)); // term freq
		// this.posting_list.get(term).add(new Long(1)); // term rank
		// }

		if (testList.get(testList.get(0).intValue()) != doc_id) {
			testList.set(1, testList.get(1) + 1); // update doc freq
			testList.add(doc_id); // add new doc id
			testList.set(0, new Long(testList.size() - 1));// update the
															// position of last
															// doc id
			testList.add(new Long(0)); // term freq
			testList.add(new Long(1)); // term rank
		}

		// if(this.posting_list.get(term).get(this.posting_list.get(term).get(0).intValue())
		// != doc_id){
		// this.posting_list.get(term).set(1,
		// this.posting_list.get(term).get(1)+1); // update doc freq
		// this.posting_list.get(term).add(doc_id); // add new doc id
		// this.posting_list.get(term).set(0, new
		// Long(this.posting_list.get(term).size()-1));// update the position of
		// last doc id
		// this.posting_list.get(term).add(new Long(0)); // term freq
		// this.posting_list.get(term).add(new Long(1)); // term rank
		// }

		testList.add(position);
		int freq_pos = (testList.get(0)).intValue() + 1;
		testList.set(freq_pos, testList.get(freq_pos) + 1);

		//
		// this.posting_list.get(term).add(position);
		// int freq_pos = (this.posting_list.get(term).get(0)).intValue()+1;
		// this.posting_list.get(term).set(freq_pos,
		// this.posting_list.get(term).get(freq_pos)+1 );
	}

	Long[] getDocuments(String term) {
		Long[] documents = new Long[0];
		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		// System.out.println("inside getDocuments");
		if (testList != null) {
			// System.out.println("inside test list not null");
			long doc_frequency = testList.get(1); // get number of documents
													// with that term
			documents = new Long[(int) doc_frequency];
			int doc_id_pos = 2;
			for (int i = 0; i < doc_frequency; i++) {
				documents[i] = testList.get(doc_id_pos);
				doc_id_pos = doc_id_pos + testList.get(doc_id_pos + 1).intValue() + 3;
			}
			// System.out.println("done accumulationg documents");
			// System.out.println("After done accumulationg documents 1 : length
			// : "+documents.length + Arrays.asList(documents));
		}
		System.out.println("After done accumulationg documents :"+term);
		return documents;
	}

	Long[] getTermFreq(String term) {
		Long[] term_freq_array = new Long[0];
		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		// System.out.println("inside getDocuments");
		if (testList != null) {
			// System.out.println("inside test list not null");
			long doc_frequency = testList.get(1); // get number of documents
													// with that term

			term_freq_array = new Long[(int) doc_frequency*2];
			int doc_id_pos = 2;
			int term_freq_pos = 3;
			System.out.println(testList);
			for (int i = 0; i < term_freq_array.length; i++) {
				term_freq_array[i] = testList.get(doc_id_pos);
				i++;
				term_freq_array[i] = testList.get(term_freq_pos);
				doc_id_pos = doc_id_pos + testList.get(doc_id_pos + 1).intValue() + 3;
				term_freq_pos = term_freq_pos + testList.get(term_freq_pos + 1).intValue() + 3;
			}
			//System.out.println("done accumulationg term frequencies of documents!!");
			// System.out.println("After done accumulationg documents 1 : length
			// : "+documents.length + Arrays.asList(documents));
		}
		// System.out.println("After done accumulationg documents 2 : "+
		// Arrays.asList(documents));
		return term_freq_array;
	}
	
	Long[] setTermFreq(String term,Long [] a) {
		Long[] term_freq_array = new Long[0];
		Long[] only_term_freq_array = new Long[0];
		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		// System.out.println("inside getDocuments");
		if (testList != null) {
//			 System.out.println("inside test list not null");
//			 get number of documents with that term
			long doc_frequency = testList.get(1); 
											

			term_freq_array = new Long[(int) doc_frequency*2];
			only_term_freq_array = new Long[(int) doc_frequency];
			int doc_id_pos = 2;
			int term_freq_pos = 3;
			System.out.println(testList);
			for (int i = 0; i < term_freq_array.length; i++) {
				term_freq_array[i] = testList.get(doc_id_pos);
				i++;
				term_freq_array[i] = testList.get(term_freq_pos);
				doc_id_pos = doc_id_pos + testList.get(doc_id_pos + 1).intValue() + 3;
				term_freq_pos = term_freq_pos + testList.get(term_freq_pos + 1).intValue() + 3;
			}
//			System.out.println("done accumulationg term frequencies of documents!!");
//			 System.out.println("After done accumulationg documents 1 : length
//			 : "+documents.length + Arrays.asList(documents));
		}
//		 System.out.println("After done accumulationg documents 2 : "+
//		 Arrays.asList(documents));
		return term_freq_array;
	}

	Long[] getPostings(String term) {
		Long positions[];
		ArrayList<Long> list_of_positions = new ArrayList<>();
		ArrayList<Long> postings = this.posting_list.get(term);
//		 get the index  if next document id
		int doc_id_skip_index = 5 + postings.get(3).intValue(); 

		for (int i = 5; i < postings.size(); i++) {
			if (i == doc_id_skip_index) {
				// get next doc id position
				doc_id_skip_index = i + postings.get(i + 1).intValue() + 3; 
				i += 2;
				continue;
			}
			list_of_positions.add(postings.get(i));

		}
		positions = list_of_positions.toArray(new Long[list_of_positions.size()]);
		return positions;
	}

	Long[] getPositions(String term, Long document) {
		Long positions[] = new Long[0];
		ArrayList<Long> postings = this.posting_list.get(term);
		if (postings != null) {
//			 position of first document ID
			int i = 2; 
//			 get the starting position of document ID
			while (i < postings.size()) { 
				if (postings.get(i).equals(document))
					break;
				i = i + 3 + postings.get(i + 1).intValue();
			}
			if (i < postings.size()) {
				positions = new Long[postings.get(i + 1).intValue()];
//				 get index of first desired position
				i = i + 3; 
				for (int j = 0; j < positions.length; j++) {
					positions[j] = postings.get(i);
					i++;
				}
			}
		}
		return positions;
	}

	Set<String> keySet() {
		return this.posting_list.keySet();
	}

	public boolean containsKey(String term) {
		if (this.posting_list.containsKey(term))
			return true;
		else
			return false;
	}
}
