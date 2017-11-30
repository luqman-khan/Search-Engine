import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class QueryProcessor {

	private int near_k = 1;
	private boolean near_k_flag = false;
	private Long[] final_file_list;
	protected InvertedIndex inverted_index;
	protected DiskInvertedIndex disk_inverted_index;
	protected IndexWriter disk_index_writer;
	protected Object index;
	private PostingData posting_data = new PostingData();
	private String folder;

	/**
	 * constructor to initialize the indexer object with the specified directory
	 */
	QueryProcessor(Path directory) {
		inverted_index = new InvertedIndex(directory);
		disk_inverted_index = new DiskInvertedIndex(directory.toString());
		disk_index_writer = new IndexWriter(directory.toString());
		folder = directory.toString();
	}

	/**
	 * Function to process query and parse the query string into different type
	 * of queries like or,and phrase etc.
	 */
	protected String processQuery(String input) {

		String result_string = "";

		// regx to match the query string with different type of query forms
		Pattern near_pattern = Pattern.compile("(.+)(\\s+)near(/)([1-9]([0-9]*))(\\s+)(.+)");
		Matcher near_matcher = near_pattern.matcher(input);
		Pattern stem_pattern = Pattern.compile("^:stem(\\s+)(.+)$");
		Matcher stem_matcher = stem_pattern.matcher(input);
		Pattern vocab_pattern = Pattern.compile("(\\s*):vocab(\\s*)");
		Matcher vocab_matcher = vocab_pattern.matcher(input);

		if (vocab_matcher.find()) { // to output vocab list
			result_string = inverted_index.indexPrint();
			return result_string;
		} else if (stem_matcher.find()) { // get a stemmed word
			result_string = new Stemmer().processWord(input.split(":stem(\\s+)")[1]);
			return result_string;
		} else if (near_matcher.find()) { // to calculate near k of set of words
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/[1-9]([0-9])*", "");
			near_k_flag = true;
		}
		final_file_list = getDocForQuery(input);
		result_string = getOutputString();
		return result_string;
	}

	/**
	 * splits the query on evry '+' then every 'phrase' ("") then every space
	 * and perform respective operations
	 */
	Long[] getDocForQuery(String query) {
		String[] or_query_array = query.trim().split("[+]+");
		Long[] phrase_and_docs = new Long[0];
		Long[] all_query_docs = new Long[0];

		for (int i = 0; i < or_query_array.length; i++) { // for each OR part of
															// the query
			boolean phrase_flag = false, and_flag = false;
			if (!or_query_array[i].isEmpty() || !or_query_array[i].equals("[ ]*")) {
				String or_query_part = or_query_array[i];
				Pattern phrase_pattern = Pattern.compile("\"([^\"]*)\"");
				Matcher phrase_matcher = phrase_pattern.matcher(or_query_part);
				Long[] phrase_docs = new Long[0];
				while (phrase_matcher.find()) { // for each phrase
					String[] phrase_array = phrase_matcher.group(1).trim().split("[ ]+|-+");
					for (int j = 0; j < phrase_array.length; j++)
						phrase_array[j] = new Stemmer().processWord(phrase_array[j]);
					Long[] each_phrase_docs;
					posting_data = disk_inverted_index.getPostings(phrase_array[0]);
					each_phrase_docs = posting_data.docIdScoreHash.keySet().toArray(new Long[posting_data.docIdScoreHash.keySet().size()]);

					if (phrase_flag == false) {
						phrase_flag = true;
						phrase_docs = each_phrase_docs;
					}

					for (int j = 1; j < phrase_array.length; j++) {
						posting_data = disk_inverted_index.getPostings(phrase_array[j]);
						each_phrase_docs = intersect(each_phrase_docs, posting_data.docIdScoreHash.keySet().toArray(new Long[posting_data.docIdScoreHash.keySet().size()]));
					}

					phrase_docs = intersect(phrase_docs, each_phrase_docs);
					phrase_docs = checkNearK(phrase_docs, phrase_array, 1);
				}
				String[] and_array = or_query_part.split("\"([^\"]*)\"");
				Long[] and_docs = new Long[0];
				for (int j = 0; j < and_array.length; j++) { // for each AND

					String[] and_terms = and_array[j].trim().split("[ ]+|[-]+");
					for (int k = 0; k < and_terms.length; k++)
						and_terms[k] = new Stemmer().processWord(and_terms[k]);
					for (String term : and_terms) {
						if (!term.isEmpty()) {
							if (and_flag == false) { // run only on first term of and query
								posting_data = disk_inverted_index.getPostings(term);
								and_docs = union(and_docs, posting_data.docIdScoreHash.keySet().toArray(new Long[posting_data.docIdScoreHash.keySet().size()]));
								and_flag = true;
								continue;
							}
							posting_data = disk_inverted_index.getPostings(term);
							and_docs = intersect(and_docs, posting_data.docIdScoreHash.keySet().toArray(new Long[posting_data.docIdScoreHash.keySet().size()]));
						}
					}
					if (near_k_flag) {
						and_docs = checkNearK(and_docs, and_terms, near_k);
						near_k_flag = false;
					}
				}
				if (phrase_flag && and_flag) // intersect all AND and phrase document sets
					phrase_and_docs = intersect(phrase_docs, and_docs);
				else if (phrase_flag)
					phrase_and_docs = phrase_docs;
				else if (and_flag)
					phrase_and_docs = and_docs;
			}
			all_query_docs = union(all_query_docs, phrase_and_docs); // union all ORs
		}

		return all_query_docs;
	}

	/**
	 * takes two array and intersect them
	 */
	private Long[] intersect(Long[] phrase1_docs, Long[] phrase2_docs) {
		Long[] combine_phrase_docs = new Long[phrase1_docs.length];
		int j = 0, k = 0, i = 0;

		while (j < phrase1_docs.length && k < phrase2_docs.length) {
			if (phrase1_docs[j] == null || phrase2_docs[k] == null)
				break;
			if (phrase1_docs[j].equals(phrase2_docs[k])) {
				combine_phrase_docs[i] = phrase1_docs[j];i++;j++;k++; // copy doc id
			} else if (phrase1_docs[j] < phrase2_docs[k]) {
				j ++;
			} else if (phrase1_docs[j] > phrase2_docs[k]) {
				k ++;
			}
		}
		return combine_phrase_docs;
	}

	/**
	 * takes two array and returns union of them
	 */

	private Long[] union(Long[] phrase1_docs, Long[] phrase2_docs) {
		// System.out.println("inside union");
		Long[] combine_phrase_docs = new Long[phrase1_docs.length + phrase2_docs.length];
		int j = 0, k = 0, i = 0;
		while (j < phrase1_docs.length || k < phrase2_docs.length) {
			if ((j < phrase1_docs.length && k < phrase2_docs.length)
					&& (phrase1_docs[j] != null && phrase2_docs[k] != null)
					&& phrase1_docs[j].equals(phrase2_docs[k])) {
				combine_phrase_docs[i] = phrase1_docs[j];i++;j++;k++; // save the document id
			} else if ((k >= phrase2_docs.length && phrase1_docs.length > j)
					|| (phrase1_docs.length > j && phrase1_docs[j] < phrase2_docs[k])) {
				combine_phrase_docs[i] = phrase1_docs[j];i++;j++; // save the document id
			} else if ((j >= phrase1_docs.length && phrase2_docs.length > k)
					|| (phrase2_docs.length > k && phrase1_docs[j] > phrase2_docs[k])) {
				combine_phrase_docs[i] = phrase2_docs[k];i++;k++; // save the document id
			}
		}
		return combine_phrase_docs;
	}

	/**
	 * returns an array of documents with desired near k value
	 */

	private Long[] checkNearK(Long[] document_score_list, String[] word_list, int k) {
		Long[] near_k_doc_list = new Long[0];
		Long document;
		for (int d = 0; d < document_score_list.length; d ++) {
			document = document_score_list[d];
			if (document != null) {
				String first_word = word_list[0];

				ArrayList<Long> first_word_positions;
				first_word_positions = disk_inverted_index.getPostings(first_word).docIdPositionsHash.get(document);

				for (Long first_word_position : first_word_positions) {
					int i = 1;
					for (; i < word_list.length; i++) {
						ArrayList<Long> word_positions = new ArrayList<>();
						for(ArrayList<Long> l : disk_inverted_index.getPostings(word_list[i]).docIdPositionsHash.values())
							word_positions.addAll(l);
						if (!contains(word_positions, first_word_position + i, k)) {
							break;
						}
					}
					if (i == word_list.length) {
						Long[] new_document = { document };
						near_k_doc_list = union(near_k_doc_list, new_document);
					}
				}
			}
		}
		return near_k_doc_list;
	}

	/**
	 * check if the array contains respective term
	 */

	private boolean contains(ArrayList<Long> word_positions, Long next_position, int k) {
		for (int i = 0; i <= word_positions.size(); i++) {
			try {
				if (word_positions.get(i) > next_position + new Long(k - 1))
					break;
				if ((word_positions.get(i) >= next_position && word_positions.get(i) <= (next_position + new Long(k - 1)))
						|| (word_positions.get(i) >= next_position - new Long(1 + k)
								&& word_positions.get(i) <= next_position - new Long(2)))
					return true;
			} catch (Exception e) {
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
		int file_count = 0;
		if (final_file_list.length > 0 && final_file_list[0] != null) {
			Long file_number;
			Stream<String> lines;
			for (int i = 0; i < final_file_list.length; i ++) {
				try {
					lines = Files.lines(Paths.get(folder+"\\FileIdMap.bin"));
					file_number = final_file_list[i];
					if (file_number != null) {
						file_count++;
						output_string = output_string + lines.skip(file_number).findFirst().get()+"\n";
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			output_string += "Number of results : " + file_count + "\n";
		} else {
			output_string = "NO RESULT FOUND.......\n";
		}
		return output_string;
	}

}
