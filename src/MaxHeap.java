public class MaxHeap {
	
	
	public Long[] myHeap;
	public int begin = 0;
	Long [] orderedDocs;
	public int current = 0;
			
	public MaxHeap(Long [] docAndScore){
		myHeap = new Long[docAndScore.length];
		for(int i=0; i<docAndScore.length; i+=2){
			insert(docAndScore[i],docAndScore[i+1]);
		}
			
	}
	public int getParent(int index){
	    return (index/2 - 1)/2;
	}

	public int getLeftChild(int index){
	    return (2*index+1)*2;
	}

	public int getRighChild(int index){
	    return (2*index+2)*2;
	}

	public void insert(Long data, Long score) {

	    myHeap[current] = data;
	    myHeap[current+1] = score;
	    
	    int i = current;
 	    Long tmpData, tmpScore;
	    int parentIndex,parentScoreIndex;
	    while(i > 0){
	    	parentIndex = getParent(i);
	    	parentScoreIndex = i+1;
//	        System.out.println(" I value"+i+" parent"+parentIndex+" data"+data);
	        if(myHeap[parentScoreIndex]!=null && myHeap[parentScoreIndex] < myHeap[i+1]){
	        	tmpData = myHeap[parentIndex];
	        	tmpScore = myHeap[parentScoreIndex];
	            myHeap[parentIndex] = myHeap[i];
	            myHeap[parentScoreIndex] = myHeap[i+1];
	            myHeap[i] = tmpData;
	            myHeap[i+1] = tmpScore;
	        } else{
	            break;
	        }
	        i = parentIndex;
	    }
	    current++;
	}
	public Long[] getTopK(int k){
		orderedDocs = new Long[k];
		for(int i=0; i<k; i++){
			if(myHeap[i*2]!=null)
				orderedDocs[i]=myHeap[i*2];
			else
				break;
		}
		return orderedDocs;
	}
}
