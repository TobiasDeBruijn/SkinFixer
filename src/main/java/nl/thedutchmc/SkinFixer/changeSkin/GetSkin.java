package nl.thedutchmc.SkinFixer.changeSkin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class GetSkin {

	
	public static String getSkin(String skinUrl) {

		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost("https://api.mineskin.org/generate/url");

		// Request parameters and other properties.
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("User-Agent", "Java Spigot Plugin"));
		params.add(new BasicNameValuePair("url", skinUrl));
		
		try {
			httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			
			//Execute and get the response.
			HttpResponse response = httpclient.execute(httppost);
			Scanner sc = new Scanner(response.getEntity().getContent());
			
			StringBuilder responseBuilder = new StringBuilder();
			
			while(sc.hasNext()) {
				responseBuilder.append(sc.next());
			}
			
			sc.close();
			return responseBuilder.toString();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
