import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


	 /**
	 * Variable byte (VB) encoding uses an integral number of bytes to encode a gap.
	 */
public class VariableByteEncodingCLass {
	public final static int MASK = 0xff;
	
	/**
	 * Encode function to fetch encoded bytes.
	 */
	protected Byte[] encode(Long[] numbers) {
		List<Byte> byteStreamArray = new ArrayList<Byte>();
		for (Long number : numbers) {
			byteStreamArray.addAll(encodeNumber(number));
		}
		return byteStreamArray.toArray(new Byte[byteStreamArray.size()]);
	}
	
	/**
	 * Conversion of integer to bytes as per algorithm in 'Introduction to Information Retrieval'.
	 */
	protected List<Byte> encodeNumber(Long number) {
		List<Byte> bytesArrayList = new ArrayList<Byte>();
		Long n = number;
		while (true) {
			bytesArrayList.add(0, (byte) (n % 128));
			if (n < 128) {
				break;
			}
			n /= 128;
		}
		bytesArrayList.set(bytesArrayList.size() - 1, (byte) ((bytesArrayList.get(bytesArrayList.size() - 1) ^ 0x80)));
		
		return bytesArrayList;
	}
	
	protected byte[] encodeNumberToByteArray(Long number) {
		List<Byte> bytesArrayList = new ArrayList<Byte>();
		ByteArrayOutputStream byte_array_buffer = new ByteArrayOutputStream();
		Long n = number;
		while (true) {
			bytesArrayList.add(0, (byte) (n % 128));
			if (n < 128) {
				break;
			}
			n /= 128;
		}
		bytesArrayList.set(bytesArrayList.size() - 1, (byte) ((bytesArrayList.get(bytesArrayList.size() - 1) ^ 0x80)));
		for(byte b: bytesArrayList)
			byte_array_buffer.write(b);
		return byte_array_buffer.toByteArray();
	}

	/**
	 * Decode function to retrieve integer from byte.
	 */
	protected Long[] decode(byte[] byteStreamArray) {
		List<Long> longArrayList = new ArrayList<Long>();
		Long n = new Long(0);
		for (Byte abyte : byteStreamArray) {
			int an_int = getIntValue(abyte.byteValue());
			if (an_int < 128) {
				n = (n * 128) + an_int;
			} else {
				n = (n * 128) + (an_int - 128);
				longArrayList.add(n);
				n = new Long(0);
			}
		}
		return longArrayList.toArray(new Long[longArrayList.size()]);
	}
	
	public Long decodeNumber(Byte[] byteStreamArray) {
		Long n = new Long(0);
		for (Byte abyte : byteStreamArray) {
			int an_int = getIntValue(abyte.byteValue());
			if (an_int < 128) {
				n = (n * 128) + an_int;
			} else {
				n = (n * 128) + (an_int - 128);
				return n;
			}
		}
		return null;
	}

	/**
	 * Byte to Integer conversion.
	 */
	private int getIntValue(Byte a_byte) {
		return a_byte & MASK;
	}
		
}
