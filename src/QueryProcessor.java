import java.nio.file.Path;
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

	/**
	 * constructor to initialize the indexer object with the specified directory
	 */
	QueryProcessor(Path directory) {
		inverted_index = new InvertedIndex(directory);
	}

	/**
	 * Function to process query and parse the query string into different type
	 * of queries like or,and phrase etc.
	 */
	protected void processQuery(String input, JTextArea result_txt) {


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
			inverted_index.indexPrint(result_txt);
			return;
		} else if (stem_matcher.find()) { // get a stemmed word
			result_txt.setText(new Stemmer().processWord(input.split(":stem(\\s+)")[1]));
			return;
		}else if (set_near_matcher.find()) { // set the value of near k
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			result_txt.setText("Near K is set to : " + near_k);
			return;
		} else if (near_matcher.find()) { // to calculate near k of set of words
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/[1-9]([0-9])*", "");
			near_k_flag = true;
		}
		final_file_list = getDocForQuery(input);
		printResults(result_txt);
	}
	Long [] getDocForQuery(String query){
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
					Long [] each_phrase_docs = inverted_index.pos_hash_list.getDocuments(phrase_array[0]);
					
					if(phrase_flag == false){
						phrase_flag = true;
						phrase_docs = each_phrase_docs;
					}
					
					for(int j=1; j<phrase_array.length; j++){ 						//for each term in phrase
						each_phrase_docs = intersect(each_phrase_docs, inverted_index.pos_hash_list.getDocuments(phrase_array[j]));
					}
					
					phrase_docs = intersect(phrase_docs,each_phrase_docs);
					phrase_docs = checkNearK(phrase_docs,phrase_array, 1);
				}
				String [] and_array = or_query_part.split("\"([^\"]*)\"");
				Long [] and_docs = new Long[0];
				for(int j=0; j<and_array.length;j++){ 								// for each AND
					
					String []and_terms = and_array[j].trim().split("[ ]+|-+");
					for(int k=0; k<and_terms.length; k++)
						and_terms[k] = new Stemmer().processWord(and_terms[k]);
					for(String term : and_terms){
						if(!term.isEmpty()){
							if(and_flag==false){
								System.out.print(i);
								and_docs = inverted_index.pos_hash_list.getDocuments(term);
								and_flag=true;
								printArray(and_docs);
								continue;
							}
							System.out.print(i);
							and_docs = intersect(and_docs,inverted_index.pos_hash_list.getDocuments(term));
						}
					}
					if(near_k_flag){
						and_docs = checkNearK(and_docs,and_terms, near_k);
						near_k_flag = false;
					}
				}
				if(phrase_flag && and_flag)											//intersect all AND and phrase document sets
					phrase_and_docs = intersect(phrase_docs,and_docs);
				else if(phrase_flag)
					phrase_and_docs = phrase_docs;
				else if(and_flag)
					phrase_and_docs = and_docs;
				
				all_query_docs = union(all_query_docs,phrase_and_docs); 			// union all ORs
			}
		}
		return all_query_docs;
	}
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
	
	private Long[] union(Long[] phrase1_docs, Long[] phrase2_docs) {
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
		return combine_phrase_docs;
	}

	/**
	 * processes the AND query string
	 */
//	protected Long [] andQuery(String input) {
//		String temp_word_list[] = input.trim().split("\\s+");
//		for (int i = 0; i < temp_word_list.length; i++)
//			temp_word_list[i] = new Stemmer().processWord(temp_word_list[i]);
//
//		for (int j = 0; j < temp_word_list.length; j++) {
//			if (inverted_index.pos_hash_list.containsKey(temp_word_list[j]))
//				final_file_list = addFileList(final_file_list,
//						inverted_index.pos_hash_list.getDocuments(temp_word_list[j]));
//		}
//		for (Iterator<Long> j = final_file_list.keySet().iterator(); j.hasNext();)
//			if (final_file_list.get(j.next()) < temp_word_list.length)
//				j.remove();
////		final_file_list = checkNearK(final_file_list, temp_word_list);
//		return final_file_list;
//	}

	/**
	 * processes the OR query string
	 */
//	protected Long [] orQuery(String input) {
//		or_word_list = input.split("\\+");
//
//		for (int i = 0; i < or_word_list.length; i++) {
//			if (or_word_list[i].trim().split(" ").length <= 0) {
//				if (inverted_index.pos_hash_list.containsKey(new Stemmer().processWord(or_word_list[i])))
//					final_file_list = addFileList(final_file_list,
//							inverted_index.pos_hash_list.getDocuments(new Stemmer().processWord(or_word_list[i])));
//			} else {
//				Long [] temp_doc_list;
//				String temp_word_list[] = or_word_list[i].trim().split(" ");
//				near_k = 1;
//				
//				//store stemmed query words in temp_word_list
//				for (int k = 0; k < temp_word_list.length; k++)
//					temp_word_list[k] = new Stemmer().processWord(temp_word_list[k]);
//				
//				//if index has that word then get all documents with that word and add to temp_doc_list
//				for (int j = 0; j < temp_word_list.length; j++) {
//					if (inverted_index.pos_hash_list.containsKey(temp_word_list[j]))
//						temp_doc_list = addFileList(temp_doc_list,
//								inverted_index.pos_hash_list.getDocuments(temp_word_list[j]));
//				}
//				
////				for (Iterator<Long> j = temp_doc_list.keySet().iterator(); j.hasNext();)
//				for(int j =0; j< temp_doc_list.length; i++) 
//					if (temp_doc_list.get(j.next()) < temp_word_list.length)
//						j.remove();
//				
//				temp_doc_list = checkNearK(temp_doc_list, temp_word_list);
//				final_file_list = addFileList(final_file_list, temp_doc_list);
//			}
//		}
//		return final_file_list;
//	}

	/**
	 * checks for near k and accordingly discard the entry if the difference
	 * between every two word is greater than near_k
	 */
//	private HashMap<Long, Long> checkNearK(HashMap<Long, Long> current_file_list, String temp_word_list[]) {
//		for (Iterator<Long> i = current_file_list.keySet().iterator(); i.hasNext();) {
//			long file_number = i.next();
//			iteratorloop: for (int j = 1; j < temp_word_list.length; j++) {
//				String pos1_str = inverted_index.pos_dictionary.get(temp_word_list[j - 1]).get(file_number);
//				String pos2_str = inverted_index.pos_dictionary.get(temp_word_list[j]).get(file_number);
//				int pos1[] = Arrays.stream(pos1_str.substring(0, pos1_str.length() - 1).split(",")).map(String::trim)
//						.mapToInt(Integer::parseInt).toArray();
//				int pos2[] = Arrays.stream(pos2_str.substring(0, pos2_str.length() - 1).split(",")).map(String::trim)
//						.mapToInt(Integer::parseInt).toArray();
//				outerloop: for (int k = 0; k < pos1.length; k++) {
//					for (int l = 0; l < pos2.length; l++) {
//						if ((pos2[l] - pos1[k]) <= near_k && (pos2[l] - pos1[k]) > 0) {
//							break outerloop;
//						}
//						if (k == pos1.length - 1 && l == pos2.length - 1) { // discard if less than near k
//							i.remove();
//							break iteratorloop;
//						}
//					}
//				}
//			}
//		}
//		return current_file_list;
//	}
	
	private Long[] checkNearK(Long [] document_list, String[] word_list, int k){
		Long []near_k_doc_list = new Long[0];
		for(Long document : document_list){
			String first_word = word_list[0];
			
			Long [] first_word_positions = inverted_index.pos_hash_list.getPositions(first_word,document);
			
			for(Long first_word_position :  first_word_positions){
				int i=1;
				for(; i<word_list.length; i++ ){
					System.out.println("next word is "+word_list[i]);
					Long [] word_positions = inverted_index.pos_hash_list.getPositions(word_list[i],document);
					if(! contains(word_positions,first_word_position+i,k)){
						printArray(word_positions);
						System.out.println("are next word position");
						System.out.println("next word position "+ (first_word_position+i) + " k value: "+ k);
						break;
					}
				}
				System.out.println(i==word_list.length);
				if(i==word_list.length){
					Long [] new_document = {document};
					near_k_doc_list = union(near_k_doc_list, new_document);
				}
			}
		}
		printArray(near_k_doc_list);
		return near_k_doc_list;
	}

	/**
	 * support function, takes two hashmaps and performs a union
	 */
//	private Long [] addFileList(Long [] old_doc_list, Long [] current_doc_list) {
//		Long [] new_doc_list = new Long [old_doc_list.length+current_doc_list.length];
//		int j=0, k=0;
//		for(int i = 0; i<current_doc_list.length; i++){
//			if(old_doc_list[j]<current_doc_list[k] && j<old_doc_list.length){
//				new_doc_list[i]=old_doc_list[j];
//				j++;
//			}
//			else if (old_doc_list[j]>current_doc_list[k] && k<current_doc_list.length){
//				new_doc_list[i] = current_doc_list[k];
//				k++;
//			}
//			else if(old_doc_list[j]==current_doc_list[k]){
//				new_doc_list[i] = current_doc_list[k];
//				j++; k++;
//			}
//		}
//		return new_doc_list;
//	}

	private boolean contains(Long[] word_positions, Long next_position, int k) {
		System.out.println("i starts : "+(next_position.intValue()-(k+1)));
		System.out.println("i ends : "+(next_position.intValue()+k-1));
		for(int i=0; i<=word_positions.length; i++){
			try{
				System.out.println("current word pos "+word_positions[i]+" next word pos "+next_position);
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
	private void printResults(JTextArea result_txt) {
		String output_string = "";
		if (final_file_list.length > 0) {
			for (Long file_number : final_file_list) {
				if(file_number != null)
					output_string = output_string + inverted_index.files.get(file_number) + "\n";
			}
//			output_string = output_string+"\n count : "+ final_file_list.keySet().size();
			result_txt.setText(output_string);
		} else {
			result_txt.setText("NO RESULT FOUND.......\n");
		}
	}
	void printArray(Long [] array){
		for(Long a : array)
			System.out.println(a);
	}
	void printArray(String [] array){
		for(String a : array)
			System.out.println(a);
	}

}
