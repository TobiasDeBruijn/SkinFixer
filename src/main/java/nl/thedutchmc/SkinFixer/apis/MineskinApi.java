package nl.thedutchmc.SkinFixer.apis;

import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

import nl.thedutchmc.SkinFixer.SkinFixer;
import nl.thedutchmc.SkinFixer.gson.GetSkinResponse;
import nl.thedutchmc.SkinFixer.util.Triple;
import nl.thedutchmc.SkinFixer.util.Utils;
import nl.thedutchmc.httplib.Http;
import nl.thedutchmc.httplib.Http.RequestMethod;
import nl.thedutchmc.httplib.Http.ResponseObject;


public class MineskinApi {
	
	public Triple<Boolean, GetSkinResponse, String> getSkin(String skinUrl, boolean slim) {
		HashMap<String, String> urlParameters = new HashMap<>();
		urlParameters.put("User-Agent", "SkinFixer");
		urlParameters.put("url", skinUrl);
		
		if(slim) {
			urlParameters.put("model", "slim");
		}
		
		ResponseObject apiResponse;
		try {
			apiResponse = new Http().makeRequest(RequestMethod.POST, "https://api.mineskin.org/generate/url", urlParameters, null, null, null);
		} catch(IOException e) {
			SkinFixer.logWarn("An IOException occured while fetching a skin.");
			//TODO logDebug
			
			return new Triple<Boolean, GetSkinResponse, String>(false, null, Utils.getStackTrace(e));
		}
		
		if(apiResponse.getResponseCode() != 200) {
			SkinFixer.logWarn("The MineSkin API returned an unexpected result: " + apiResponse.getConnectionMessage());
			return new Triple<Boolean, GetSkinResponse, String>(false, null, apiResponse.getConnectionMessage());
		}
		
		final Gson gson = new Gson();
		return new Triple<Boolean, GetSkinResponse, String>(true, gson.fromJson(apiResponse.getMessage(), GetSkinResponse.class), null);
	}
	
	public Triple<Boolean, GetSkinResponse, String> getSkinOfPremiumPlayer(String uuid) {
		
		ResponseObject apiResponse;
		try {
			apiResponse = new Http().makeRequest(RequestMethod.GET, "https://api.mineskin.org/generate/user/" + uuid, null, null, null, null);
		} catch(IOException e) {
			SkinFixer.logWarn("An IOException occured while fetching a skin.");
			//TODO logDebug
			
			return new Triple<Boolean, GetSkinResponse, String>(false, null, Utils.getStackTrace(e));
		}
		
		if(apiResponse.getResponseCode() != 200) {
			SkinFixer.logWarn("The MineSkin API returned an unexpected result: " + apiResponse.getConnectionMessage());
			return new Triple<Boolean, GetSkinResponse, String>(false, null, apiResponse.getConnectionMessage());
		}
		
		final Gson gson = new Gson();
		return new Triple<Boolean, GetSkinResponse, String>(true, gson.fromJson(apiResponse.getMessage(), GetSkinResponse.class), null);
	}
}
