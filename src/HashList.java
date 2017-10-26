import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class HashList {

	HashMap<String, ArrayList<Long>> posting_list;
	int doc_id_pos, term_freq_pos;
	
	HashList(){
		posting_list = new HashMap<>();
	}
	
	protected void add(Long doc_id, String term, long position ){

		if(!this.posting_list.containsKey(term))
			this.posting_list.put(term, new ArrayList<Long>());

		if(this.posting_list.get(term).isEmpty()){
			this.posting_list.get(term).add(new Long(2));//last doc id
			this.posting_list.get(term).add(new Long(1));// store doc frequence in second position
			this.posting_list.get(term).add(doc_id);//doc id
			this.posting_list.get(term).add(new Long(0)); // term freq
			this.posting_list.get(term).add(new Long(1)); // term rank
		}
		if(this.posting_list.get(term).get(this.posting_list.get(term).get(0).intValue()) != doc_id){
			this.posting_list.get(term).set(1, this.posting_list.get(term).get(1)+1); // update doc freq
			this.posting_list.get(term).add(doc_id); // add new doc id
			this.posting_list.get(term).set(0, new Long(this.posting_list.get(term).size()-1));// update the position of last doc id
			this.posting_list.get(term).add(new Long(0)); // term freq
			this.posting_list.get(term).add(new Long(1)); // term rank
		}
		this.posting_list.get(term).add(position);
		int freq_pos = (this.posting_list.get(term).get(0)).intValue()+1;
		this.posting_list.get(term).set(freq_pos, this.posting_list.get(term).get(freq_pos)+1 );
	}
	
	void print(){
		for(String key : this.posting_list.keySet()){
		}
	}
	
	Long[] getDocuments(String term){
		Long[] documents = new Long[0];
		if(this.posting_list.get(term) != null){
			long doc_frequency = this.posting_list.get(term).get(1); // get number of documents with that term
			documents = new Long[(int)doc_frequency];
			int doc_id_pos = 2;
			for(int i = 0 ;i<doc_frequency; i++){
				documents[i]=this.posting_list.get(term).get(doc_id_pos);
				doc_id_pos = doc_id_pos + this.posting_list.get(term).get(doc_id_pos+1).intValue() + 3;
			}
		}
		return documents;
	}
	
	Long [] getPostings(String term){
		Long positions [];
		ArrayList <Long> list_of_positions = new ArrayList<>();
		ArrayList<Long> postings = this.posting_list.get(term);
		int doc_id_skip_index = 5 + postings.get(3).intValue(); // get the index if next document id
		
		for(int i = 5; i<postings.size(); i++){
			if(i==doc_id_skip_index){
				doc_id_skip_index = i+postings.get(i+1).intValue()+3; //get next doc id position
				i+=2;
				continue;
			}
			list_of_positions.add(postings.get(i));
			 
		}
		positions = list_of_positions.toArray(new Long[list_of_positions.size()]);
		return positions;
	}
	
	Long [] getPositions(String term, Long document){
		Long positions [] = new Long[0];
		ArrayList<Long> postings = this.posting_list.get(term);
		if(postings!=null){
			int i=2; // position of first document ID
			while(i<postings.size() && postings.get(i)!=document){ //get the starting position of document ID
				i = i+3+postings.get(i+1).intValue();
			}
			if(i<postings.size()){
				positions = new Long[postings.get(i+1).intValue()];
				i = i + 3; // get index of first desired position
				for(int j=0; j<positions.length; j++){
					positions[j] = postings.get(i);
					i++;
				}
			}
		}
		return positions;
	}
	
	 Set<String> keySet(){
		return this.posting_list.keySet();
	}

	public boolean containsKey(String term) {
		if(this.posting_list.containsKey(term))
			return true;
		else
			return false;
	}
}
