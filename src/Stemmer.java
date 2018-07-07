import org.tartarus.snowball.SnowballStemmer;

 // This class is used for stemming file and query terms using Porter2 stemmer.
 
class Stemmer {
	public String processWord(String word) {
		try {
			Class stemClass = Class.forName("org.tartarus.snowball.ext.englishStemmer");
			SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
			word = word.replaceAll("\\W", "").toLowerCase();
			stemmer.setCurrent(word);
			stemmer.stem();
			return stemmer.getCurrent();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
