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
import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonFileCleaning {

	public static void main(String[] args) {
		
		final String API_KEY = "e171bba5-29fa-41e8-a1af-2c82b601b947";
		
		String file_prefix = "matches";
		for( int i = 1; i < 11; i++ ){
			String json_string = "";
			String champion_json_string = "";
			try{
				List<String> lines = Files.readAllLines( Paths.get( file_prefix + i + ".json" ), Charset.forName("ISO-8859-1") );
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
			for( int j = 0; i < matches.length(); i++ ){
				JSONObject match = matches.getJSONObject( j );
				
				int match_id = match.getInt( "matchId" );
				
				JSONArray participant_identities = match.getJSONArray( "participantIdentities" );
				for( int k = 0; k < participant_identities.length(); k++ ){
					JSONObject participant_identity = participant_identities.getJSONObject( k );
					JSONObject player_identity = participant_identity.getJSONObject( "player" );
					
					int summoner_id = player_identity.getInt( "summonerId" );
					
					JSONObject match_history = executeGetRequest( "https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + summoner_id + "?rankedQueues=RANKED_SOLO_5x5&api_key=" + API_KEY, match_id );
					
					JSONArray match_history_matches = match_history.getJSONArray( "matches" );
					JSONObject first_match = match_history_matches.getJSONObject( 0 );
					if( first_match.getInt( "matchCreation" ) + 30*24*60*60 < System.currentTimeMillis() / 1000 ){
						System.err.println( "Match #" + first_match.getInt( "matchId" ) + " too old, moving on." );
						break;
					}
					
					//Valid match, proceeding
					int target_match_id = match.getInt( "matchId" );
					int target_match_creation_time = match.getInt( "matchCreation" );
					
					JSONArray fetched_participants = first_match.getJSONArray( "participants" );
					HashMap<String, JSONObject> participant_info = new HashMap<String, JSONObject>();
					for( int l = 0; l < fetched_participants.length(); l++ ){
						
						JSONObject participant = fetched_participants.getJSONObject( l );
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
						
						participant_info.put( Integer.toString( participant_id ), participant_insert );
					}
					
					JSONArray fetched_participant_identities = first_match.getJSONArray( "participantIdentities" );
					for( int l = 0; l < fetched_participant_identities.length(); l++ ){
						JSONObject fetched_participant_identity = fetched_participant_identities.getJSONObject( l );
						JSONObject fetched_player_identity = fetched_participant_identity.getJSONObject( "player" );
						
						int fetched_summoner_id = fetched_player_identity.getInt( "summonerId" );
						int fetched_participant_id = fetched_participant_identity.getInt( "participantId" );
						
						JSONObject current_participant = participant_info.get( Integer.toString( fetched_participant_id ) );
						current_participant.put( "summonerId", fetched_summoner_id );
						
						JSONObject searching_match_history = executeGetRequest( "https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + fetched_summoner_id + "?rankedQueues=RANKED_SOLO_5x5&api_key=" + API_KEY, match_id );
						
						JSONArray searching_matches = searching_match_history.getJSONArray( "matches" );
						
						for( int m = 0; m < searching_matches.length(); m++ ){
							JSONObject searching_match = searching_matches.getJSONObject( m );
							int searching_match_id = searching_match.getInt( "matchId" );
							if( searching_match_id == target_match_id ){
								int search_index = m + 1;
								JSONObject desired_match_history = executeGetRequest( "https://na.api.pvp.net/api/lol/na/v2.2/matchhistory/" + fetched_summoner_id + "?beginIndex=" + search_index + "api_key=" + API_KEY );
								JSONArray desired_matches = desired_match_history.getJSONArray( "matches" );
								for( int n = 0; n < desired_matches.length(); n++ ){ //This is getting ridiculous
									JSONObject desired_match = desired_matches.getJSONObject( n );
									int desired_match_id = desired_match.getInt( "matchId" );
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
							}
						}
					}
				}
				System.exit( 0 );
			}
			
		}

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

}
