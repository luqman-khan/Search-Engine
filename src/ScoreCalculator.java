import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScoreCalculator {
	Double wqt,wdt,score;
	protected byte[] calculateScores(Long tf, Long df, DocumentInfo docInfo, Long totalDocs){
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			bos.write(calculateDefault(tf,df,docInfo,totalDocs));
			bos.write(calculateTfIdf(tf,df,docInfo,totalDocs));
			bos.write(calculateOkapiBm25(tf,df,docInfo,totalDocs));
			bos.write(Wacky(tf,df,docInfo,totalDocs));
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	private byte[] Wacky(Long tf, Long df, DocumentInfo docInfo, Long totalDocs) {
		wdt = (new Double(1+Math.log(new Double(tf)))/new Double (1+Math.log(docInfo.avgTf)));
		wqt = Math.max(0, Math.log((totalDocs-df)/df));
		Double ld = Math.sqrt(docInfo.byteSize);
		score = (new Double((wdt*wqt/ld)));
		byte [] scoreBytes = new byte[8];
		ByteBuffer.wrap(scoreBytes).putDouble(score);
		return scoreBytes;
	}

	private byte[] calculateOkapiBm25(Long tf, Long df, DocumentInfo docInfo, Long totalDocs) {
		wdt = new Double(2.2*new Double(tf));
		wqt = Math.max(new Double(0.1), Math.log(new Double(totalDocs-df+0.5)/new Double(df+0.5)));
		Double ld = new Double(1.2*(0.25+(0.75*new Double(docInfo.token_count)/docInfo.avg_term_count))+new Double(tf));
		score = (new Double((wdt*wqt/ld)));
		byte [] scoreBytes = new byte[8];
		ByteBuffer.wrap(scoreBytes).putDouble(score);
		return scoreBytes;
	}

	private byte[] calculateTfIdf(Long tf, Long df, DocumentInfo docInfo, Long totalDocs) {
		
		wdt = new Double(tf);
		wqt = Math.log(new Double(new Double(totalDocs)/new Double(df))); 
		score = (new Double((wdt*wqt/docInfo.ld)));
		byte [] scoreBytes = new byte[8];
		ByteBuffer.wrap(scoreBytes).putDouble(score);
		return scoreBytes;
	}

	private byte[] calculateDefault(Long tf, Long df, DocumentInfo docInfo, Long totalDocs) {
		wdt = new Double (new Double(1) + Math.log(new Double(tf)));
		wqt = Math.log(1+ new Double(new Double(totalDocs)/new Double(df)));
		score = (new Double((wdt*wqt/docInfo.ld)));
		byte [] scoreBytes = new byte[8];
		ByteBuffer.wrap(scoreBytes).putDouble(score);
		return scoreBytes;
	}

}
