package RiotAPI;

import java.io.IOException;
import java.net.*;

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
	private static void sendGet(String url, String options) throws MalformedURLException, IOException {
		String newUrl = BASE_URL.replace("{url}", url).replace("{options}", options).replace("{apikey}", API_KEY);
		
		// Instantiate & open the connection to the API
		URL get = new URL(newUrl);
		HttpURLConnection conn = (HttpURLConnection) get.openConnection();
		
		// Set request method & add to the request header.
		conn.setRequestMethod("GET");
		conn.setRequestProperty("USER_AGENT", USER_AGENT);
		
		// Dictate next action based on response
		switch (conn.getResponseCode()) {
			case(HttpURLConnection.HTTP_ACCEPTED):
				conn.disconnect();
				return;
			case(423): // HTTP_LOCKED, too many requests
				conn.disconnect();
				return;
		}
	}
	
	public static void retrieveSummonerID(String Summoner) throws MalformedURLException, IOException {
		sendGet("", "");
	}
	
	public static void retrieveWinRate(long SummonerID) {
		
	}
	
	public static void retrieveChampionWinRate(long SummonerID, String Champion) {
		
	}
	
	public static void retrieveRoleWinRate(String Champion, String Role) {
		
	}
	
	public static void retrieveLeague(long SummonerID) {
		
	}
}
