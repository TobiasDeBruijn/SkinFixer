package dev.array21.skinfixer.common;

import java.util.HashMap;
import java.util.Random;

import dev.array21.skinfixer.SkinFixer;

public class AddNewSkin {

	private SkinFixer plugin;
	public AddNewSkin(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public int add(String url) {
		
		//Generate a code
		int code = generateRandomInt();
		
		//If the pendingLinks list contains the code already, generate a new one.
		//Keep going until we get a code that does not yet exist
		HashMap<Integer, String> skinCodes = this.plugin.getSkinCodeMap();
		
		while(skinCodes.containsKey(code)) {
			code = generateRandomInt();
		}
		
		this.plugin.insertSkinCode(code, url);
		return code;
	}
	
	private int generateRandomInt() {
		final Random rnd = new Random();
		final int n = 100000 + rnd.nextInt(900000);
		
		return n;
	}
}
