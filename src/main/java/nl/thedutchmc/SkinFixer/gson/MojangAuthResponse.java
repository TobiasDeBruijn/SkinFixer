package nl.thedutchmc.SkinFixer.gson;

import com.google.gson.annotations.SerializedName;

public class MojangAuthResponse {
	
	@SerializedName("id")
	private String uuid;

	public String getUuid() {
		return uuid;
	}
}
