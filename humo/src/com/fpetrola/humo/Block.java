package com.fpetrola.humo;

public class Block {

	private String string;

	public Block(char c) {
		string = c + "";
	}

	public Block(String string) {
		this.string = string;
	}

	public int hashCode() {
		return clear(this).hashCode();
	}

	public boolean equals(Object obj) {

		if (obj instanceof Block) {
			Block block = (Block) obj;
			String replace = clear(block);
			return clear(this).equals(replace);
		} else if (obj instanceof Character) {
			Character character = (Character) obj;
			boolean equals = clear(this).equals(character.toString());
			return equals;
		}

		return false;
	}

	public String clear(Block block) {
		return block.string.replaceAll("[\\x0D\\x0A\\x20\\x09]", "");
	}

	public String toString() {
		return string;
	}

	public Block replace(Block block) {
		if (string.length() == 0)
			string = block.string;
		else {
			String cleared = clear(block);
			if (cleared.equals(block.string)) {
				String replaceAll = string.replaceAll("[^\\x0D\\x0A\\x20\\x09]", cleared.replace("$", "\\$"));
				string = replaceAll;
			} else
				string = block.string;
		}
		return this;
	}

	public void merge(Block block) {
		block.replace(this);
		string = block.string;
	}
}