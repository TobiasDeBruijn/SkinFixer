package nl.thedutchmc.SkinFixer;

import java.util.UUID;

public class SkinObject {

	private UUID owner;
	private String value, signature;
	private boolean slim;
	
	public SkinObject(UUID owner, String value, String signature) {
		this.owner = owner;
		this.value = value;
		this.signature = signature;
		slim = false;
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
	
	public void setSlim(boolean slim) {
		this.slim = slim;
	}
	
	public boolean getSlim() {
		return slim;
	}
} 
