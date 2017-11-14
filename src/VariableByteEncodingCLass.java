package vbe;

import java.util.ArrayList;
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
	protected List<Byte> encode(int[] numbers) {
		List<Byte> byteStreamArray = new ArrayList<Byte>();
		for (int number : numbers) {
			byteStreamArray.addAll(encodeNumber(number));
		}
		return byteStreamArray;
	}
	
	/**
	 * Conversion of integer to bytes as per algorithm in 'Introduction to Information Retrieval'.
	 */
	protected List<Byte> encodeNumber(int number) {
		List<Byte> bytesArrayList = new ArrayList<Byte>();
		int n = number;
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
	protected List<Integer> decode(List<Byte> byteStreamArray) {
		List<Integer> intArrayList = new ArrayList<Integer>();
		int n = 0;
		for (Byte abyte : byteStreamArray) {
			int an_int = getIntValue(abyte.byteValue(), n);
			if (an_int < 128) {
				n = (n * 128) + an_int;
			} else {
				n = (n * 128) + (an_int - 128);
				intArrayList.add(n);
				n = 0;
			}
		}
		return intArrayList;
	}

	/**
	 * Byte to Integer conversion.
	 */
	private int getIntValue(Byte a_byte, int n) {
		return a_byte & MASK;
	}
	
	/**
	 * Main VBE Tester.
	 */
	public static void main(String[] args) {
		
		VariableByteEncodingCLass vbeObj = new VariableByteEncodingCLass();
		Scanner scanner = new Scanner(System.in);
		int[] my_int_array = new int[] { 333, 22, 11,12312, 2 };
		List<Byte> encoded_array = new ArrayList<Byte>();
		List<Integer> decoded_array = new ArrayList<Integer>();
		encoded_array = vbeObj.encode(my_int_array);
		decoded_array = vbeObj.decode(encoded_array);
		System.out.println("\n Encoded :" + encoded_array);
		System.out.println("\n Decoded :" + decoded_array);

		scanner.close();
	}
}
