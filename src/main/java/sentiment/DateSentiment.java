package sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class DateSentiment {

    public static void main(String[] args) {
        String consoleOutput = "ConsoleOutput.txt";
        ArrayList<String> folders = new ArrayList<>();
        try {
            BufferedReader reader = Analysis.getReader(consoleOutput);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                folders.add(values[0]);
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter_correct.json");
        ArrayList<Tweet> italianTweets = Analysis.removeNonItalianTweets(tweets);
        String pi = "final_final.newpi";

        for(String folder : folders) {
            String lang;
            String[] ps = folder.split("_");
            if(ps[0].startsWith("n")) {
                int isStem = (ps[4].equals("S")) ? 1 : 0;
                if(isStem == 0)
                    lang = "All";
                else lang = "It";
            } else {
                lang = (ps[0].equals("It")) ? "It" : "All";
            }

            ArrayList<Tweet> predicted = new ArrayList<>();
            ArrayList<Tweet> trueclass = new ArrayList<>();
//            if(lang.equals("It")) {
//                //predicted = (ArrayList<Tweet>) italianTweets.clone();
//                predicted = new ArrayList<>(italianTweets.size());
//                for(Tweet t : italianTweets) {
//                    Tweet t1 = new Tweet();
//                    t1.setSentiment(new String(t.sentiment));
//                    t1.setString_date(new String(t.string_date));
//                    predicted.add(t1);
//                }
//            } else {
//                predicted = new ArrayList<>(tweets.size());
//                for(Tweet t : tweets) {
//                    Tweet t1 = new Tweet();
//                    t1.setSentiment(new String(t.sentiment));
//                    t1.setString_date(new String(t.string_date));
//                    predicted.add(t1);
//                }
//            }

            try {
                String filename = "cvresults/test/"+folder+"/"+pi;
                BufferedReader reader = Analysis.getReader(filename);
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(" ");
                    if (!line.isEmpty() && values.length == 5) {
                        try {
                            String document = values[1].substring(1);
                            int documentID = Integer.parseInt(document);
                            float neutral = Float.parseFloat(values[2]);
                            float positive = Float.parseFloat(values[3]);
                            float negative = Float.parseFloat(values[4]);
                            Tweet t;
                            if(lang.equals("It"))
                                t = italianTweets.get(documentID);
                            else
                                t = tweets.get(documentID);
                            Tweet t1 = new Tweet();
                            t1.setString_date(t.string_date);
                            if (neutral > Math.max(positive, negative))
                                t1.setSentiment("neutral");
                            else if (positive > Math.max(negative, neutral))
                                t1.setSentiment("positive");
                            else t1.setSentiment("negative");
                            predicted.add(t1);
                            trueclass.add(t);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            countDateSentiment(predicted, trueclass, "dateSentiment/"+folder+".csv");

//            if(lang.equals("It")) {
//                countDateSentiment(predicted, italianTweets, "dateSentiment/"+folder+".csv");
//            } else {
//                countDateSentiment(predicted, tweets, "dateSentiment/"+folder+".csv");
//            }

        }
    }

    public static void countDateSentiment(ArrayList<Tweet> predicted, ArrayList<Tweet> trueclass, String path) {
        HashMap<String, SentimentCount> dates = new HashMap<>();
        for(Tweet t : predicted) {
            String fullDate = t.getString_date();
            String date = fullDate.split(" ")[0];
            int sentiment = Analysis.getTrueClass(t.getSentiment());
            if(dates.containsKey(date)) {
                SentimentCount sc = dates.get(date);
                sc.addSentiment(sentiment);
            } else {
                SentimentCount sc = new SentimentCount();
                sc.addSentiment(sentiment);
                dates.put(date, sc);
            }
        }

        for(Tweet t : trueclass) {
            String fullDate = t.getString_date();
            String date = fullDate.split(" ")[0];
            int sentiment = Analysis.getTrueClass(t.getSentiment());
            if(dates.containsKey(date)) {
                SentimentCount sc = dates.get(date);
                sc.addTrueSentiment(sentiment);
            } else {
                SentimentCount sc = new SentimentCount();
                sc.addTrueSentiment(sentiment);
                dates.put(date, sc);
            }
        }

        ArrayList<String> myCsv = new ArrayList<>(dates.size());
        myCsv.add("Date,TrueSentNeutral,TrueSentPositive,TrueSentNegative,SentNeutral,SentPositive,SentNegative");
        SortedSet<String> keys = new TreeSet<>(dates.keySet());
        for(String key : keys) {
            SentimentCount sc = dates.get(key);
            String line = key + "," + sc.trueNeutral + "," + sc.truePositive + "," + sc.trueNegative + "," +
                    sc.neutralCount + "," + sc.positiveCount + "," + sc.negativeCount;
            myCsv.add(line);
        }
        try {
            Files.write(Paths.get(path), myCsv, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SentimentCount {
        private int neutralCount;
        private int positiveCount;
        private int negativeCount;

        private int trueNeutral, truePositive, trueNegative;

        public void addSentiment(int sentiment) {
            switch (sentiment) {
                case Analysis.NEUTRAL: neutralCount++; break;
                case Analysis.POSITIVE: positiveCount++; break;
                case Analysis.NEGATIVE: negativeCount++; break;
            }
        }

        public void addTrueSentiment(int sentiment) {
            switch (sentiment) {
                case Analysis.NEUTRAL: trueNeutral++; break;
                case Analysis.POSITIVE: truePositive++; break;
                case Analysis.NEGATIVE: trueNegative++; break;
            }
        }
    }

}
