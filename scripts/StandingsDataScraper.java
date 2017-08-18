package scripts;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class StandingsDataScraper
{
    FileWriter out;
    final String standings_directory = "2014-2016 Standings Data"; // directory where standings data is saved

    public static void main(String args[]) throws Exception
    {
        StandingsDataScraper s = new StandingsDataScraper();
        s.scrapeStandings();
    }

    public void scrapeStandings() throws IOException
    {
        // create directory
        new File(standings_directory).mkdir();

        for (int year = 2014; year < 2017; year++)
        {
            getFantasyMatchData(year);
        }
    }

    private void getFantasyMatchData(int year) throws IOException
    {
        String url = "http://matchcentre.starsports.com/football_data/static/api.aspx?id=MVNwMHJ0ekFQSQ==&league=india_sl&endpoints=standings/?season=" + year + "&accept=json";
        String json_contents = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                .ignoreContentType(true)
                .timeout(0)
                .execute()
                .body();
        String file_name = standings_directory + File.separator + year + "Standings.json";
        out = new FileWriter(file_name);
        out.write(json_contents);
        out.close();
    }
}