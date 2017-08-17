import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MatchDataScraper 
{
	BufferedReader in;
    FileWriter out;
    final String fixtures_file_name = "Fixtures.json"; //name of file where fixture data file is stored, in main directory
    final String fantasy_match_directory = "2016 Fantasy Match Data"; //directory where fantasy match data is saved
    final String detailed_match_directory = "2016 Detailed Match Data"; //directory where detailed match data is saved

    public static void main(String args[]) throws Exception
    {
    	//MatchDataScraper m = new MatchDataScraper();
    	//m.scrapeFixtures();
    	//m.scrapeMatchData();
    }
    
    public void scrapeFixtures() throws IOException
    {
    	//manually check url by using Chrome DevTools Network on http://www.indiansuperleague.com/Fantasy/stats?iswv=0
    	String url = "http://www.indiansuperleague.com/FantasyData/Fixtures/Fixtures_tId_2.json";
    	String json_contents = Jsoup.connect( url )
    			.referrer("http://www.indiansuperleague.com/Fantasy/stats?iswv=0")
    			.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
    			.ignoreContentType( true )
    			.timeout(0)
    			.execute()
    			.body();
    	out = new FileWriter(fixtures_file_name);
    	out.write(json_contents);
    	out.close();
    }
    
    //needs fixture file before scraping data
    public void scrapeMatchData() throws IOException, JSONException
    {
    	//read the fixtures file to get match ids
    	in = new BufferedReader(new FileReader(fixtures_file_name));
    	String file_contents = "";
        String line;
        while ((line = in.readLine()) != null)
        {
        	file_contents += line;
        }
        
        //create directories
        new File(fantasy_match_directory).mkdir();
        new File(detailed_match_directory).mkdir();

        //loop through through matches
        JSONArray fixture_arr = new JSONArray(file_contents);
        for (int match_index = 0; match_index < fixture_arr.length(); match_index++)
        {
        	JSONObject current_match = (JSONObject) fixture_arr.get(match_index);
        	int match_id = current_match.getInt("matchId");
        	getFantasyMatchData(match_id);
        	getDetailedMatchData(match_id);
        	System.out.println("Finished game id " + match_id);
        }
    }
    
    private void getFantasyMatchData(int match_id) throws IOException
    {
    	String url_prefix = "http://www.indiansuperleague.com/FantasyData/PlayerStats/";
    	String file_prefix = "Stats_mId_" + match_id + ".json";
    	String url = url_prefix + file_prefix;
    	String json_contents = Jsoup.connect( url )
    			.referrer("http://www.indiansuperleague.com/Fantasy/stats?iswv=0")
    			.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
    			.ignoreContentType( true )
    			.timeout(0)
    			.execute()
    			.body();
    	String file_name = fantasy_match_directory + File.separator + "Fantasy" + file_prefix;
    	out = new FileWriter(file_name);
    	out.write(json_contents);
    	out.close();
    }
    
    private void getDetailedMatchData(int match_id) throws IOException
    {
    	String url = "http://www.indiansuperleague.com/sifeeds/soccer/live/getmatchdata.aspx?type=match&matchid=" + match_id + "&teams=true&events=true&league=india_sl";
    	String json_contents = Jsoup.connect( url )
    			.userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
    			.ignoreContentType( true )
    			.timeout(0)
    			.execute()
    			.body();
    	String file_name = detailed_match_directory + File.separator + "DetailedStats_mId_" + match_id + ".json";
    	out = new FileWriter(file_name);
    	out.write(json_contents);
    	out.close();
    }
}