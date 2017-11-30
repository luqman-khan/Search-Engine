import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class HashList {

	HashMap<String, ArrayList<Long>> posting_list;
	protected ArrayList<DocumentInfo> docInfoArray = new ArrayList<>();
	protected Long total_docs = new Long(0);
	protected Long total_term_count = new Long(0);
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

		if (!testList.get(testList.get(0).intValue()).equals(doc_id)) {
			testList.set(1, testList.get(1) + 1); // update doc freq
			testList.add(doc_id); // add new doc id
			testList.set(0, new Long(testList.size() - 1));// update the
															// position of last
															// doc id
			testList.add(new Long(0)); // term freq
			testList.add(new Long(1)); // term rank
		}

		testList.add(position);
		int freq_pos = (testList.get(0)).intValue() + 1;
		testList.set(freq_pos, testList.get(freq_pos) + 1);
	}

	Long[] getDocuments(String term) {
		Long[] documents = new Long[0];
		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		if (testList != null) {
			long doc_frequency = testList.get(1); // get number of documents
													// with that term
			if(term.equals("a"))
				System.out.println("Document frequency is : "+doc_frequency);
			documents = new Long[(int) doc_frequency];
			int doc_id_pos = 2;
			for (int i = 0; i < doc_frequency; i++) {
				documents[i] = testList.get(doc_id_pos);
				doc_id_pos = doc_id_pos + testList.get(doc_id_pos + 1).intValue() + 3;
			}
		}
		if(term.equals("a"))
			System.out.println("Document length is : "+documents.length);
		return documents;
	}
	
	
	Long[] getDocumentsScore(String term) {
		Long[] documentsScore = new Long[0];
		ArrayList<Long> testList;
		testList = this.posting_list.get(term);
		if (testList != null) {
			long doc_frequency = testList.get(1); // get number of documents
													// with that term
			documentsScore = new Long[(int) doc_frequency*2];
			int doc_id_pos = 2;
			int doc_score_pos;
			for (int i = 0; i < 2*doc_frequency; i++) {
				doc_score_pos = doc_id_pos + 2;
				documentsScore[i] = testList.get(doc_id_pos); 
				i++;
				documentsScore[i] = testList.get(doc_score_pos);
				doc_id_pos = doc_id_pos + testList.get(doc_id_pos + 1).intValue() + 3;
			}
		}
		return documentsScore;
	}
	
	
	
	protected void calculateWdt() {
		for(String term : this.keySet()){
			ArrayList<Long> postings = new ArrayList<Long>();
			postings = this.posting_list.get(term);
			if(postings!=null){
				long doc_frequency = postings.get(1);
				int doc_id_pos = 2;
				int term_freq_pos=0;
				int doc_term_rank_pos=0;
				Double wdt, wqt, ld;
				Long score;
				for (int i = 0; i < doc_frequency; i++) {
					term_freq_pos = doc_id_pos+1;
					doc_term_rank_pos = term_freq_pos+1;
					wdt = 1 + Math.log10(postings.get(term_freq_pos));
					wqt = Math.log10(total_docs/doc_frequency);
					ld = docInfoArray.get(postings.get(doc_id_pos).intValue()).ld;
					score = (new Double((wdt*wqt/ld)*1000000).longValue());	//as we do not store double value convert it to long
					postings.set(doc_term_rank_pos, score); 	//calculate score and store in tghe postings
					doc_id_pos = doc_id_pos + postings.get(doc_id_pos + 1).intValue() + 3;
				}
			}
		}
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
