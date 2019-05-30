package sentiment;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TweetUtils {

    public static ArrayList<Tweet> parseTweetJson(String filename) {
        ArrayList<Tweet> tweets = new ArrayList<>();

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(filename)) {
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray TwList = (JSONArray) obj;
            //System.out.println(TwList);

            //Iterate over tw array
            TwList.forEach(tweet -> tweets.add(parseTwObject((JSONObject) tweet)));


        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return tweets;
    }

    public static void countFrequenciesString(ArrayList<String> tweetUsernames) {
        int a=0;
        // hashmap to store the frequency of element
        Map<String, Integer> hm = new HashMap<String, Integer>();

        for (String i : tweetUsernames) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        // displaying the occurrence of elements in the arraylist
        for (Map.Entry<String, Integer> val : hm.entrySet()) {
            System.out.println("Element " + val.getKey() + " "
                    + "occurs"
                    + ": " + val.getValue() + " times" );
            a = a+ val.getValue();
        }
        System.out.println(a + "tot");
    }

    public static void countFrequenciesBool(ArrayList<Boolean> followerCount) {
        // hashmap to store the frequency of element
        Map<Boolean, Integer> hm = new HashMap<Boolean, Integer>();

        for (Boolean i : followerCount) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }

        // displaying the occurrence of elements in the arraylist
        for (Map.Entry<Boolean, Integer> val : hm.entrySet()) {
            System.out.println("Element " + val.getKey() + " "
                    + "occurs"
                    + ": " + val.getValue() + " times");
        }
    }

    public static void countFrequenciesLong(ArrayList<Long> defaultProfiles){

        // hashmap to store the frequency of element
        Map<Long, Integer> hm = new HashMap<Long, Integer>();

        for (Long i : defaultProfiles) {
            Integer j = hm.get(i);
            hm.put(i, (j == null) ? 1 : j + 1);
        }
        int p1 = 0, p2 = 0, p3 = 0, p4 = 0, p5 = 0, n=0;
        p1= 50;
        p2 = 100;
        p3 = 400;
        p4 = 5000;
        p5 = 10000;
        int i1 = 0,i2= 0,i3= 0,i4= 0,i5= 0, i6=0;
        // displaying the occurrence of elements in the arraylist
        for (Map.Entry<Long, Integer> val : hm.entrySet()) {
            if(val.getKey()==null)
                n=n+val.getValue();
            if(val.getKey()>=0 && val.getKey()<=p1)
                i1=i1+val.getValue();
            if(val.getKey()>p1 && val.getKey()<=p2)
                i2=i2+val.getValue();
            if(val.getKey()>p2 && val.getKey()<=p3)
                i3=i3+val.getValue();
            if(val.getKey()>p3 && val.getKey()<=p4)
                i4=i4+val.getValue();
            if(val.getKey()>p4 && val.getKey()<=p5)
                i5=i5+val.getValue();
            if(val.getKey()>p5)
                i6=i6+val.getValue();
        }
        System.out.println("Element range 0" + " - " + p1
                + " occurs "
                + ": " + i1 + " times");
        System.out.println("Element range " + p1 + " - " + p2
                + " occurs "
                + ": " + i2 + " times");
        System.out.println("Element range " + p2 + " - " + p3
                + " occurs "
                + ": " + i3 + " times");
        System.out.println("Element range " + p3 + " - " + p4
                + " occurs "
                + ": " + i4 + " times");
        System.out.println("Element range " + p4+ " - " + p5
                + " occurs "
                + ": " + i5 + " times");
        System.out.println("Element over " + p5
                + " occurs "
                + ": " + i6 + " times");
        System.out.println("tot : "+ (i1+i2+i3+i4+i5+i6) + ", null : " + n );

    }

    private static Tweet parseTwObject(JSONObject Tweets) {
        //Get language
        String lang = (String) Tweets.get("lang");
        //  System.out.println("language " + lang);

        //Get tweet object (status) within list
        JSONObject TwS = (JSONObject) Tweets.get("status");

        //Get saved
        boolean saved = (boolean) TwS.get("saved");
        //   System.out.println("saved " + saved);

        //Get sentiment
        String sentiment = (String) TwS.get("sentiment");
        //  System.out.println("sentiment " + sentiment);

        //Get tweet object (stats) within list
        JSONObject TwSt = (JSONObject) Tweets.get("stats");

        //Get virality
        long virality = (long) TwSt.get("virality");
        // System.out.println("virality " + virality);

        //Get score
        long score = (long) TwSt.get("score");
        //  System.out.println("score " + score);

        //Get reach
        long reach = (long) TwSt.get("reach");
        // System.out.println("reach " + reach);

        //Get weight
        long weight = (long) TwSt.get("weight");
        //  System.out.println("weight " + weight);

        //Get tweet object (author) within list
        JSONObject TwAu = (JSONObject) Tweets.get("author");

        //Get followReqSent
        String follow_request_sent = (String) TwAu.get("follow_request_sent");
        // System.out.println("follow request sent " + follow_request_sent);

        //Get id
        long id = (long) TwAu.get("id");
        // System.out.println("id " + id);

        //Get description
        String description = (String) TwAu.get("description");
        //System.out.println("description " + description);

        //Get verified
        boolean verified = (boolean) TwAu.get("verified");
        //System.out.println("verified " + verified);

        //Get profile_text_color
        String profile_text_color = (String) TwAu.get("profile_text_color");
        //System.out.println("profile text color " + profile_text_color);

        //Get followers_count
        long followers_count = (long) TwAu.get("followers_count");
        //System.out.println("followers " + followers_count);

        //Get protected
        boolean protecte = (boolean) TwAu.get("protected");
        //System.out.println("protected " + protecte);

        //Get location
        String location = (String) TwAu.get("location");
        //System.out.println("location " + location);

        //Get default_profile_image
        boolean default_profile_image = (boolean) TwAu.get("default_profile_image");
        //System.out.println("default profile image " + default_profile_image);

        //Get id_str
        String id_str = (String) TwAu.get("id_str");
        //System.out.println("id in forma stringa " + id_str);

        //Get username
        String username = (String) TwAu.get("username");
        //System.out.println("username " + username);

        //Get statuses_count
        long statuses_count = (long) TwAu.get("statuses_count");
        //System.out.println("statuses " + statuses_count);

        //Get friends_count
        long friends_count = (long) TwAu.get("friends_count");
        //System.out.println("friends " + friends_count);

        //Get geo_enabled
        boolean geo_enabled = (boolean) TwAu.get("geo_enabled");
        //System.out.println("geo enabled " + geo_enabled);

        //Get friends_count
        String langAu = (String) TwAu.get("lang");
        //System.out.println("language Author " + langAu);

        //Get favourites_count
        long favourites_count = (long) TwAu.get("favourites_count");
        //System.out.println("favorites " + favourites_count);

        //Get name
        String name = (String) TwAu.get("name");
        //System.out.println("name " + name);

        //Get urlAu
        String urlAu = (String) TwAu.get("url");
        //System.out.println("url " + urlAu);

        //Get created_at
        String created_at = (String) TwAu.get("created_at");
        //System.out.println("created at " + created_at);

        //Get time_zone
        String time_zone = (String) TwAu.get("time_zone");
        //System.out.println("time zone " + time_zone);

        //Get default_profile
        boolean default_profile = (boolean) TwAu.get("default_profile");
        //System.out.println("default profile " + default_profile);

        //Get listed_count
        long listed_count = (long) TwAu.get("listed_count");
        //System.out.println("listed " + listed_count);

        //Get text
        String text = (String) Tweets.get("text");
        //System.out.println("text " + text);

        //Get twett object (site) within list
        JSONObject TwSi = (JSONObject) Tweets.get("site");

        //Get domain
        String domain = (String) TwSi.get("domain");
        //System.out.println("domain " + domain);

        //Get content_type
        String content_type = (String) Tweets.get("content_type");
        //System.out.println("content type " + content_type);

        //Get father_content_type
        String f_content_type = (String) Tweets.get("father_content_type");
        //System.out.println("father content type " + f_content_type);

        //Get string_date
        String string_date = (String) Tweets.get("string_date");
        //System.out.println("date stringa " + string_date);

        //Get type
        String type = (String) Tweets.get("type");
        //System.out.println("type " + type);

        //Get idTwett
        String idTw = (String) Tweets.get("id");
        //System.out.println("id twett " + idTw);

        return new Tweet(lang, sentiment, follow_request_sent, description, profile_text_color, location, id_str,
                username, langAu, name, urlAu, created_at, time_zone, text, domain, content_type, f_content_type,
                string_date, type, idTw, saved, verified, protecte, default_profile_image, geo_enabled, default_profile,
                virality, score, reach, weight, id, followers_count,statuses_count, friends_count, favourites_count,  listed_count);
    }

}
