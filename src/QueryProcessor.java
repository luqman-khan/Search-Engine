import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextPane;

public class QueryProcessor extends InvertedIndex {

	QueryProcessor(Path directory) {
		super(directory);
	}

	private int near_k = 1;

	public void processQuery(String input, JTextPane result_txt) {
		near_k = 3;
		
		Pattern near_pattern = Pattern.compile("(.+)(\\s+)near(/)([0-9]+)(\\s+)(.+)");
		Matcher near_matcher = near_pattern.matcher(input);
		Pattern stem_pattern = Pattern.compile("^:stem(\\s+)(.+)$");
		Matcher stem_matcher = stem_pattern.matcher(input);
		
//		Pattern wild_pattern = Pattern.compile("(.*)([*,+])near(/)([0-9]+)(\\s+)(.+)");

		String [] input_terms;
		if (near_matcher.find()) {
			near_k = Integer.parseInt((input.split("near/")[1].split(" ")[0]));
			input = input.replaceAll("near/([0-9])+", "");
			System.out.println("Actual string is : "+ input);
			System.out.println(near_matcher.find());
			System.out.println("Near k value is " + near_k);
		}
		if(stem_matcher.find()){
			result_txt.setText(new Stemmer().processWord(input.split(":stem(\\s+)")[1]));
			return;
		}
		input_terms = getInputTerms(input);

		System.out.println("Length of input after stemmer   " + input_terms.length);
		if (input_terms.length > 0) {
			printResults(input_terms, result_txt);
		}
	}

	private void printResults(String input_terms[], JTextPane result_txt) {
		System.out.println("Length of input term is " + input_terms.length);
		HashMap<Long, Long> query_result = searchQuery(input_terms);

		if (query_result.size() > 0) {
			for (Iterator<Long> i = query_result.keySet().iterator(); i.hasNext();) {
				long file_number = i.next();
				result_txt.setText(result_txt.getText() + super.files.get(file_number) + "\n");
			}
		} else {
			result_txt.setText("NO RESULT FOUND.......\n");
		}
	}

	private HashMap<Long, Long> searchQuery(String[] input_terms) {
		HashMap<Long, Long> file_intersection = new HashMap<Long, Long>();
		for (int i = 0; i < input_terms.length; i++) {
			System.out.println("Word " + i);
			if (super.pos_dictionary.containsKey(input_terms[i])) {
				for (Iterator<Long> j = super.pos_dictionary.get(input_terms[i]).keySet().iterator(); j.hasNext();) {
					long file_number = j.next();
					System.out.print("File " + file_number + "   ");
					if (!file_intersection.containsKey(file_number))
						file_intersection.put(file_number, new Long(1));
					else
						file_intersection.put(file_number, file_intersection.get(file_number) + 1);
					System.out.println(file_intersection.get(file_number));
				}
			}
		}
		System.out.println("pass 1 " + file_intersection.size());
		for (Iterator<Long> i = file_intersection.keySet().iterator(); i.hasNext();) {
			long file_number = i.next();
			System.out.println(file_intersection.get(file_number));
			if (file_intersection.get(file_number) < input_terms.length)
				i.remove();
		}
		System.out.println("pass 2 " + file_intersection.size());
		for (Iterator<Long> i = file_intersection.keySet().iterator(); i.hasNext();) {
			long file_number = i.next();
			for (int j = 1; j < input_terms.length; j++) {
				String pos1_str = super.pos_dictionary.get(input_terms[j - 1]).get(file_number);
				String pos2_str = super.pos_dictionary.get(input_terms[j]).get(file_number);
				int pos1[] = Arrays.stream(pos1_str.substring(0, pos1_str.length() - 1).split(",")).map(String::trim)
						.mapToInt(Integer::parseInt).toArray();
				int pos2[] = Arrays.stream(pos2_str.substring(0, pos2_str.length() - 1).split(",")).map(String::trim)
						.mapToInt(Integer::parseInt).toArray();

				outerloop: for (int k = 0; k < pos1.length; k++) {
					for (int l = 0; l < pos2.length; l++) {

						System.out.println(((pos2[l] - pos1[k]) <= near_k) + " and " + ((pos2[l] - pos2[k]) >= 0)
								+ "           " + (((pos2[l] - pos1[k]) <= near_k) && ((pos2[l] - pos2[k]) >= 0)));
						if (((pos2[l] - pos1[k]) <= near_k) && ((pos2[l] - pos2[k]) >= 0)) {
							System.out.println("break to outer loop");
							break outerloop;
						}
						if (k == pos1.length - 1 && l == pos2.length - 1) {
							System.out.println("remove " + (pos1.length - 1) + " and " + (pos2.length - 1));
							i.remove();
						}
					}
				}
			}
		}

		return file_intersection;
	}

	private String[] getInputTerms(String input) {
		String input_terms[] = new String[input.length()];
		input_terms = input.split("\\s+");
		for (int i = 0; i < input_terms.length; i++) {
			System.out.println("before   " + input_terms[i]);
			input_terms[i] = new Stemmer().processWord(input_terms[i]);
			System.out.println("after   " + input_terms[i]);
		}
		return input_terms;
	}
}
