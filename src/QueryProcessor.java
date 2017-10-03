import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;

public class QueryProcessor extends InvertedIndex {

	private int near_k = 1;
	private HashMap<Long, Long> final_file_list;
	String[] or_word_list;

	QueryProcessor(Path directory) {
		super(directory);
	}

	public void processQuery(String input, JTextArea result_txt) {

		final_file_list = new HashMap<Long, Long>();

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

		if (phrase_matcher.find())
			near_k = 1;
		if(vocab_matcher.find()){
			super.indexPrint(result_txt);
			return;
		}
		else if (or_matcher.find()) {
			orQuery(input);
		} else if (near_matcher.find()) {
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/[1-9]([0-9])*", "");
			andQuery(input);
		} else if (set_near_matcher.find()) {
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			result_txt.setText("Near K is set to : " + near_k);
			return;
		} else if (stem_matcher.find()) {
			result_txt.setText(new Stemmer().processWord(input.split(":stem(\\s+)")[1]));
			return;
		} else {
			andQuery(input);
		}
		printResults(result_txt);
	}

	private void printResults(JTextArea result_txt) {
		String output_string = "";
		if (final_file_list.size() > 0) {
			for (Iterator<Long> i = final_file_list.keySet().iterator(); i.hasNext();) {
				long file_number = i.next();
				output_string = output_string + super.files.get(file_number) +"\n";
				
			}
			result_txt.setText(output_string);
		} else {
			result_txt.setText("NO RESULT FOUND.......\n");
		}
	}

	private void andQuery(String input) {
		String temp_word_list[] = input.trim().split("\\s+");
		for (int i = 0; i < temp_word_list.length; i++)
			temp_word_list[i] = new Stemmer().processWord(temp_word_list[i]);

		for (int j = 0; j < temp_word_list.length; j++) {
			if (super.pos_dictionary.containsKey(temp_word_list[j]))
				final_file_list = addFileList(final_file_list, super.pos_dictionary.get(temp_word_list[j]).keySet());
		}
		for (Iterator<Long> j = final_file_list.keySet().iterator(); j.hasNext();)
			if (final_file_list.get(j.next()) < temp_word_list.length)
				j.remove();
		final_file_list = checkNearK(final_file_list, temp_word_list);
	}
	private HashMap<Long, Long> checkNearK(HashMap<Long, Long> current_file_list, String temp_word_list[]) {
		for (Iterator<Long> i = current_file_list.keySet().iterator(); i.hasNext();) {
			long file_number = i.next();
			iteratorloop: for (int j = 1; j < temp_word_list.length; j++) {
				String pos1_str = super.pos_dictionary.get(temp_word_list[j - 1]).get(file_number);
				String pos2_str = super.pos_dictionary.get(temp_word_list[j]).get(file_number);
				int pos1[] = Arrays.stream(pos1_str.substring(0, pos1_str.length() - 1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
				int pos2[] = Arrays.stream(pos2_str.substring(0, pos2_str.length() - 1).split(",")).map(String::trim).mapToInt(Integer::parseInt).toArray();
				outerloop: for (int k = 0; k < pos1.length; k++) {
					for (int l = 0; l < pos2.length; l++) {
						if ((pos2[l] - pos1[k]) <= near_k && (pos2[l] - pos1[k]) > 0) {
							break outerloop;
						}
						if (k == pos1.length - 1 && l == pos2.length - 1) {
							i.remove();
							break iteratorloop;
						}
					}
				}
			}
		}
		return current_file_list;
	}

	private void orQuery(String input) {
		or_word_list = input.split("\\+");
		for (int i = 0; i < or_word_list.length; i++)
			or_word_list[i] = new Stemmer().processWord(or_word_list[i]);

		for (int i = 0; i < or_word_list.length; i++) {
			if (or_word_list[i].trim().split(" ").length <= 0) {
				if (super.pos_dictionary.containsKey(or_word_list[i]))
					final_file_list = addFileList(final_file_list, super.pos_dictionary.get(or_word_list[i]).keySet());
			} else {
				HashMap<Long, Long> temp_file_list = new HashMap<>();
				String temp_word_list[] = or_word_list[i].trim().split(" ");
				near_k = 1;
				for (int j = 0; j < temp_word_list.length; j++) {
					if (super.pos_dictionary.containsKey(temp_word_list[j]))
						temp_file_list = addFileList(temp_file_list,
								super.pos_dictionary.get(temp_word_list[j]).keySet());
				}
				for (Iterator<Long> j = temp_file_list.keySet().iterator(); j.hasNext();)
					if (temp_file_list.get(j.next()) < temp_word_list.length)
						j.remove();
				temp_file_list = checkNearK(temp_file_list, temp_word_list);
				final_file_list = addFileList(final_file_list, temp_file_list.keySet());
			}

		}
	}

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

}
