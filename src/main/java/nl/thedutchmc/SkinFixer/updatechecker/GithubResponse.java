package nl.thedutchmc.SkinFixer.updatechecker;

import com.google.gson.annotations.SerializedName;

public class GithubResponse {
	@SerializedName("html_url")
	private String htmlUrl;
	
	@SerializedName("tag_name")
	private String tagName;
	
	/**
	 * @return The URL of the new GitHub Release
	 */
	public String getUrl() {
		return htmlUrl;
	}
	/**
	 * @return The name of the Release
	 */
	public String getTagName() {
		return tagName;
	}
}