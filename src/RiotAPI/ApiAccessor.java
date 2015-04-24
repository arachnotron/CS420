package RiotAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import org.json.*;

/**
 * Final, singleton class to directly access Riot's RESTful API
 * @author Jacob
 */
public final class ApiAccessor {
	private static final String API_KEY = "c11103a9-3464-4cfd-a738-3c0df33eb4db";
	private static final String BASE_URL = "https://na.api.pvp.net/api/lol/na/{url}?{options}api_key={apikey}";
	private static final String USER_AGENT = "Mozilla/5.0";
	
	/**
	 * HTTPGET request accessory function
	 * @param url The API function we are attempting to access
	 * @param options Optional request parameters
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private static JSONObject sendGet(String url, String options) throws MalformedURLException, IOException {
		// Build URL. If options != empty string, add & separator
		if (!options.isEmpty())
			options += "&";
		String newUrl = BASE_URL.replace("{url}", url).replace("{options}", options).replace("{apikey}", API_KEY);
		
		// Instantiate & open the connection to the API
		URL get = new URL(newUrl);
		HttpURLConnection conn = (HttpURLConnection) get.openConnection();
		
		// Set request method & add to the request header.
		conn.setRequestMethod("GET");
		conn.setRequestProperty("USER_AGENT", USER_AGENT);
		
		// Dictate next action based on response
		if (conn.getResponseCode() != HttpURLConnection.HTTP_OK){
			System.err.println("Failed [Code " + conn.getResponseCode() + "] on " + newUrl);
			conn.disconnect();
			return null;
		}
		
		BufferedReader br = new BufferedReader( new InputStreamReader(conn.getInputStream()) );
		StringBuffer sbuff = new StringBuffer();
		String input;
		
		while( (input = br.readLine()) != null ) {
			sbuff.append(input);
		}
		
		JSONObject response = new JSONObject(sbuff.toString());
		
		conn.disconnect();
		return response;
	}
	
	/**
	 * Retrieves Summoner ID's when given a name. Useful for prediction via user input (web app?)
	 * @param Summoners Array of Strings of summoner names
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void retrieveSummonerIDs(String[] Summoners) throws MalformedURLException, IOException {
		String baseUrl = "v1.4/summoner/by-name/{summonerNames}";
		StringBuffer summoner = new StringBuffer();
		
		for (String name : Summoners) {
			summoner.append(name);
			summoner.append(",");
		}
		
		// Just clear the last comma :)
		summoner.deleteCharAt(summoner.lastIndexOf(","));
		
		JSONObject summonerData;
		try {
			summonerData = sendGet(baseUrl.replace("{summonerNames}", summoner), "");
		} catch (NullPointerException e) {
			// Didn't work. Just return.
			return;
		}
		
		// for now we're just gonna print stuff
		for (String key : summonerData.keySet()) {
			JSONObject summonerObj = summonerData.getJSONObject(key);
			System.out.println(summonerObj.get("name") + ": " + summonerObj.get("id"));
		}
	}
	
	public static void retrieveWinRate(long SummonerID) {
		
	}
	
	public static void retrieveChampionWinRate(long SummonerID, String Champion) {
		
	}
	
	public static void retrieveRoleWinRate(String Champion, String Role) {
		
	}
	
	public static void retrieveLeague(long SummonerID) {
		
	}
	
	// test
	public static void main(String[] args) {
		try {
			String[] summoners = new String[2];
			summoners[0] = "jbiebin";
			summoners[1] = "solidzer0";
			
			retrieveSummonerIDs(summoners);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
