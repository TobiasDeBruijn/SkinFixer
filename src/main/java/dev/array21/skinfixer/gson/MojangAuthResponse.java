package dev.array21.skinfixer.gson;

import com.google.gson.annotations.SerializedName;

public class MojangAuthResponse {
	
	@SerializedName("id")
	private String uuid;

	public String getUuid() {
		return uuid;
	}
}
