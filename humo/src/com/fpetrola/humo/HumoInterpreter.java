package com.fpetrola.humo;

import java.util.Arrays;

public class HumoInterpreter {

	private void parse(char[] result, int length) {
		int[] readIndexes = new int[result.length];
		for (int write = length, read = 0, keyLen = 0, chars = 0, position = 0, closed = 0, depth = 0; read != length; keyLen = chars == 0 ? 1 : keyLen + 1, chars = 0) {
			 System.out.println(new String(result));
			if ((result[write++] = result[read++]) == '{') {
				readIndexes[++depth] = 0;
			} else if (result[read - 1] == '}') {
				if (readIndexes[depth--] != 0 && (depth == 0 || readIndexes[depth] != 0 || readIndexes[depth] < readIndexes[depth + 1])) {
					read = readIndexes[depth + 1];
					write--;
				}
			} else {
				for (position = write, closed = 0, chars = -1; chars != 0 && position >= length; position--) {
					closed = result[position] == '}' ? (closed < 0 ? 1 : closed + 1) : (result[position] == '{' ? closed - 1 : closed);
					if (chars == -1 && result[position] == '{' && (result[position - keyLen - 1] == '}' || result[position - keyLen - 1] == '{'))
						chars = keyLen;
					else
						chars = (chars > 0 && closed >= 0 ? result[position] == result[write - keyLen + chars - 1] ? chars - 1 : -1 : chars);
				}
				if (chars == 0) {
					readIndexes[++depth] = read;
					read = position + keyLen + 2;
					write -= keyLen;
				}
			}
		}
	}

	// System.out.println(new String(result));
	// showMatching(writingIndex, keyLength,
	// position, found);
	// private void showMatching(int writingIndex, char keyLength, int position,
	// int found) {
	// StringBuffer stringBuffer = new StringBuffer(new String(result));
	// stringBuffer.insert(writingIndex - keyLength + found - 1 + 1, ']');
	// stringBuffer.insert(writingIndex - keyLength + found - 1, '[');
	// stringBuffer.insert(position + 1, ')');
	// stringBuffer.insert(position, '(');
	// System.out.println(stringBuffer.toString());
	// }

	public static void main(String[] args) {
		HumoInterpreter humoInterpreter = new HumoInterpreter();
		char[] result = new char[1000];
		Arrays.fill(result, ' ');
		String source = "a{12}b{23}c{abz{3}}d{1223zA{4}a3A{5}}e{3{d}}";
		System.arraycopy(source.toCharArray(), 0, result, 0, source.length());

		humoInterpreter.parse(result, source.length());

		String x = new String(result);
		boolean assert1 = "a{12}b{23}c{abz{3}}d{1223zA{4}a3A{5}}e{3{d}}a{12}b{23}c{1223z{3}}d{3A{4}124{5}}e{3{4{4}5{5}}}".equals(x.trim());
		System.out.println(x);
		System.out.println(assert1);
	}
}