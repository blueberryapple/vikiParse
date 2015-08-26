import java.net.*;
import java.time.Instant;
import java.io.*;
import org.apache.commons.io.*;
import com.google.gson.*;

public class vikiParse
{
  private static String vikiApi = "http://api.viki.io/v4/";
  private static String appId = "100444a";

  private static String getSeriesId(String name) throws IOException
  {
    String lookUp = "search.json?c=" + name + "&";
    String url = vikiApi + lookUp + "app=" + appId;

    URL website = new URL(url);
    FileUtils.copyURLToFile(website, new File("search.json"));

    JsonParser parser = new JsonParser();
    String id = parser.parse(new FileReader("search.json"))
                      .getAsJsonArray()
                      .get(0)
                      .getAsJsonObject()
                      .get("id")
                      .getAsJsonPrimitive()
                      .getAsString();

    //vikiParse.printPretty("search.json");
    //System.out.println(id);
    return id;
  }

  private static void printPretty(String file) throws IOException
  {
    Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();
    
    JsonParser parser = new JsonParser();
    JsonElement parsed = parser.parse(new FileReader(file));
    String jsonOutput = gson.toJson(parsed);
    System.out.println(jsonOutput);
  }
  
  private static void getJson(String id) throws IOException
  {
    // make the url to get json data
    String lookUp = "containers/" + id + "/episodes.json?";
    //long time = Instant.now().getEpochSecond();

    String url = vikiApi + lookUp + "app=" + appId /*+ "&t=" + time +
      "&sig=" + secret + "&subtitle_completion=en"*/; 

    URL website = new URL(url);

    // writes json to file
    FileUtils.copyURLToFile(website, new File("episodes.json"));
    //printPretty("episodes.json");
  }

  private static int printJson(int desired) throws IOException
  {
    JsonParser parser = new JsonParser();
    JsonElement parsed = parser.parse(new FileReader("episodes.json"));
    
    JsonArray filteredParsed = parsed.getAsJsonObject()
                                     .get("response")
                                     .getAsJsonArray();

    // calculates the position of the episode in the json array
    // this is better than trying to reverse the entire array
    int episode = filteredParsed.size();
    episode -= desired;

    // if episode desired isn't out yet
    if(episode < 0)
    {
      return -1;
    }

    // filters json data to retrieve en sub completion
    int percentSubbed = filteredParsed.get(episode)
                                      .getAsJsonObject()
                                      .get("subtitle_completions")
                                      .getAsJsonObject()
                                      .get("en")
                                      .getAsJsonPrimitive()
                                      .getAsInt();

    // prints out the subtitle completion
    System.out.println("Episode " + desired + " subbed " + percentSubbed
      + "%");

    return percentSubbed;
  }

  private static void checkSub(int watchable, int percent)
  {
    if(watchable <= percent)
    {
      System.out.println("Expectations met!");
    }
    else
      System.out.println("Unfulfilled desires.");
  }

  public static void main(String args[]) throws IOException
  {
    System.out.println("vikiParse!");
    
    String id = vikiParse.getSeriesId(args[0]);
    vikiParse.getJson(id);
    int percent = vikiParse.printJson(new Integer(args[1]));

    checkSub(new Integer(args[2]), percent);
  }
}
