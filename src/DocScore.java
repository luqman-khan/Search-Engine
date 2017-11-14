
public class DocScore implements Comparable<DocScore> {
	Long docId;
	Long score;
	public DocScore(Long id, Long scr){
		docId=id;
		score=scr;
	}
	
	@Override
    public int compareTo(DocScore cmp) {
		if(cmp!=null)
			return this.score.compareTo(cmp.score);
		else
			return this.score.compareTo(new Long(0));
	}
}
