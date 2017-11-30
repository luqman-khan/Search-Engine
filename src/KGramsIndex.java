import java.util.ArrayList;

/**
 * Created by sinaastani on 10/2/17.
 */
public class KGramsIndex {

    public static ArrayList<String> gen_ngrams(int n, String in){
        ArrayList<String> ngrams = new ArrayList<>();
        char[] chars = in.toCharArray();
        int num_chars = chars.length;
        for(int i=0; i < (num_chars - n + 1); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(chars[i]);
            int start = i+1;
            for(; start < i+n; start++) {
                sb.append(chars[start]);
            }
            ngrams.add(sb.toString());
        }
        return ngrams;
    }

    public static void printKgram(ArrayList<String> s){
        for(String r : s){
            System.out.println(r);
        }
    }

    public static void main(String[] args){
        String a = "metric";
//        String b = "oh bro big time";
        ArrayList<String> ls = gen_ngrams(1, a);
        printKgram(ls);
        ls = gen_ngrams(2, a);
        printKgram(ls);
        ls = gen_ngrams(3, a);
        printKgram(ls);
    }
}
