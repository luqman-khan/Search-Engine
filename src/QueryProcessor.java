import java.nio.file.Path;
import java.util.Arrays;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;

public class QueryProcessor {

	private int near_k = 1;
	private boolean near_k_flag = false;
//	private HashMap<Long, Long> final_file_list = new HashMap<Long, Long>();
	private Long [] final_file_list;
//	private String[] or_word_list;
	protected InvertedIndex inverted_index;
	protected DiskInvertedIndex disk_inverted_index;
	protected IndexWriter disk_index_writer;
	protected Object index;

	/**
	 * constructor to initialize the indexer object with the specified directory
	 */
	QueryProcessor(Path directory) {
		inverted_index = new InvertedIndex(directory);
		disk_inverted_index = new DiskInvertedIndex(directory.toString());
		disk_index_writer = new IndexWriter(directory.toString());
	}

	/**
	 * Function to process query and parse the query string into different type
	 * of queries like or,and phrase etc.
	 */
	protected String processQuery(String input, boolean mode) {

		String result_string = "";
		
		// regx to match the query string with different type of query forms
		Pattern near_pattern = Pattern.compile("(.+)(\\s+)near(/)([1-9]([0-9]*))(\\s+)(.+)");
		Matcher near_matcher = near_pattern.matcher(input);
		Pattern stem_pattern = Pattern.compile("^:stem(\\s+)(.+)$");
		Matcher stem_matcher = stem_pattern.matcher(input);
		Pattern set_near_pattern = Pattern.compile(":near(/)([1-9]([0-9])*)");
		Matcher set_near_matcher = set_near_pattern.matcher(input);
		Pattern vocab_pattern = Pattern.compile("(\\s*):vocab(\\s*)");
		Matcher vocab_matcher = vocab_pattern.matcher(input);

		if (vocab_matcher.find()) { // to output vocab list
			result_string = inverted_index.indexPrint();
			return result_string;
		} else if (stem_matcher.find()) { // get a stemmed word
			result_string = new Stemmer().processWord(input.split(":stem(\\s+)")[1]);
			return result_string;
		}else if (set_near_matcher.find()) { // set the value of near k
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			result_string = "Near K is set to : " + near_k;
			return result_string;
		} else if (near_matcher.find()) { // to calculate near k of set of words
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/[1-9]([0-9])*", "");
			near_k_flag = true;
		}
		final_file_list = getDocForQuery(input, mode);
		result_string = getOutputString();
		return result_string;
	}
	
	/**
	 * splits the query on evry '+' then every 'phrase' ("") then every space and perform respective operations
	 */
	Long [] getDocForQuery(String query, boolean mode){
		String [] or_query_array = query.trim().split("[+]+");
		Long [] phrase_and_docs = new Long[0];
		Long [] all_query_docs = new Long[0] ;
		
		for(int i=0; i<or_query_array.length; i++){ 								// for each OR part of the query
			boolean phrase_flag=false, and_flag=false;
			if(!or_query_array[i].isEmpty() || !or_query_array[i].equals("[ ]*")){
				String or_query_part = or_query_array[i];
				Pattern phrase_pattern = Pattern.compile("\"([^\"]*)\"");
				Matcher phrase_matcher = phrase_pattern.matcher(or_query_part);
				Long [] phrase_docs = new Long[0];
				while(phrase_matcher.find()){ 										// for each phrase
					String [] phrase_array = phrase_matcher.group(1).trim().split("[ ]+|-+");
					for(int j=0; j<phrase_array.length; j++)
						phrase_array[j] = new Stemmer().processWord(phrase_array[j]);
					Long [] each_phrase_docs;
					if(mode)						// check if user wants to search by disk index or memory index
						each_phrase_docs = inverted_index.pos_hash_list.getDocuments(phrase_array[0]);
					else
						each_phrase_docs = disk_inverted_index.getDocuments(phrase_array[0]);
					
					if(phrase_flag == false){
						phrase_flag = true;
						phrase_docs = each_phrase_docs;
					}
					
					for(int j=1; j<phrase_array.length; j++){ 						//for each term in phrase
						if(mode)						// check if user wants to search by disk index or memory index
							each_phrase_docs = intersect(each_phrase_docs, inverted_index.pos_hash_list.getDocuments(phrase_array[j]));
						else
							each_phrase_docs = intersect(each_phrase_docs, disk_inverted_index.getDocuments(phrase_array[j]));
					}
					
					phrase_docs = intersect(phrase_docs,each_phrase_docs);
//					System.out.println("reached near k in phrase");
					phrase_docs = checkNearK(phrase_docs,phrase_array, 1, mode);
				}
				String [] and_array = or_query_part.split("\"([^\"]*)\"");
				Long [] and_docs = new Long[0];
				for(int j=0; j<and_array.length;j++){ 								// for each AND
					
					String []and_terms = and_array[j].trim().split("[ ]+|[-]+");
//					System.out.println("For each and terms "+Arrays.asList(and_terms));
					for(int k=0; k<and_terms.length; k++)
						and_terms[k] = new Stemmer().processWord(and_terms[k]);
					for(String term : and_terms){
						if(!term.isEmpty()){
//							System.out.println("reached inside is empty");
							if(and_flag==false){
//								System.out.println("reached before and doc query");
								if(mode)					// check if user wants to search by disk index or memory index
									and_docs = union(and_docs,inverted_index.pos_hash_list.getDocuments(term));
								else
									and_docs = union(and_docs,disk_inverted_index.getDocuments(term));
//								System.out.println("reached after and doc query");
								and_flag=true;
								continue;
							}
							if(mode)
								and_docs = intersect(and_docs,inverted_index.pos_hash_list.getDocuments(term));
							else
								and_docs = intersect(and_docs,disk_inverted_index.getDocuments(term));
						}
					}
//					System.out.println("---------------------AND DOCS ["+j+"]---------------------"+Arrays.asList(and_docs));
					if(near_k_flag){
						and_docs = checkNearK(and_docs,and_terms, near_k, mode);
//						System.out.println("reached near k in and");
						near_k_flag = false;
					}
				}
				if(phrase_flag && and_flag)											//intersect all AND and phrase document sets
					phrase_and_docs = intersect(phrase_docs,and_docs);
				else if(phrase_flag)
					phrase_and_docs = phrase_docs;
				else if(and_flag)
					phrase_and_docs = and_docs;
//				System.out.println("reached phrase and Union");
				
//				all_query_docs = union(all_query_docs,phrase_and_docs); 			// union all ORs
			}
//			System.out.println("reached or union");
			all_query_docs = union(all_query_docs,phrase_and_docs); 			// union all ORs
		}
//		System.out.println("reached return");
		return all_query_docs;
	}
	
	/**
	 * takes two array and intersect them
	 */
	private Long[] intersect(Long[] phrase1_docs, Long[] phrase2_docs) {
		Long [] combine_phrase_docs = new Long[phrase1_docs.length];
		int j=0, k=0, i=0;
		while(j<phrase1_docs.length&&k<phrase2_docs.length){
			if(phrase1_docs[j] == null || phrase2_docs[k] == null)
				break;
			else if(phrase1_docs[j]==phrase2_docs[k]){
				combine_phrase_docs[i]=phrase1_docs[j];
				i++;j++;k++;
			}
			else if(phrase1_docs[j]<phrase2_docs[k]){
				j++;
			}
			else if(phrase1_docs[j]>phrase2_docs[k]){
				k++;
			}
		}
		return combine_phrase_docs;
	}
	
	/**
	 * takes two array and returns union of them
	 */
	
	private Long[] union(Long[] phrase1_docs, Long[] phrase2_docs) {
//		System.out.println("inside union");
		Long [] combine_phrase_docs = new Long[phrase1_docs.length+phrase2_docs.length];
		int j=0, k=0, i=0;
		while(j<phrase1_docs.length || k<phrase2_docs.length){
			if((j<phrase1_docs.length && k<phrase2_docs.length) && phrase1_docs[j]==phrase2_docs[k]){
				combine_phrase_docs[i]=phrase1_docs[j];
				i++;j++;k++;
			}
			else if(k>=phrase2_docs.length || (phrase1_docs.length > j && phrase1_docs[j]<phrase2_docs[k])){
				combine_phrase_docs[i]=phrase1_docs[j];
				i++;j++;
			}
			else if(j>=phrase1_docs.length || (phrase2_docs.length > k && phrase1_docs[j]>phrase2_docs[k])){
				combine_phrase_docs[i]=phrase2_docs[k];
				i++;k++;
			}
		}
//		System.out.println("inside union output "+ Arrays.asList(combine_phrase_docs));
		return combine_phrase_docs;
	}
	
	/**
	 * returns an array of documents with desired near k value
	 */
	
	private Long[] checkNearK(Long [] document_list, String[] word_list, int k, boolean mode){
		Long []near_k_doc_list = new Long[0];
		for(Long document : document_list){
			String first_word = word_list[0];
			
			Long [] first_word_positions;
			
			if(mode)
				first_word_positions = inverted_index.pos_hash_list.getPositions(first_word,document);
			else
				first_word_positions = disk_inverted_index.getPositions(first_word,document);
			
			for(Long first_word_position :  first_word_positions){
				int i=1;
				for(; i<word_list.length; i++ ){
					Long [] word_positions = inverted_index.pos_hash_list.getPositions(word_list[i],document);
					if(mode)
						word_positions = inverted_index.pos_hash_list.getPositions(word_list[i],document);
					else
						word_positions = disk_inverted_index.getPositions(word_list[i],document);
					if(! contains(word_positions,first_word_position+i,k)){
						break;
					}
				}
				if(i==word_list.length){
					Long [] new_document = {document};
					near_k_doc_list = union(near_k_doc_list, new_document);
				}
			}
		}
		return near_k_doc_list;
	}
	
	/**
	 * check if the array contains respective term
	 */

	private boolean contains(Long[] word_positions, Long next_position, int k) {
		for(int i=0; i<=word_positions.length; i++){
			try{
				if(word_positions[i]>next_position+new Long(k-1))
					break;
				if(( word_positions[i]>=next_position && word_positions[i]<=(next_position+new Long(k-1)) )|| (word_positions[i]>=next_position-new Long(1+k) && word_positions[i]<= next_position-new Long(2)) )
					return true;
			}
			catch(Exception e){
				continue;
			}
		}
		return false;
	}

	/**
	 * prints the result to the output text field
	 */
	private String getOutputString() {
		String output_string = "";
		if (final_file_list.length > 0 && final_file_list[0] != null) {
			for (Long file_number : final_file_list) {
				if(file_number != null)
					output_string = output_string + inverted_index.files.get(file_number) + "\n";
			}
			output_string += "Number of results : "+final_file_list.length + "\n";
		} else {
			output_string = "NO RESULT FOUND.......\n";
		}
		return output_string;
	}

}
