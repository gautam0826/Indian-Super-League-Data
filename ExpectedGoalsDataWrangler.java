import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExpectedGoalsDataWrangler 
{
	BufferedReader in;
    FileWriter out;
    final String fixtures_file_name = "Fixtures.json"; //name of file where fixture data file is stored, in main directory
    final String fantasy_match_directory = "2016 Fantasy Match Data"; //directory where fantasy match data is saved
    final String detailed_match_directory = "2016 Detailed Match Data"; //directory where detailed match data is saved
    final String output_file_name = "XGdata.csv";

    public static void main(String args[]) throws Exception
    {
    	ExpectedGoalsDataWrangler e = new ExpectedGoalsDataWrangler();
    	e.wrangle();
    }
    
    public void wrangle() throws IOException, JSONException, ParseException
    {
    	out = new FileWriter(output_file_name);
    	out.write("Match,Time,Result,Player,Team,Opponent,X ball,Y ball,X player,Y player,Game State,Crossed?,Not from Cross,Indirectly from Cross,Directly from Cross\n");

    	//loop through all files in the detailed match directory
    	File folder = new File(detailed_match_directory);
    	File[] fileList = folder.listFiles();
    	for (File file : fileList) 
    	{
    		if (file.isFile())
    	    {
    			wrangleMatchData(file.getName());
    	    }
    	}
    }
    
    private void wrangleMatchData(String match_file_name) throws IOException, JSONException, ParseException
    {
    	in = new BufferedReader(new FileReader(detailed_match_directory + File.separator + match_file_name));
    	String file_contents = "";
        String line;
        while ((line = in.readLine()) != null)
        {
        	file_contents += line;
        }
        
        //used to store time from last cross
        SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
        Date last_cross = null;

        JSONObject match_obj = new JSONObject(file_contents);
        String match_number = match_obj.getJSONObject("match_detail").getString("match_number");
        JSONArray teams = match_obj.getJSONArray("teams");
        JSONObject home_team = (JSONObject) teams.get(0);
        JSONObject away_team = (JSONObject) teams.get(1);
        String home_team_name = home_team.getString("name");
        String away_team_name = away_team.getString("name");

        //loop through all match events
        JSONArray events = match_obj.getJSONArray("events");
        for (int event_index = 0; event_index < events.length(); event_index++)
        {
        	JSONObject current_event = (JSONObject) events.get(event_index);
        	String event_type = current_event.getString("event");
        	if (event_type.equals("Goal") || event_type.equals("On Target") || event_type.equals("Off Target"))
        	{
        		//generic info
        		JSONObject offensive_player = current_event.getJSONObject("offensive_player");
        		String player_name = offensive_player.getString("player_name");
        		String team_name = current_event.getString("team_name");

        		//coordinate calculations
        		JSONObject ball_coordinates = current_event.getJSONObject("ball_coordinates");
        		JSONObject player_coordinates = offensive_player.getJSONObject("player_coordinates");
        		double x_ball = Double.NaN;
        		double y_ball = Double.NaN;
        		double x_player = Double.NaN;
        		double y_player = Double.NaN;
        		try
        		{
            		x_ball = ball_coordinates.getDouble("x");
            		y_ball = ball_coordinates.getDouble("y");
        		}
        		catch (JSONException e) //x and y ball coordinates are null
        		{}
        		try
        		{
            		x_player = player_coordinates.getDouble("x");
            		y_player = player_coordinates.getDouble("y");
        		}
        		catch (JSONException e) //x and y player coordinates are null
        		{}
        		
        		//game state calculation / opponent info
        		JSONArray current_score = current_event.getJSONArray("score");
        		int home_score = current_score.getInt(0);
        		int away_score = current_score.getInt(1);
        		int game_state = home_score - away_score;
        		String opponent_name = away_team_name;
        		if (team_name.equals(away_team_name))
        		{
        			game_state = -game_state;
        			opponent_name = home_team_name;
        		}
        		
        		//time since last cross calculation
        		//first part of resulting string is normal text(cross/indirect/direct) and the second part is a binarized version of the same data
        		JSONObject time = current_event.getJSONObject("time");
        		int minutes = time.getInt("minutes") + time.getInt("additional_minutes");
        		int seconds = time.getInt("seconds");
        		String time_stamp = minutes + ":" + seconds;
        		Date current_shot = dateFormat.parse(time_stamp);
        		String cross_state = "Not from Cross,1,0,0";
        		if (last_cross != null)
        		{
            		long time_since_cross = Math.abs(current_shot.getTime() - last_cross.getTime())/1000; //sometimes events are slightly out or order
            		if (time_since_cross <= 2)
            		{
            			cross_state = "Directly from Cross,0,0,1";
            		}
            		else if (time_since_cross <= 5)
            		{
            			cross_state = "Indirectly from Cross,0,1,0";
            		}
        		}
        		
        		//write to output file
        		out.write(match_number + "," + time_stamp + "," + event_type + "," + player_name + "," + team_name + "," + opponent_name + "," + x_ball + "," + y_ball + "," + x_player + "," + y_player + "," + game_state + "," + cross_state + "\n");
        		out.flush();
        	}
        	else if (event_type.equals("Cross"))//keep track of last cross
        	{
        		JSONObject time = current_event.getJSONObject("time");
        		int minutes = time.getInt("minutes") + time.getInt("additional_minutes");
        		int seconds = time.getInt("seconds");
        		last_cross = dateFormat.parse(minutes + ":" + seconds);
        	}
        	else if (event_type.equals("Start of Second Half"))//cross at 44:59 and goal at 45:01 on different sides of half are not corresponding
        	{
        		last_cross = null;
        	}
        }
    }
}