package sentiment;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Analysis {

    public static final int NEUTRAL = 0;
    public static final int POSITIVE = 1;
    public static final int NEGATIVE = 2;

    public static void main(String[] args) {
        //CreateLexicon();
        ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter_correct.json");
        removePunctuation(tweets);
        removeStopWords(tweets);
        countDateSentiment(tweets);
        //TrainTest tt = generateTrainAndTestFiles(tweets);
        //TrainTest tt = generateEqualTrainAndTestFiles(tweets);

        /*
        int[][] confMatTrain = getConfusionMatrix(tweets, true);
        int[][] confMatTest = getConfusionMatrix(tweets, false);
        if(confMatTrain == null || confMatTest == null) {
            System.out.println("Confusion Matrix is null");
            return;
        }
        System.out.println("Confusion Matrix for Training Set: \n"+ Arrays.deepToString(confMatTrain) +"\nAccuracy: "+getConfusionMatrixAccuracy(confMatTrain));
        System.out.println("Confusion Matrix for Testing Set: \n"+ Arrays.deepToString(confMatTest) +"\nAccuracy: "+getConfusionMatrixAccuracy(confMatTest));
        */
    }

    public static void CreateLexicon() {
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
        char[] p = new char[34];
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
        p[33] = '\n';

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
//                if(value == '\n')
//                    value = ' ';
//                if(!punctFound)
//                    builder.append(value);
                if(punctFound)
                    builder.append(' ');
                else
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

    public static TrainTest generateTrainAndTestFiles(ArrayList<Tweet> tweets) {
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

        TrainTest tt = new TrainTest();
        tt.trainIndexes = trainIndexes;
        tt.testIndexes = testIndexes;
        return tt;
    }

    public static TrainTest generateEqualTrainAndTestFiles(ArrayList<Tweet> tweets) {
        int totTweets = tweets.size();
        float trainPerc = 0.8f;
        //int trainSize = Math.round(trainPerc * totTweets);
        //int testSize = totTweets - trainSize;

        ArrayList<Integer> neutral = new ArrayList<>(), positive = new ArrayList<>(), negative = new ArrayList<>();
        for(int i = 0; i < totTweets; i++) {
            Tweet t = tweets.get(i);
            if(getTrueClass(t.getSentiment()) == NEUTRAL)
                neutral.add(i);
            else if(getTrueClass(t.getSentiment()) == POSITIVE)
                positive.add(i);
            else
                negative.add(i);
        }

        int neutralTrainSize = Math.round(trainPerc * neutral.size());
        int positiveTrainSize = Math.round(trainPerc * positive.size());
        int negativeTrainSize = Math.round(trainPerc * negative.size());
        int neutralTestSize = neutral.size() - neutralTrainSize;
        int positiveTestSize = positive.size() - positiveTrainSize;
        int negativeTestSize = negative.size() - negativeTrainSize;

        TreeSet<Integer> trainIndexes = new TreeSet<>();
        TreeSet<Integer> testIndexes;
        TreeSet<Integer> tempIndexes;

        while(trainIndexes.size() != neutralTrainSize)
            trainIndexes.add(neutral.get(ThreadLocalRandom.current().nextInt(0, neutralTrainSize)));
        while(trainIndexes.size() != neutralTrainSize+positiveTrainSize)
            trainIndexes.add(positive.get(ThreadLocalRandom.current().nextInt(0, positiveTrainSize)));
        while(trainIndexes.size() != neutralTrainSize+positiveTrainSize+negativeTrainSize)
            trainIndexes.add(negative.get(ThreadLocalRandom.current().nextInt(0, negativeTrainSize)));
        tempIndexes = new TreeSet<>(neutral);
        tempIndexes.addAll(positive);
        tempIndexes.addAll(negative);
        tempIndexes.removeAll(trainIndexes);
        testIndexes = new TreeSet<>(tempIndexes);
        assert testIndexes.size() == (neutralTestSize + positiveTestSize + negativeTestSize);

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

        TrainTest tt = new TrainTest();
        tt.trainIndexes = trainIndexes;
        tt.testIndexes = testIndexes;
        return tt;
    }

    public static int[][] getConfusionMatrix(ArrayList<Tweet> tweets, boolean useTrainIndexes) {
        /*
              TRUE        PREDICTED
             CLASS NEU POS NEG
               NEU
               POS
               NEG
         */
        int[][] confMat = new int[3][3];
        String filename;
        if(useTrainIndexes)
            filename = "results/final.pi";
        else
            filename = "resultstest/final_final.newpi";
        try {
            BufferedReader reader = getReader(filename);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                String document = values[1].substring(1);
                int documentID = Integer.parseInt(document);
//                if(useTrainIndexes)
//                    assert tt.trainIndexes.contains(documentID);
//                else
//                    assert tt.testIndexes.contains(documentID);

                float neutral = Float.parseFloat(values[2]);
                float positive = Float.parseFloat(values[3]);
                float negative = Float.parseFloat(values[4]);
                int trueClass = getTrueClass(tweets.get(documentID).getSentiment());
                assert trueClass > -1;
                int predictedClass;
                if (neutral > Math.max(positive, negative))
                    predictedClass = NEUTRAL;
                else if (positive > Math.max(negative, neutral))
                    predictedClass = POSITIVE;
                else predictedClass = NEGATIVE;
                //update the confusion matrix
                confMat[trueClass][predictedClass] += 1;
            }
            reader.close();
            return confMat;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedReader getReader(String filename) throws IOException {
        final Charset ENCODING = StandardCharsets.UTF_8;
        InputStream is = new FileInputStream(filename);
        return new BufferedReader(new InputStreamReader(is, ENCODING));
    }

    private static int getTrueClass(String sentiment) {
        int trueClass;
        switch (sentiment){
            case "neutral": trueClass = NEUTRAL; break;
            case "positive": trueClass = POSITIVE; break;
            case "negative": trueClass = NEGATIVE; break;
            default: trueClass = -1;
        }
        return trueClass;
    }

    private static float getConfusionMatrixAccuracy(int[][] confMat) {
        float correct = confMat[NEUTRAL][NEUTRAL]
                + confMat[POSITIVE][POSITIVE]
                + confMat[NEGATIVE][NEGATIVE];
        float total = 0;
        for(int r = NEUTRAL; r <= NEGATIVE; r++)
            for(int c = NEUTRAL; c <= NEGATIVE; c++)
                total += confMat[r][c];
        return correct / total;
    }

    private static void countUsernamesSentiment(ArrayList<Tweet> tweets) {
        HashMap<String, SentimentCount> users = new HashMap<>();
        for(Tweet t : tweets) {
            String username = t.getUsername();
            int sentiment = getTrueClass(t.getSentiment());
            if(users.containsKey(username)) {
                SentimentCount sc = users.get(username);
                sc.addSentiment(sentiment);
            } else {
                SentimentCount sc = new SentimentCount();
                sc.addSentiment(sentiment);
                users.put(username, sc);
            }
        }

        ArrayList<String> myCsv = new ArrayList<>(users.size());
        Set<Map.Entry<String, SentimentCount>> entries = users.entrySet();
        for(Map.Entry<String, SentimentCount> e : entries) {
            SentimentCount sc = e.getValue();
            String line = e.getKey() + "," + sc.neutralCount +
                    "," + sc.positiveCount + "," + sc.negativeCount;
            myCsv.add(line);
        }
        try {
            Files.write(Paths.get("data/userSentimentCount.csv"), myCsv, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void countDateSentiment(ArrayList<Tweet> tweets) {
        HashMap<String, SentimentCount> dates = new HashMap<>();
        for(Tweet t : tweets) {
            String fullDate = t.getString_date();
            String date = fullDate.split(" ")[0];
            int sentiment = getTrueClass(t.getSentiment());
            if(dates.containsKey(date)) {
                SentimentCount sc = dates.get(date);
                sc.addSentiment(sentiment);
            } else {
                SentimentCount sc = new SentimentCount();
                sc.addSentiment(sentiment);
                dates.put(date, sc);
            }
        }

        ArrayList<String> myCsv = new ArrayList<>(dates.size());
        SortedSet<String> keys = new TreeSet<>(dates.keySet());
        for(String key : keys) {
            SentimentCount sc = dates.get(key);
            String line = key + "," + sc.neutralCount +
                    "," + sc.positiveCount + "," + sc.negativeCount;
            myCsv.add(line);
        }
        try {
            Files.write(Paths.get("data/dateSentimentCount.csv"), myCsv, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class SentimentCount {
        private int neutralCount;
        private int positiveCount;
        private int negativeCount;

        public void addSentiment(int sentiment) {
            switch (sentiment) {
                case NEUTRAL: neutralCount++; break;
                case POSITIVE: positiveCount++; break;
                case NEGATIVE: negativeCount++; break;
            }
        }
    }

    private static class TrainTest {

        public TreeSet<Integer> trainIndexes;
        public TreeSet<Integer> testIndexes;

    }

}
