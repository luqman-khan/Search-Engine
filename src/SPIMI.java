import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SPIMI {
	// [ last_doc_pos| doc_freq| doc_id| term_freq| term_rank| p| p2| p3|.....| doc_id| term_freq| term_rank| p1|p2|.....]
	private Long[] mergeIndex(Long [] index_a, Long [] index_b){
		Long merge_index[];
		if(index_a[index_a[0].intValue()].equals(index_b[2])){ 	//check if last docID of first index is equal to the first docId of second
			merge_index = new Long[(index_a.length+index_b.length-5)];	// -last_doc, -doc_freq, -doc_id. -term_freq, -term_rank, 
			List<Long> index_a_list = new ArrayList<Long>(Arrays.asList(index_a));
			List<Long> index_b_list = new ArrayList<Long> (Arrays.asList(Arrays.copyOfRange(index_b, 5, index_b.length)));
			index_a_list.addAll(index_b_list);
			merge_index = index_a_list.toArray(new Long[index_a_list.size()]);
			merge_index[1] += (index_b[1]-new Long(1)); // update the document frequency
			merge_index[merge_index[0].intValue()+1] += index_b[3];  // update the frequency of the last term in first index
			merge_index [0] = index_a.length + index_b[0] - new Long(5); 	// update last docid position 
			
			
		} else {
			merge_index = new Long[(index_a.length+index_b.length-2)];	// -last_doc, -doc_freq
			List<Long> index_a_list = new ArrayList<Long>(Arrays.asList(index_a));
			List<Long> index_b_list = new ArrayList<Long> (Arrays.asList(Arrays.copyOfRange(index_b, 2, index_b.length)));
			index_a_list.addAll(index_b_list);
			merge_index = index_a_list.toArray(new Long[index_a_list.size()]);
			merge_index[1] += index_b[1]; // update the document frequency
			merge_index [0] = index_a.length + index_b[0] - new Long(2); 	// update last docid position 
		}
		return merge_index;
	}
}
