package vbe;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class VariableByteEncodingCLass {
	public List<Byte> encode(int[] numbers) {
		List<Byte> byteStreamArray = new ArrayList<Byte>();
		for (int number : numbers) {
			byteStreamArray.addAll(encodeNumber(number));
		}
		return byteStreamArray;
	}

	private List<Byte> encodeNumber(int number) {
		List<Byte> bytesArrayList = new ArrayList<Byte>();
		int n = number;
		int i = 0;
		while (true) {
			bytesArrayList.add(0, (byte) (n % 128));
			if (n < 128) {
				break;
			}
			n /= 128;
		}
		Byte b = (Byte) (bytesArrayList.get(bytesArrayList.size() - 1));
		Byte q = (byte) (b ^ (byte) (1 << 7));
		bytesArrayList.set(bytesArrayList.size() - 1, (byte) ((bytesArrayList.get(bytesArrayList.size() - 1) ^ 0x80)));
		return bytesArrayList;
	}

	public List<Integer> decode(List<Byte> byteStreamArray) {
		List<Integer> intArrayList = new ArrayList<Integer>();
		int n = 0;
		for (Byte abyte : byteStreamArray) {
			System.out.println("-----abyte---IN FOR---IS  " + abyte);
			int someInt = getIntValue(abyte.byteValue(), n);
			if (someInt < 128) {
				n = (n * 128) + someInt;
			} else {
				n = (n * 128) + (someInt - 128);
				intArrayList.add(n);
				n = 0;
			}
		}
		return intArrayList;
	}

	private int getIntValue(Byte aByte, int n) {
		int newInt = 0;
		int res = 0;

		String someString = Integer.toBinaryString(aByte);
		int[] numarray = new int[someString.length()];
		for (int i = 0; i < someString.length(); i++) {
			numarray[i] = someString.charAt(i);
		}
		for (int j = 0; j < numarray.length; j++) {
			System.out.println("sasdsadasdadasdasd   " + numarray[j]);
		}
		System.out.println(someString);
		for (int k = 0; k < numarray.length; k++) {
			res += numarray[k] * Math.pow(2, (7 - k));
		}
		System.out.println(" Value ***** " + res);
		return newInt;
	}

	public static void main(String[] args) {
		VariableByteEncodingCLass vbeObj = new VariableByteEncodingCLass();
		Scanner scanner = new Scanner(System.in);
		int[] myIntArray = new int[] { 333, 2 };
		System.out.println("\n Encode called :" + vbeObj.encode(myIntArray));
		System.out.println("Decode called :" + vbeObj.decode(vbeObj.encode(myIntArray)));
		Byte test = -126;

		System.out.println("some thing needed*******" + Byte.toUnsignedInt((test.byteValue())));
		System.out.println("some thing needed in binary*******" + Integer.toBinaryString(test));
		scanner.close();
	}
}
