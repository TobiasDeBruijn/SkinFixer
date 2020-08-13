package nl.thedutchmc.SkinFixer.changeSkin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class CheckUserAgainstMojang {

	public static String premiumUser(String playername) {		
		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://api.mojang.com/profiles/minecraft");
		
		String jsonOut = "[\"" + playername + "\"]";
				
		StringEntity requestEntity = new StringEntity(
				jsonOut,
			    ContentType.APPLICATION_JSON);
		
		httpPost.setEntity(requestEntity);
		
		HttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		HttpEntity entity = response.getEntity();
		
		String json = "";
		try {
			json = EntityUtils.toString(entity, StandardCharsets.UTF_8);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		JSONTokener tokener = new JSONTokener(json);
		JSONArray array = (JSONArray) tokener.nextValue();
		for(Object o : array) {
			JSONObject jsonObj = (JSONObject) o;
			String uuid = (String) jsonObj.getString("id");
			return insertDashUUID(uuid);
		}
			
		return null;
	}
	
	  private static String insertDashUUID(String uuid) {
		    StringBuilder sb = new StringBuilder(uuid);
		    sb.insert(8, "-");
		    sb = new StringBuilder(sb.toString());
		    sb.insert(13, "-");
		    sb = new StringBuilder(sb.toString());
		    sb.insert(18, "-");
		    sb = new StringBuilder(sb.toString());
		    sb.insert(23, "-");

		    return sb.toString();
		  }
}
