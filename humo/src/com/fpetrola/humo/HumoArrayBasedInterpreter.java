package com.fpetrola.humo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class HumoArrayBasedInterpreter {

	private static JTextArea view;

	public static void main(String[] args) throws IOException, URISyntaxException {

		String content = new String(Files.readAllBytes(Paths.get(HumoArrayBasedInterpreter.class.getResource("/t2.humo").toURI())));
		show(content);

		List<Block> result = buildBlocks(content);
		addBlocks(result, 20000);
		HumoArrayBasedInterpreter humo1 = new HumoArrayBasedInterpreter();
		humo1.parse(result, content.length());
		humo1.printProductions(result);
	}

	private static void addBlocks(List<Block> blocks, int i) {
		for (int j = 0; j < i; j++) {
			blocks.add(new Block(""));
		}
	}

	private static List<Block> buildBlocks(String content) {

		List<Block> blocks = new ArrayList<>();
		int lastChar = -1;
		for (int i = 0; i < content.length(); i++) {
			char charAt = content.charAt(i);

			if (charAt == 0xD || charAt == 0xA || charAt == 0x20 || charAt == 0x09) {
			} else {
				if (lastChar != -1)
					blocks.add(new Block(content.substring(lastChar, i)));
				lastChar = i;
			}
		}

		return blocks;
	}

	public HumoArrayBasedInterpreter() {
	}

	private void parse(List<Block> result, int length) {
		Stack<Block> blocks = new Stack<>();
		int[] readIndexes = new int[result.size()];
		for (int write = length, read = 0, keyLen = 0, chars = 0, position = 0, closed = 0, depth = 0; read != length; keyLen = chars == 0 ? 1 : keyLen + 1, chars = 0) {
			if ((result.get(write++).replace(result.get(read++))).equals('{')) {
				readIndexes[++depth] = 0;
			} else if (result.get(read - 1).equals('}')) {
				if (readIndexes[depth--] != 0 && (depth == 0 || readIndexes[depth] != 0 || readIndexes[depth] < readIndexes[depth + 1])) {
					read = readIndexes[depth + 1];
					write--;
					for (int i = 0; i < keyLen; i++) {
						result.set(write + i, new Block(""));
					}
					Block block = blocks.pop();
					result.get(write - 1).merge(block);
				}
			} else {
				for (position = write, closed = 0, chars = -1; chars != 0 && position >= length; position--) {
					closed = result.get(position).equals('}') ? (closed < 0 ? 1 : closed + 1) : (result.get(position).equals('{') ? closed - 1 : closed);
					chars = chars == -1 && result.get(position).equals('{') && (result.get(position - keyLen - 1).equals('}') || result.get(position - keyLen - 1).equals('{')) ? keyLen : (chars > 0 && closed >= 0 ? result.get(position).equals(result.get(write - keyLen + chars - 1)) ? chars - 1 : -1 : chars);
				}
				if (chars == 0) {
					Block block = result.get(write - 1);
					blocks.push(block);
					result.set(write - 1, new Block(""));

					readIndexes[++depth] = read;
					for (int i = 1; i < keyLen; i++) {
						result.set(write + i, new Block(""));
					}
					read = position + keyLen + 2;
					write -= keyLen;
				}
			}
			printProductions(result);
		}
	}

	private void printProductions(List<Block> result) {

		try {
			Appendable r = new StringBuilder();
			for (int i = 0; i < result.size() - 0; i++) {
				r.append(result.get(i + 0).toString());
			}
			String string = r.toString();
			view.setText(string);
			view.setCaretPosition(string.length());
			Thread.sleep(1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void show(String content) {
		JFrame f = new JFrame("Text Area Examples");
		JPanel lowerPanel = new JPanel();
		f.getContentPane().add(lowerPanel, "South");

		view = new JTextArea(content, 40, 60);
		view.setLineWrap(true);
		view.setWrapStyleWord(true);
		lowerPanel.add(new JScrollPane(view));

		f.pack();
		f.setVisible(true);
	}
}
