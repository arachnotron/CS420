package DataCleaning;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonFileCleaning {
	
	private static final String API_KEY = "e171bba5-29fa-41e8-a1af-2c82b601b947";
	private static final String BASE_URL = "https://na.api.pvp.net/api/lol/na/";

	public static void main(String[] args) {
		
		if( args.length != 1 ){
			usage();
		}
		
		String file_name = args[0];
		
		//go through each file
		String json_string = "";
		String champion_json_string = "";
		try{
			List<String> lines = Files.readAllLines( Paths.get( file_name ), Charset.forName("ISO-8859-1") );
			ListIterator<String> lines_iterator = lines.listIterator();
			while( lines_iterator.hasNext() ){
				json_string += lines_iterator.next();
			}
			
			List<String> champ_lines = Files.readAllLines( Paths.get( "ChampionIDWinRates.json" ), Charset.forName( "UTF-8" ) );
			ListIterator<String> champ_lines_iterator = champ_lines.listIterator();
			while( champ_lines_iterator.hasNext() ){
				champion_json_string += champ_lines_iterator.next();
			}
			
		}catch( IOException e ){
			System.err.println( e);
		}
		
		JSONObject champ_wr = new JSONObject( champion_json_string );
		JSONArray matches = new JSONObject( json_string ).getJSONArray( "matches" );
		//loop through matches in offline file
		for( int j = 0; j < matches.length(); j++ ){
			JSONObject match = matches.getJSONObject( j );
			
			int match_id = match.getInt( "matchId" );
			
			//get players from offline matches
			JSONArray participant_identities = match.getJSONArray( "participantIdentities" );
			for( int k = 0; k < participant_identities.length(); k++ ){
				JSONObject participant_identity = participant_identities.getJSONObject( k );
				JSONObject player_identity = participant_identity.getJSONObject( "player" );
				
				int summoner_id = player_identity.getInt( "summonerId" );
	
				JSONObject first_match = checkValidGame( summoner_id, match_id );
				
				//Valid match, proceeding
				int target_match_id = first_match.getInt( "matchId" );
				int target_match_creation_time = first_match.getInt( "matchCreation" );
				
				//got a match with data we want, compile player information
				JSONArray first_match_participants = first_match.getJSONArray( "participants" );
				HashMap<String, JSONObject> participant_info = new HashMap<String, JSONObject>();
				for( int l = 0; l < first_match_participants.length(); l++ ){
					
					JSONObject participant = first_match_participants.getJSONObject( l );
					int participant_id = participant.getInt( "participantId" );
					int champion_id = participant.getInt( "championId" );
					int team_id = participant.getInt( "teamId" );
					Double champ_win_rate = champ_wr.getDouble( Integer.toString( champion_id ) );
					
					JSONObject participant_insert = new JSONObject();
					participant_insert.put( "championId", champion_id );
					participant_insert.put( "teamId", team_id );
					participant_insert.put( "championWinRate", champ_win_rate );
					participant_insert.put( "recentWonGames", 0 );
					participant_insert.put( "recentPlayedGames", 0 );
					participant_insert.put( "championPicked", 0 );
					participant_insert.put( "championPlayedGames", 0 );
					participant_insert.put( "championWonGames", 0 );
					
					participant_info.put( Integer.toString( participant_id ), participant_insert );
				}
				
				//getPreviousStats();
				
				JSONArray first_match_participant_identities = first_match.getJSONArray( "participantIdentities" );
				String summoner_id_list = "";
				for( int l = 0; l < first_match_participant_identities.length(); l++ ){
					if( summoner_id_list != "" ){
						summoner_id_list += ",";
					}
					JSONObject first_match_participant_identity = first_match_participant_identities.getJSONObject( l );
					JSONObject fetched_player_identity = first_match_participant_identity.getJSONObject( "player" );
					
					int first_match_summoner_id = fetched_player_identity.getInt( "summonerId" );
					int first_match_participant_id = first_match_participant_identity.getInt( "participantId" );
					
					summoner_id_list += first_match_summoner_id;

					JSONObject current_participant = participant_info.get( Integer.toString( first_match_participant_id ) );
					current_participant = buildPlayerApiInfo( current_participant, first_match_summoner_id, match_id, target_match_id, target_match_creation_time );
				}
				
				JSONObject first_match_league_info = getLeagueInfo( summoner_id_list );
			}
			System.exit( 0 );
		}

	}
	
	public static JSONObject getLeagueInfo( String summoner_id_list ){
		JSONObject league_info = executeGetRequest( BASE_URL + "v2.5/league/by-summoner/" + summoner_id_list + "/entry?api_key=" + API_KEY );
		
		JSONObject league_info_return = new JSONObject();
		
		Iterator<String> summoner_id_iterator = league_info.keys();
		while( summoner_id_iterator.hasNext() ){
			String summoner_id = summoner_id_iterator.next();
			
			JSONArray summoner_league_info = league_info.getJSONArray( summoner_id );
			for( int n = 0; n < summoner_league_info.length(); n++ ){
				JSONObject one_league_info = summoner_league_info.getJSONObject( n );
				
				if( one_league_info.getString( "queue" ) == "RANKED_SOLO_5x5" ){
					String tier = one_league_info.getString( "tier" );
					JSONObject entry = one_league_info.getJSONArray( "entries" ).getJSONObject( 0 );
					int division = getDivisionMapping( entry.getString( "division" ) );
					
					JSONObject league_info_bin = new JSONObject();
					league_info_bin.put( "tier", tier );
					league_info_bin.put( "division",  division );
					
					league_info_return.put( summoner_id, league_info_bin );
				}
			}
		}
		
		return league_info_return;
	}
	
	public static int getDivisionMapping( String division ){
		switch( division ){
			case "I":
				return 1;
			case "II":
				return 2;
			case "III":
				return 3;
			case "IV":
				return 4;
			case "V":
				return 5;
			default:
				return 0;
		}
	}
	
	public static int getTierMapping( String tier ){
		switch( tier ){
			case "BRONZE":
				return 1;
			case "SILVER":
				return 2;
			case "GOLD":
				return 3;
			case "PLATINUM":
				return 4;
			case "DIAMOND":
				return 5;
			case "MASTER":
				return 6;
			case "CHALLENGER":
				return 7;
		}
		return 8;
	}
	
	public static JSONObject buildPlayerApiInfo( JSONObject current_participant, int summoner_id, int match_id, int target_match_id, int target_match_creation_time ){
		current_participant.put( "summonerId", summoner_id );
		
		int search_index = -1;
		int begin_index = 0;
		while( search_index == -1 ){
			//get player ranked match history
			JSONObject participant_match_history = executeGetRequest( BASE_URL + "v2.2/matchhistory/" + summoner_id + "?beginIndex=" + begin_index + "&rankedQueues=RANKED_SOLO_5x5&api_key=" + API_KEY, match_id );
			
			JSONArray participant_matches = participant_match_history.getJSONArray( "matches" );
	
			//get the index of this player for the desired match in their match history
			for( int m = 0; m < participant_matches.length(); m++ ){
				JSONObject participant_match = participant_matches.getJSONObject( m );
				int participant_match_id = participant_match.getInt( "matchId" );
				if( participant_match_id == target_match_id ){
					search_index = m + 1;
					JSONObject desired_match_history = executeGetRequest( "https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + summoner_id + "?beginIndex=" + search_index + "&api_key=" + API_KEY );
					JSONArray desired_matches = desired_match_history.getJSONArray( "matches" );
					//parse previous matches before desired match
					for( int n = 0; n < desired_matches.length(); n++ ){ //This is getting ridiculous
						JSONObject desired_match = desired_matches.getJSONObject( n );
						//int desired_match_id = desired_match.getInt( "matchId" ); //may not be needed
						int desired_match_creation = desired_match.getInt( "matchCreation" );
						
						int desired_champion_id = desired_match.getJSONArray( "participants" ).getJSONObject( 0 ).getInt( "championId" );
						int recent_won_games = current_participant.getInt( "recentWonGames" );
						int recent_played_games = current_participant.getInt( "recentPlayedGames" );
						int champion_picked = current_participant.getInt( "recentPlayedGames" );
						if( desired_champion_id == current_participant.getInt( "championId" ) ){
							champion_picked++;
							if( desired_match_creation <= target_match_creation_time + 24*60*60 ){
								recent_won_games++;
								recent_played_games++;
							}
						}
						current_participant.put( "recentWonGames", recent_won_games );
						current_participant.put( "recentPlayedGames", recent_played_games );
						current_participant.put( "championPicked", champion_picked );
					}
					
					JSONObject champ_wr_mh = executeGetRequest( BASE_URL + "v2.2/matchhistory/" + summoner_id + "?beginIndex=" + search_index + "&championIds=" + current_participant.getDouble( "championId") + "&api_key=" + API_KEY );
					JSONArray champ_wr_matches = champ_wr_mh.getJSONArray( "matches" );
					for( int n = 0; n < champ_wr_matches.length(); n++ ){
						JSONObject champ_wr_match = champ_wr_matches.getJSONObject( n );
						
						int champion_played_games = current_participant.getInt( "championPlayedGames" );
						int champion_won_games = current_participant.getInt( "championWonGames" );
						
						JSONObject champ_wr_match_stats = champ_wr_match.getJSONObject( "stats" );
						Boolean winner = champ_wr_match_stats.getBoolean( "winner" );
						champion_played_games++;
						if( winner ){
							champion_won_games++;
						}
						current_participant.put( "championPlayedGames", champion_played_games );
						current_participant.put( "championWonGames", champion_won_games );
					}
				}
			}
			begin_index += 15;
		}
		return current_participant;
	}
	
	public static JSONObject checkValidGame( int summoner_id, int match_id ){
		
		String url = "https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + summoner_id + "?rankedQueues=RANKED_SOLO_5x5&api_key=" + API_KEY;
		
		JSONObject match_history = executeGetRequest( url, match_id );
		JSONArray match_history_matches = match_history.getJSONArray( "matches" );
		JSONObject first_match = match_history_matches.getJSONObject( 0 ); //get most recent match
		//check if most recent match was played lass than a month ago
		if( first_match.getInt( "matchCreation" ) + 30*24*60*60 < System.currentTimeMillis() / 1000 ){
			System.err.println( "Match #" + first_match.getInt( "matchId" ) + " too old, moving on." );
			return null;
		}
		return first_match;
	}
	
	public static JSONObject executeGetRequest( String url, int match_id ){
		try{
			URL mh_url = new URL( url );
			HttpURLConnection con = (HttpURLConnection) mh_url.openConnection();
			int responseCode = con.getResponseCode();
			if( responseCode != 200 ){
				System.err.println( "Response Code " + responseCode + " received. Match # " + match_id );
				System.exit( 1 );
			}
			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return new JSONObject( response.toString() );
			
		}catch( Exception e ){
			System.err.println( e );
			return null;
		}
	}
	
	public static JSONObject executeGetRequest( String url ){
		try{
			URL mh_url = new URL( url );
			HttpURLConnection con = (HttpURLConnection) mh_url.openConnection();
			int responseCode = con.getResponseCode();
			if( responseCode != 200 ){
				System.err.println( "Response Code " + responseCode + " received." );
				System.exit( 1 );
			}
			BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return new JSONObject( response.toString() );
			
		}catch( Exception e ){
			System.err.println( e );
			return null;
		}
	}

	
	public static void usage(){
		System.err.println( "Usage: JsonFileCleaning <file_name>");
	}
	
}
