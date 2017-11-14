import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


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
	private List<Byte> encodeNumber(Long number) {
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

	/**
	 * Decode function to retrieve integer from byte.
	 */
	protected Long[] decode(Byte[] byteStreamArray) {
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

	/**
	 * Byte to Integer conversion.
	 */
	private int getIntValue(Byte a_byte) {
		return a_byte & MASK;
	}
	
	/**
	 * Main VBE Tester.
	 */
	public static void main(String[] args) {
		
		VariableByteEncodingCLass vbeObj = new VariableByteEncodingCLass();
		Scanner scanner = new Scanner(System.in);
		Long[] my_long_array = {new Long(555), new Long(456), new Long(200), new Long(150)};
		Byte[] encoded_array;
		Long[] decoded_array;
		encoded_array = vbeObj.encode(my_long_array);
		decoded_array = vbeObj.decode(encoded_array);
		System.out.println("\n Encoded :" + Arrays.asList(encoded_array));
		System.out.println("\n Decoded :" + Arrays.asList(decoded_array));

		scanner.close();
	}
}
