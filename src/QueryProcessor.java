import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;

public class QueryProcessor {

	private int near_k = 1;
	private HashMap<Long, Long> final_file_list = new HashMap<Long, Long>();
	private String[] or_word_list;
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

		final_file_list = new HashMap<Long, Long>();

		// regx to match the query string with different type of query forms
		Pattern near_pattern = Pattern.compile("(.+)(\\s+)near(/)([1-9]([0-9]*))(\\s+)(.+)");
		Matcher near_matcher = near_pattern.matcher(input);
		Pattern stem_pattern = Pattern.compile("^:stem(\\s+)(.+)$");
		Matcher stem_matcher = stem_pattern.matcher(input);
		Pattern or_pattern = Pattern.compile("(((.+)(\\+)(.+))+)");
		Matcher or_matcher = or_pattern.matcher(input);
		Pattern set_near_pattern = Pattern.compile(":near(/)([1-9]([0-9])*)");
		Matcher set_near_matcher = set_near_pattern.matcher(input);
		Pattern phrase_pattern = Pattern.compile("((.*)(\"+)(.*))+");
		Matcher phrase_matcher = phrase_pattern.matcher(input);
		Pattern vocab_pattern = Pattern.compile("(\\s*):vocab(\\s*)");
		Matcher vocab_matcher = vocab_pattern.matcher(input);

		if (phrase_matcher.find()) // for phrase query
			near_k = 1;
		if (vocab_matcher.find()) { // to output vocab list
			inverted_index.indexPrint(result_txt);
			return;
		} else if (or_matcher.find()) { // to parse or queries
			final_file_list = orQuery(input);
		} else if (near_matcher.find()) { // to calculate near k of set of words
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/[1-9]([0-9])*", "");
			final_file_list = andQuery(input);
		} else if (set_near_matcher.find()) { // set the value of near k
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			result_txt.setText("Near K is set to : " + near_k);
			return;
		} else if (stem_matcher.find()) { // get a stemmed word
			result_txt.setText(new Stemmer().processWord(input.split(":stem(\\s+)")[1]));
			return;
		} else { // runs every time it is not a special query form
			final_file_list = andQuery(input);
		}
		printResults(result_txt);
	}

	/**
	 * processes the AND query string
	 */
	protected HashMap<Long, Long> andQuery(String input) {
		String temp_word_list[] = input.trim().split("\\s+");
		for (int i = 0; i < temp_word_list.length; i++)
			temp_word_list[i] = new Stemmer().processWord(temp_word_list[i]);

		for (int j = 0; j < temp_word_list.length; j++) {
			if (inverted_index.pos_dictionary.containsKey(temp_word_list[j]))
				final_file_list = addFileList(final_file_list,
						inverted_index.pos_dictionary.get(temp_word_list[j]).keySet());
		}
		for (Iterator<Long> j = final_file_list.keySet().iterator(); j.hasNext();)
			if (final_file_list.get(j.next()) < temp_word_list.length)
				j.remove();
		return final_file_list = checkNearK(final_file_list, temp_word_list);
	}

	/**
	 * processes the OR query string
	 */
	protected HashMap<Long, Long> orQuery(String input) {
		or_word_list = input.split("\\+");

		for (int i = 0; i < or_word_list.length; i++) {
			if (or_word_list[i].trim().split(" ").length <= 0) {
				if (inverted_index.pos_dictionary.containsKey(new Stemmer().processWord(or_word_list[i])))
					final_file_list = addFileList(final_file_list,
							inverted_index.pos_dictionary.get(new Stemmer().processWord(or_word_list[i])).keySet());
			} else {
				HashMap<Long, Long> temp_file_list = new HashMap<>();
				String temp_word_list[] = or_word_list[i].trim().split(" ");
				near_k = 1;
				for (int k = 0; k < temp_word_list.length; k++)
					temp_word_list[k] = new Stemmer().processWord(temp_word_list[k]);
				for (int j = 0; j < temp_word_list.length; j++) {
					if (inverted_index.pos_dictionary.containsKey(temp_word_list[j]))
						temp_file_list = addFileList(temp_file_list,
								inverted_index.pos_dictionary.get(temp_word_list[j]).keySet());
				}
				for (Iterator<Long> j = temp_file_list.keySet().iterator(); j.hasNext();)
					if (temp_file_list.get(j.next()) < temp_word_list.length)
						j.remove();
				temp_file_list = checkNearK(temp_file_list, temp_word_list);
				final_file_list = addFileList(final_file_list, temp_file_list.keySet());
			}
		}
		return final_file_list;
	}

	/**
	 * checks for near k and accordingly discard the entry if the difference
	 * between every two word is greater than near_k
	 */
	private HashMap<Long, Long> checkNearK(HashMap<Long, Long> current_file_list, String temp_word_list[]) {
		for (Iterator<Long> i = current_file_list.keySet().iterator(); i.hasNext();) {
			long file_number = i.next();
			iteratorloop: for (int j = 1; j < temp_word_list.length; j++) {
				String pos1_str = inverted_index.pos_dictionary.get(temp_word_list[j - 1]).get(file_number);
				String pos2_str = inverted_index.pos_dictionary.get(temp_word_list[j]).get(file_number);
				int pos1[] = Arrays.stream(pos1_str.substring(0, pos1_str.length() - 1).split(",")).map(String::trim)
						.mapToInt(Integer::parseInt).toArray();
				int pos2[] = Arrays.stream(pos2_str.substring(0, pos2_str.length() - 1).split(",")).map(String::trim)
						.mapToInt(Integer::parseInt).toArray();
				outerloop: for (int k = 0; k < pos1.length; k++) {
					for (int l = 0; l < pos2.length; l++) {
						if ((pos2[l] - pos1[k]) <= near_k && (pos2[l] - pos1[k]) > 0) {
							break outerloop;
						}
						if (k == pos1.length - 1 && l == pos2.length - 1) { // discard
																			// if
																			// difference
																			// is
																			// greater
																			// than
																			// near_k
																			// value
							i.remove();
							break iteratorloop;
						}
					}
				}
			}
		}
		return current_file_list;
	}

	/**
	 * support function, takes two hashmaps and performs a union
	 */
	private HashMap<Long, Long> addFileList(HashMap<Long, Long> old_file_hash, Set<Long> current_file_list) {
		for (Iterator<Long> i = current_file_list.iterator(); i.hasNext();) {
			long file_number = i.next();
			if (!old_file_hash.containsKey(file_number))
				old_file_hash.put(file_number, new Long(1));
			else
				old_file_hash.put(file_number, old_file_hash.get(file_number) + 1);
		}
		return old_file_hash;
	}

	/**
	 * prints the result to the output text field
	 */
	private void printResults(JTextArea result_txt) {
		String output_string = "";
		if (final_file_list.size() > 0) {
			for (Iterator<Long> i = final_file_list.keySet().iterator(); i.hasNext();) {
				long file_number = i.next();
				output_string = output_string + inverted_index.files.get(file_number) + "\n";

			}
//			output_string = output_string+"\n count : "+ final_file_list.keySet().size();
			result_txt.setText(output_string);
		} else {
			result_txt.setText("NO RESULT FOUND.......\n");
		}
	}

}
