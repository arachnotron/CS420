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
	 * 
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
	 * Retrieves Summoner objects when given a name. Useful for prediction via user input (web app?)
	 * 
	 * @param Summoners Array of Strings of summoner names. Max 10.
	 * @return JSONObject mapping of name to summoner object ; empty object if failed
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONObject retrieveSummonerObjsByName(String[] Summoners) throws MalformedURLException, IOException {
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
			return new JSONObject();
		}
		
		return summonerData;
	}
	
	/**
	 * Retrieves stats summary for player by game type. Includes wins, losses, and aggregate stats.
	 * 
	 * @param SummonerID long ID for player
	 * @param Gametype String game type [RankedSolo5x5, Unranked, etc.]
	 * @return JSONObject mapping of ID to stat summary ; empty object if failure
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static JSONObject retrieveSummonerStatSummary(long SummonerID, String Gametype) throws MalformedURLException, IOException {
		String baseUrl = "v1.3/stats/by-summoner/{summonerId}/summary";
		String summoner = ""+SummonerID;
		
		JSONObject summonerData;
		try {
			summonerData = sendGet(baseUrl.replace("{summonerId}", summoner), "");
		} catch (NullPointerException e) {
			// Didn't work. Just return.
			return new JSONObject();
		}
		
		// Map of stats to player ID
		JSONObject statMap = new JSONObject();

		JSONArray statArray = summonerData.getJSONArray("playerStatSummaries");
		
		for (int i = 0 ; i < statArray.length() ; i++) {
			JSONObject mode = statArray.getJSONObject(i);
			
			if (mode.getString("playerStatSummaryType").equals(Gametype)) {
				statMap.append(""+summonerData.getLong("summonerId"), mode);
				break;
			}
		}
		
		return statMap;
	}
	
	public static void retrieveChampionWinRate(long SummonerID, String Champion) {
		
	}
	
	public static void retrieveRoleWinRate(String Champion, String Role) {
		
	}
	
	/**
	 * Retrieves league information for group of players by ID.
	 * 
	 * Format is a list of leagues that player is a part of (RANKED_SOLO_5x5, RANKED_TEAM_3x3, 
	 * RANKED_TEAM_5x5) with tier (BRONZE, SILVER, GOLD, DIAMOND, PLATINUM, CHALLENGER) and 
	 * finally a list of all player objects in the current tier ladder. The player objects contain 
	 * information on wins, losses, division, "league points", and whether or not they are in their 
	 * first 10 league games ever. Also gives player/team name and ID.
	 * 
	 * For future classification, victory should be weighted toward the higher tiers.
	 * 
	 * @param SummonerIDs long[] player IDs. Max 10.
	 * @return JSONObject mapping of IDs to list of leagues they are a part of ; empty object if failure
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	public static JSONObject retrieveLeague(long[] SummonerIDs) throws MalformedURLException, IOException {
		String baseUrl = "v2.5/league/by-summoner/{summonerIds}";
		StringBuffer summoner = new StringBuffer();
		
		for (long id : SummonerIDs) {
			summoner.append(id);
			summoner.append(",");
		}
		
		// Just clear the last comma :)
		summoner.deleteCharAt(summoner.lastIndexOf(","));
		
		JSONObject summonerData;
		try {
			summonerData = sendGet(baseUrl.replace("{summonerIds}", summoner), "");
		} catch (NullPointerException e) {
			// Didn't work. Just return.
			return new JSONObject();
		}
		
		return summonerData;
	}
	
	// test
	public static void main(String[] args) {
		try {
			String[] summoners = new String[2];
			summoners[0] = "jbiebin";
			summoners[1] = "solidzer0";
			
			System.out.println(retrieveSummonerObjsByName(summoners).toString());
			
			System.out.println(retrieveSummonerStatSummary(22559384, "RankedSolo5x5").toString());
			System.out.println(retrieveSummonerStatSummary(83325, "RankedSolo5x5").toString());
			
			long[] ids = new long[2];
			ids[0] = 22559384;
			ids[1] = 83325;
			
			System.out.println(retrieveLeague(ids).toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
