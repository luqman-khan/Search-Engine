import org.tartarus.snowball.SnowballStemmer;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
}
