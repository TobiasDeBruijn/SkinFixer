package dev.array21.skinfixer.apis;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

import com.google.gson.Gson;

import dev.array21.skinfixer.SkinFixer;
import dev.array21.skinfixer.apis.gson.GetSkinResponse;
import dev.array21.skinfixer.apis.gson.GetUuidResponse;
import dev.array21.skinfixer.apis.gson.RemoteInfoManifest;
import dev.array21.skinfixer.util.Triple;
import dev.array21.skinfixer.util.Utils;
import dev.array21.httplib.Http;
import dev.array21.httplib.Http.RequestMethod;
import dev.array21.httplib.Http.ResponseObject;
import org.jetbrains.annotations.NotNull;

public class SkinFixerApi {

	final String[] servers;

	public SkinFixerApi(RemoteInfoManifest infoManifest) {
		this.servers = infoManifest.servers;
	}

	public Triple<Boolean, String, String> getUuid(String playerName) {
		ResponseObject apiResponse = null;
		IOException exception = null;
		for(String server : this.servers) {
			try {
				apiResponse = new Http().makeRequest(
						RequestMethod.GET,
						String.format("%s/player/%s", server, playerName),
						null,
						null,
						null,
						null
				);
			} catch(IOException e) {
				exception = e;
				SkinFixer.logWarn(String.format("An IOException occured while fetching a UUID from the SkinFixer API (%s). Trying other servers (if there are any)", server));
			}
		}

		Triple<Boolean, GetUuidResponse, String> responseTriple = this.checkResponseAndParse(apiResponse, exception, GetUuidResponse.class);

		if(responseTriple.getB() != null) {
			return new Triple<>(responseTriple.getA(), responseTriple.getB().uuid, responseTriple.getC());
		} else {
			return new Triple<>(responseTriple.getA(), null, responseTriple.getC());
		}
	}
	
	public Triple<Boolean, GetSkinResponse, String> getSkin(String skinUrl, boolean slim) {
		String skinUrlBase64 = Base64.getEncoder().encodeToString(skinUrl.getBytes());

		ResponseObject apiResponse = null;
		IOException exception = null;
		for(String server : this.servers) {
			try {
				apiResponse = new Http().makeRequest(RequestMethod.GET, String.format("%s/generate/url/%s", server, skinUrlBase64), new HashMap<>(), null, null, null);
				break;
			} catch(IOException e) {
				exception = e;
				SkinFixer.logWarn(String.format("An IOException occured while fetching a skin from the SkinFixer API (%s). Trying other servers (if there are any)", server));
			}
		}

		return checkResponseAndParse(apiResponse, exception, GetSkinResponse.class);
	}

	@NotNull
	private <T> Triple<Boolean, T, String> checkResponseAndParse(ResponseObject apiResponse, IOException exception, Class<T> parseToClass) {
		if(apiResponse == null) {
			assert exception != null;

			SkinFixer.logWarn(Utils.getStackTrace(exception));
			return new Triple<>(false, null, Utils.getStackTrace(exception));
		}

		if(apiResponse.getResponseCode() != 200) {
			SkinFixer.logWarn("The SkinFixer API returned an unexpected result: " + apiResponse.getConnectionMessage());
			return new Triple<>(false, null, apiResponse.getConnectionMessage());
		}

		final Gson gson = new Gson();
		return new Triple<>(true, gson.fromJson(apiResponse.getMessage(), parseToClass), null);
	}

	public Triple<Boolean, GetSkinResponse, String> getSkinOfPremiumPlayer(String uuid) {
		String uuidBase64 = Base64.getEncoder().encodeToString(uuid.getBytes());
		
		ResponseObject apiResponse = null;
		IOException exception = null;
		for(String server : servers) {
			try {
				apiResponse = new Http().makeRequest(RequestMethod.GET, String.format("%s/generate/uuid/%s", server, uuidBase64), new HashMap<>(), null, null, null);
				break;
			} catch(IOException e) {
				exception = e;
				SkinFixer.logWarn(String.format("An IOException occured while fetching a skin from the SkinFixer API (%s). Trying other servers (if there are any)", server));
			}
		}

		return checkResponseAndParse(apiResponse, exception, GetSkinResponse.class);
	}
}
