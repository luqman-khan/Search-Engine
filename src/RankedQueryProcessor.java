import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Stream;

public class RankedQueryProcessor {
	private DiskInvertedIndex disk_inverted_index;
	private HashMap<Long, Double> docScoreMap = new HashMap<>();
	private String folder;
	
	
	RankedQueryProcessor(String directory) {
		disk_inverted_index = new DiskInvertedIndex(directory);
		folder = directory;
	}
	
	protected String processQuery(String input,int k,QueryProcessor index) {
		String[] terms = input.split("[ ]+");
		
		HashMap<Long, Double[]> docScore = new HashMap<>();
		for(String term: terms){
			term = new Stemmer().processWord(term);
			if(disk_inverted_index.getPostings(term)!=null){
				docScore = disk_inverted_index.getPostings(term).docIdScoreHash;
				for(Long key : docScore.keySet()){
					if(docScoreMap.containsKey(key)){
						docScoreMap.put(key, docScoreMap.get(key)+docScore.get(key)[k]);
					} else {
						docScoreMap.put(key, docScore.get(key)[k]);
					}
				}
			}
		}
		PriorityQueue<Map.Entry<Long, Double>> queue = getTopK(k,docScoreMap);
		String outputString = getOutputString(queue);
		return outputString;
	}
	
	private String getOutputString(PriorityQueue<Map.Entry<Long, Double>> queue) {
		String outputString="";
		if(queue.size()<1)
			return "Word Not Found ...... !!!";
		try {
			Stream<String> lines;
			for(int i=0; i<10; i++){
				lines = Files.lines(Paths.get(folder+"\\FileIdMap.bin"));
				Map.Entry<Long, Double> a = queue.poll();
				if(a==null)
					break;
				outputString += lines.skip(a.getKey()).findFirst().get()+" \t"+a.getKey()+" \t"+a.getValue()+"\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return outputString;
	}

	private PriorityQueue<Map.Entry<Long, Double>> getTopK(int k, HashMap<Long, Double> documentsScore){
		
		PriorityQueue<Map.Entry<Long, Double>> queue = 
		        new PriorityQueue<Map.Entry<Long, Double>>(new Comparator<Map.Entry<Long, Double>>()
		        {
		           @Override
		           public int compare(Map.Entry<Long, Double> entry1, Map.Entry<Long, Double> entry2)
		             {
		        	   if((entry2.getValue() - entry1.getValue())>0)
		        		   return 1;
		        	   else if((entry2.getValue() - entry1.getValue())<0)
		        		   return -1;
		        	   else 
		        		   return 0;
		             }
		        });
		
		for(Map.Entry<Long, Double> a: documentsScore.entrySet())
			queue.add(a);
		
		return queue;
	}
	
}
