package nl.thedutchmc.SkinFixer.common;

import java.util.Random;

import nl.thedutchmc.SkinFixer.fileHandlers.StorageHandler;

public class AddNewSkin {

	public static int add(String url) {
		
		//Generate a code
		int code = generateRandomInt();
		
		//If the pendingLinks list contains the code already, generate a new one.
		//Keep going until we get a code that does not yet exist
		while(StorageHandler.pendingLinks.containsKey(code)) {
			code = generateRandomInt();
		}
		
		StorageHandler.pendingLinks.put(code, url);
		
		return code;
	}
	
	private static int generateRandomInt() {
		final Random rnd = new Random();
		final int n = 100000 + rnd.nextInt(900000);
		
		return n;
	}
}
