package dev.array21.skinfixer.storage;

import java.util.UUID;

import dev.array21.skinfixer.SkinObject;

public class SkinData {
	
	public SkinData() {}
	public SkinData(UUID owner, String value, String signature) {
		this.playerUuid = owner.toString();
		this.value = value;
		this.signature = signature;
	}
	
	public String playerUuid;
	public String value;
	public String signature;
	
	public SkinObject into() {
		return new SkinObject(UUID.fromString(this.playerUuid), value, signature);
	}	
}
