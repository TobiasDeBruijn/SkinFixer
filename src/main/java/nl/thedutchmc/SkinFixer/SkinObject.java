package nl.thedutchmc.SkinFixer;

import java.util.UUID;

public class SkinObject {

	private UUID owner;
	private String value, signature;
	
	public SkinObject(UUID owner, String value, String signature) {
		this.owner = owner;
		this.value = value;
		this.signature = signature;
	}
	
	public UUID getOwner() {
		return owner;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void updateSkin(String value, String signature) {
		this.value = value;
		this.signature = signature;
	}
} 
