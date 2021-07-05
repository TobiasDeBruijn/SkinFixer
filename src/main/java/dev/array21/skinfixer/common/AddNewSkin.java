package dev.array21.skinfixer.common;

import java.util.Random;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.apis.SkinFixerApi;
import dev.array21.skinfixer.util.Pair;
import dev.array21.skinfixer.util.Triple;
import dev.array21.skinfixer.util.Utils;

public class AddNewSkin {

	private SkinFixer plugin;
	public AddNewSkin(SkinFixer plugin) {
		this.plugin = plugin;
	}
	
	public Pair<Integer, String> addByNickname(String url) {
		//Generate a code
		int code = generateRandomInt();
		
		//If the pendingLinks list contains the code already, generate a new one.
		//Keep going until we get a code that does not yet exist		
		while(this.plugin.getSkinCodeUrlMap().containsKey(code) || this.plugin.getSkinCodeUuidMap().containsKey(code)) {
			code = generateRandomInt();
		}
		
		Triple<Boolean, String, String> apiResponse = new SkinFixerApi().getUuid(url);
		if(!apiResponse.getA()) {
			return new Pair<Integer, String>(null, apiResponse.getC());
		}
		
		String uuidDashed = Utils.insertDashUUID(apiResponse.getB());
		this.plugin.insertSkinCodeUuid(code, uuidDashed);
		
		return new Pair<Integer, String>(code, null);
	}
	
	public int addByUrl(String url) {
		
		//Generate a code
		int code = generateRandomInt();
		
		//If the pendingLinks list contains the code already, generate a new one.
		//Keep going until we get a code that does not yet exist		
		while(this.plugin.getSkinCodeUrlMap().containsKey(code) || this.plugin.getSkinCodeUuidMap().containsKey(code)) {
			code = generateRandomInt();
		}
		
		this.plugin.insertSkinCodeUrl(code, url);
		return code;
	}
	
	private int generateRandomInt() {
		final Random rnd = new Random();
		final int n = 100000 + rnd.nextInt(900000);
		
		return n;
	}
}
