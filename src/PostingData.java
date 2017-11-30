import java.util.ArrayList;
import java.util.HashMap;

public class PostingData {
	HashMap<Long, Double[]> docIdScoreHash = new HashMap<>();
	HashMap<Long, ArrayList<Long>> docIdPositionsHash = new HashMap<>();
}
