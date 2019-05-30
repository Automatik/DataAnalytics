package sentiment;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class Analysis {

    public static void main(String[] args) {
        ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter.json");

        //Create Train and Test set
        int totTweets = tweets.size();
        float trainPerc = 0.8f;
        int trainSize = Math.round(trainPerc * totTweets);
        int testSize = totTweets - trainSize;

        TreeSet<Integer> trainIndexes = new TreeSet<>();
        TreeSet<Integer> testIndexes = new TreeSet<>();

        while(trainIndexes.size() != trainSize)
            trainIndexes.add(ThreadLocalRandom.current().nextInt(0, totTweets));
        for(int i = 0; i < totTweets; i++)
            testIndexes.add(i);
        testIndexes.removeAll(trainIndexes);
        assert testIndexes.size() == testSize;

        //Generate Train and Test txt
        ArrayList<String> trainTxt = new ArrayList<>();
        ArrayList<String> testTxt = new ArrayList<>();
        Iterator<Integer> it = trainIndexes.iterator();
        while(it.hasNext()){
            Tweet t = tweets.get(it.next());
            trainTxt.add(t.getText());
        }
        it = testIndexes.iterator();
        while(it.hasNext()) {
            Tweet t = tweets.get(it.next());
            testTxt.add(t.getText());
        }

        try {
            Files.write(Paths.get("data/mytrain.txt"), trainTxt, Charset.defaultCharset());
            Files.write(Paths.get("data/mytest.txt"), testTxt, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
