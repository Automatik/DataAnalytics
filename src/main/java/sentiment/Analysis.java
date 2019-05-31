package sentiment;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Analysis {

    public static void main(String[] args) {
        //lexicon();
        ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter.json");
        removePunctuation(tweets);
        removeStopWords(tweets);
        generateTrainAndTestFiles(tweets);
    }

    public static void lexicon() {
        String filename = "data/sentix";
        ArrayList<String> myLexicon = new ArrayList<>();
        try {
            BufferedReader reader = getReader(filename);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                String lemma = values[0];
                float posScore = Float.parseFloat(values[3]);
                float negScore = Float.parseFloat(values[4]);
                float neuScore = 1 - (posScore + negScore);
                String s = lemma + "\t" + neuScore + "\t" + posScore + "\t" + negScore;
                myLexicon.add(s);
            }
            reader.close();
            Files.write(Paths.get("data/mylexicon"), myLexicon, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void removePunctuation(ArrayList<Tweet> tweets) {
        char[] p = new char[33];
        p[0] = '!';
        p[1] = '"';
        p[2] = '#';
        p[3] = '$';
        p[4] = '%';
        p[5] = '&';
        p[6] = '\'';
        p[7] = '(';
        p[8] = ')';
        p[9] = '*';
        p[10] = '+';
        p[11] = ',';
        p[12] = '-';
        p[13] = '.';
        p[14] = '/';
        p[15] = ':';
        p[16] = ';';
        p[17] = '<';
        p[18] = '=';
        p[19] = '>';
        p[20] = '?';
        p[21] = '@';
        p[22] = '[';
        p[23] = '\\';
        p[24] = ']';
        p[25] = '^';
        p[26] = '_';
        p[27] = '`';
        p[28] = '{';
        p[29] = '|';
        p[30] = '}';
        p[31] = '~';
        p[32] = '…'; //...
        //p[33] = '\n';

        for(Tweet t : tweets) {
            String text = t.getText();
            StringBuilder builder = new StringBuilder();
            char[] arr = text.toCharArray();
            for (char value : arr) {
                boolean punctFound = false;
                int c = 0;
                while(!punctFound && c < p.length) {
                    if (p[c] == value)
                        punctFound = true;
                    c++;
                }
                if(value == '\n')
                    value = ' ';
                if(!punctFound)
                    builder.append(value);
            }
            t.setText(builder.toString());
        }
    }

    public static void removeStopWords(ArrayList<Tweet> tweets) {
        ArrayList<String> stopWords = new ArrayList<>();
        try {
            BufferedReader reader = getReader("data/stopwords-it.txt");
            String line;
            while ((line = reader.readLine()) != null) {
                stopWords.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Tweet t : tweets) {
            String text = t.getText();
            String[] words = text.split(" ");
            ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));
            wordsList.removeAll(stopWords);
            wordsList.toArray(words);
            StringBuilder builder = new StringBuilder();
            for (String word : words)
                builder.append(word).append(" ");
            builder.deleteCharAt(builder.length() - 1); //remove the last space
            t.setText(builder.toString());
        }
    }

    public static void generateTrainAndTestFiles(ArrayList<Tweet> tweets) {
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
            int documentID = it.next();
            Tweet t = tweets.get(documentID);
            trainTxt.add("d" + documentID + " " + t.getText() + "\n");
            //trainTxt.add("\n");
        }
        it = testIndexes.iterator();
        while(it.hasNext()) {
            int documentID = it.next();
            Tweet t = tweets.get(documentID);
            testTxt.add("d" + documentID + " " + t.getText() + "\n");
            //testTxt.add("\n");
        }

        try {
            Files.write(Paths.get("data/mytrain.txt"), trainTxt, Charset.defaultCharset());
            Files.write(Paths.get("data/mytest.txt"), testTxt, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static BufferedReader getReader(String filename) throws IOException {
        final Charset ENCODING = StandardCharsets.UTF_8;
        InputStream is = new FileInputStream(filename);
        return new BufferedReader(new InputStreamReader(is, ENCODING));
    }

}
