package sentiment;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.italianStemmer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Analysis {

    public static final int NEUTRAL = 0;
    public static final int POSITIVE = 1;
    public static final int NEGATIVE = 2;

    public static String mainDir = "C:\\Users\\Emil\\Desktop\\JST-CPP\\JST-master";
    public static String resultsTrain = "results/";
    public static String resultsTest = "resultstest/";
    public static String myResultsDir = "cvresults/";

    //Files path for method afterJST()
    public static String finalOthers = "final.others";
    public static String finalPi = "final.pi";
    public static String finalTwords = "final.twords";
    public static String finalNewOthers = "final_final.newothers";
    public static String finalNewPi = "final_final.newpi";
    public static String finalNewTwords = "final_final.newtwords";

    public static int kFolds = 10;
    public static boolean applyStemming = true;

    public static void main(String[] args) throws Exception {

        ArrayList<String> output = new ArrayList<>();

        //Eseguire prima del JST in C++
        ArrayList<Tweet> tweets = beforeJST(applyStemming);
        //ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter_correct.json");
//     //   tweets = removeNonItalianTweets(tweets);

        //DEFAULT VALUES
        //setTrainingProperties("mytrain.txt", 1, 800, 20, 0.01, true);
        //setTestProperties("mytest.txt", 60, 20, 0.01, true);

        int[] nTopicsValues = new int[]{10};
        //int[] nTopicsValues = new int[]{1, 2, 3, 4, 5, 10, 30, 50};
        //int[] nItersValues = new int[]{200, 400, 800};
        int[] nItersValues = new int[]{400};
        //int[] tWordsValues = new int[]{10, 15, 20, 30};
        int[] tWordsValues = new int[]{15};
        //float[] betaValues = new float[]{(float) 0.01, (float) 0.02, (float) 0.05, (float) 0.1, (float) 0.5};
        float[] betaValues = new float[]{(float) 10};


        for(int a=0; a<nTopicsValues.length; a++){
            for(int b=0; b<nItersValues.length; b++){
                for(int c=0; c<tWordsValues.length; c++){
                    for(int d=0; d<betaValues.length; d++){
                        int nTopics = nTopicsValues[a];
                        int nIters = nItersValues[b];
                        int tWords = tWordsValues[c];
                        float beta = betaValues[d];
                        String folderName = "En_nT"+nTopics+"_nI"+nIters+"_tW"+tWords+"_b"+beta;
                        folderName += (applyStemming) ? "_S" : "_NS";
                        float meanAccuracyTrain = 0, meanAccuracyTest = 0;
                        for(int k = 0; k < kFolds; k++) {
                            String trainDataset = "mytrain"+k+".txt";
                            String testDataset = "mytest"+k+".txt";
                            setTrainingProperties(trainDataset, nTopics, nIters, tWords, beta, applyStemming);
                            setTestProperties(testDataset, 60, tWords, beta, applyStemming); //TODO niters
                            executeJST(true);
                            float[] acc;
                            try {
                                acc = afterJST(tweets, folderName);
                            } catch (Exception e) {
                                System.out.println("FolderName: "+folderName+" K:"+k);
                                e.printStackTrace();
                                throw new Exception();
                            }
                            assert acc != null;
                            meanAccuracyTrain += acc[0];
                            meanAccuracyTest += acc[1];
                        }
                        meanAccuracyTrain /= kFolds;
                        meanAccuracyTest /= kFolds;
                        String out = "Parameters: "+folderName+" Mean Accuracy Train: "+meanAccuracyTrain+" Test: "+meanAccuracyTest;
                        output.add(out);
                        System.out.println(out);
                    }
                }
            }
        }
        Files.write(Paths.get(myResultsDir+"ParametersOutput.txt"), output, Charset.defaultCharset());


//        int nTopics = 100;
//        int nIters = 400;
//        int tWords = 20;
//        float beta = (float) 0.1;
//        String folderName = "It_nT"+nTopics+"_nI"+nIters+"_tW"+tWords+"_b"+beta;
//        folderName += (applyStemming) ? "_S" : "_NS";
//        float meanAccuracyTrain = 0, meanAccuracyTest = 0;
//        for(int k=0; k<kFolds; k++){
//            String trainDataset = "mytrain"+k+".txt";
//            String testDataset = "mytest"+k+".txt";
//            setTrainingProperties(trainDataset, nTopics, nIters, tWords, beta, applyStemming);
//            setTestProperties(testDataset, 60, tWords, beta, applyStemming); //TODO 60
//            executeJST(true);
//            float[] acc;
//            try{
//                acc = afterJST(tweets, folderName);
//            } catch (Exception e) {
//                System.out.println("FolderName: "+folderName+" K:"+k);
//                e.printStackTrace();
//                throw new Exception();
//            }
//            assert acc != null;
//            meanAccuracyTrain += acc[0];
//            meanAccuracyTest += acc[1];
//        }
//        meanAccuracyTrain /= kFolds;
//        meanAccuracyTest /= kFolds;
//        System.out.println("Parameters: "+folderName+" Mean Accuracy Train: "+meanAccuracyTrain+" Test: "+meanAccuracyTest);
    }

    public static ArrayList<Tweet> beforeJST(boolean applyStemming) {
        CreateLexicon(applyStemming);
        ArrayList<Tweet> tweets = TweetUtils.parseTweetJson("data/gdf_twitter_correct.json");
        //removePunctuation(tweets);
        removeNonAlphabet(tweets);
        removeStopWords(tweets);
        if(applyStemming)
            applyStemming(tweets);

        //ArrayList<Tweet> withoutRT = removeRetweets(tweets);

        tweets = removeNonItalianTweets(tweets);

        //countDateSentiment(tweets);

        //TrainTest tt = generateTrainAndTestFiles(tweets);
        //TrainTest tt = generateEqualTrainAndTestFiles(0.8f, tweets);
        crossValidation(kFolds, tweets);
        return tweets;
    }

    public static float[] afterJST(ArrayList<Tweet> tweets, String folderName) throws Exception {
        int[][] confMatTrain = getConfusionMatrix(tweets, true);
        int[][] confMatTest = getConfusionMatrix(tweets, false);
        if(confMatTrain == null || confMatTest == null) {
            System.out.println("Confusion Matrix is null");
            return null;
        }
        //System.out.println("Confusion Matrix for Training Set: \n"+ Arrays.deepToString(confMatTrain) +"\nAccuracy: "+getConfusionMatrixAccuracy(confMatTrain));
        //System.out.println("Confusion Matrix for Testing Set: \n"+ Arrays.deepToString(confMatTest) +"\nAccuracy: "+getConfusionMatrixAccuracy(confMatTest));

        String trainDir = myResultsDir +"train/"+folderName;
        String testDir = myResultsDir +"test/"+folderName;
        String trainConfMat = trainDir+"/trainConfMat.txt";
        String testConfMat = testDir+"/testConfMat.txt";

        try {
            Files.createDirectories(Paths.get(trainDir));
        } catch(FileAlreadyExistsException e) {
            assert true;
        } catch(IOException e) {
            e.printStackTrace();
        }
        try {
            Files.createDirectories(Paths.get(testDir));
        } catch(FileAlreadyExistsException e) {
            assert true;
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            //Copy Train Results
            Files.copy(Paths.get(resultsTrain+finalOthers), Paths.get(trainDir+"/"+finalOthers), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(resultsTrain+finalPi), Paths.get(trainDir+"/"+finalPi), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(resultsTrain+finalTwords), Paths.get(trainDir+"/"+finalTwords), StandardCopyOption.REPLACE_EXISTING);
            ArrayList<String> confMatTxt = new ArrayList<>();
            confMatTxt.add("Confusion Matrix for Training Set:");
            confMatTxt.add(Arrays.deepToString(confMatTrain));
            confMatTxt.add("Accuracy: "+getConfusionMatrixAccuracy(confMatTrain));
            Files.write(Paths.get(trainConfMat), confMatTxt, Charset.defaultCharset());

            //Copy Test Results
            Files.copy(Paths.get(resultsTest+finalNewOthers), Paths.get(testDir+"/"+finalNewOthers), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(resultsTest+finalNewPi), Paths.get(testDir+"/"+finalNewPi), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(Paths.get(resultsTest+finalNewTwords), Paths.get(testDir+"/"+finalNewTwords), StandardCopyOption.REPLACE_EXISTING);
            confMatTxt.clear();
            confMatTxt.add("Confusion Matrix for Testing Set:");
            confMatTxt.add(Arrays.deepToString(confMatTest));
            confMatTxt.add("Accuracy: "+getConfusionMatrixAccuracy(confMatTest));
            Files.write(Paths.get(testConfMat), confMatTxt, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        float[] accuracy = new float[2];
        accuracy[0] = getConfusionMatrixAccuracy(confMatTrain);
        accuracy[1] = getConfusionMatrixAccuracy(confMatTest);
        return accuracy;
    }

    private static void executeJST(boolean printToConsole) {
        try {
            String trainJST = "cmd /c "+mainDir+"\\Debug\\jst -est -config "+mainDir+"\\training.properties";
            String testJST = "cmd /c "+mainDir+"\\Debug\\jst -inf -config "+mainDir+"\\test.properties";
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("cmd.exe","/c",trainJST);
            builder.redirectErrorStream(true);
            Process pr1 = builder.start();
            //if(printToConsole) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(pr1.getInputStream()));
                String line;
                while ((line = buf.readLine()) != null)
                    System.out.println(line);
            //}
            int waitFlag = pr1.waitFor();
            System.out.println("waitFlag: "+waitFlag);
            System.out.println("exitValue: "+pr1.exitValue());
            builder.command("cmd.exe","/c",testJST);
            builder.redirectErrorStream(true);
            Process pr2 = builder.start();
            //if(printToConsole) {
                buf = new BufferedReader(new InputStreamReader(pr2.getInputStream()));
                //String line;
                while ((line = buf.readLine()) != null)
                    System.out.println(line);
            //}
            waitFlag = pr2.waitFor();
            System.out.println("waitFlag: "+waitFlag);
            System.out.println("exitValue: "+pr1.exitValue());
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private static void setTrainingProperties(String datasetFile, int nTopics, int nIters, int tWords, double beta, boolean applyStemming) {
        ArrayList<String> trainProperties = new ArrayList<>(11);
        trainProperties.add("nsentiLabs=3");
        trainProperties.add("ntopics="+nTopics);
        trainProperties.add("niters="+nIters);
        trainProperties.add("savestep=100");
        trainProperties.add("updateParaStep=50");
        trainProperties.add("twords="+tWords);
        trainProperties.add("data_dir=C:/Users/Emil/Desktop/jst-master/data/cv");
        trainProperties.add("datasetFile="+datasetFile);
        trainProperties.add("result_dir=C:/Users/Emil/Desktop/jst-master/results/");
        if(applyStemming)
            trainProperties.add("sentiFile=C:/Users/Emil/Desktop/jst-master/data/mystemlexicon");
        else
            trainProperties.add("sentiFile=C:/Users/Emil/Desktop/jst-master/data/mylexicon");
        trainProperties.add("beta="+beta);
        try{
            Files.write(Paths.get(mainDir+"\\training.properties"), trainProperties, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void setTestProperties(String datasetFile, int nIters, int tWords, double beta, boolean applyStemming) {
        ArrayList<String> testProperties = new ArrayList<>(10);
        testProperties.add("niters="+nIters);
        testProperties.add("savestep=20");
        testProperties.add("twords="+tWords);
        testProperties.add("data_dir=C:/Users/Emil/Desktop/jst-master/data/cv");
        testProperties.add("datasetFile="+datasetFile);
        testProperties.add("result_dir=C:/Users/Emil/Desktop/jst-master/resultstest/");
        if(applyStemming)
            testProperties.add("sentiFile=C:/Users/Emil/Desktop/jst-master/data/mystemlexicon");
        else
            testProperties.add("sentiFile=C:/Users/Emil/Desktop/jst-master/data/mylexicon");
        testProperties.add("beta="+beta);
        testProperties.add("model_dir=C:/Users/Emil/Desktop/jst-master/results/");
        testProperties.add("model=final");
        try{
            Files.write(Paths.get(mainDir+"\\test.properties"), testProperties, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void CreateLexicon(boolean applyStemming) {
        String filename = "data/sentix";
        SnowballStemmer stemmer = (SnowballStemmer) new italianStemmer();
        ArrayList<String> myLexicon = new ArrayList<>();
        try {
            BufferedReader reader = getReader(filename);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                String lemma = values[0];
                if(applyStemming) {
                    stemmer.setCurrent(lemma);
                    stemmer.stem();
                    lemma = stemmer.getCurrent();
                }
                float posScore = Float.parseFloat(values[3]);
                float negScore = Float.parseFloat(values[4]);
                float neuScore = 1 - (posScore + negScore);
                String s = lemma + "\t" + neuScore + "\t" + posScore + "\t" + negScore;
                myLexicon.add(s);
            }
            reader.close();
            String outputName;
            if(applyStemming)
                outputName = "data/mystemlexicon";
            else
                outputName = "data/mylexicon";
            Files.write(Paths.get(outputName), myLexicon, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public static void removeNonAlphabet(ArrayList<Tweet> tweets) {
        String alphabet = "abcdefghijklmnopqrstuvwxyzáéíóúàèìòù";
        for(Tweet t : tweets) {
            String text = t.getText().toLowerCase();
            StringBuilder builder = new StringBuilder();
            char[] arr = text.toCharArray();
            for(char value : arr) {
                if (alphabet.indexOf(value) != -1)
                    builder.append(value);
                else
                    builder.append(' ');
            }
            t.setText(builder.toString());
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
        //Add "RT" to stop words
        stopWords.add("rt");

        for(Tweet t : tweets) {
            String text = t.getText();
            String[] words = text.split(" ");
            ArrayList<String> wordsList = new ArrayList<String>(Arrays.asList(words));
            wordsList.replaceAll(String::toLowerCase);
            wordsList.removeAll(stopWords);
            wordsList.toArray(words);
            StringBuilder builder = new StringBuilder();
            for (String word : wordsList) {
                //Se non è una singola lettera o non contiene la stringa null
                //aggiungila
                if(word.length() > 1 && !word.equals("null"))
                    builder.append(word).append(" ");
            }
            builder.deleteCharAt(builder.length() - 1); //remove the last space
            t.setText(builder.toString());
        }
    }

    private static void applyStemming(ArrayList<Tweet> tweets) {
        SnowballStemmer stemmer = (SnowballStemmer) new italianStemmer();
        for(Tweet t : tweets) {
            String text = t.getText();
            String[] words = text.split(" ");
            StringBuilder builder = new StringBuilder();
            for(String word : words) {
                if(word.length() > 1) {
                    stemmer.setCurrent(word);
                    stemmer.stem();
                    builder.append(stemmer.getCurrent()).append(" ");
                }
            }
            builder.deleteCharAt(builder.length() - 1); //remove last space
            t.setText(builder.toString());
        }
    }

    public static ArrayList<Tweet> removeNonItalianTweets(ArrayList<Tweet> tweets) {
        ArrayList<Tweet> italianTweets = new ArrayList<>();
        for(Tweet t : tweets)
            if(t.getLang().equals("it"))
                italianTweets.add(t);
        return italianTweets;
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
        ArrayList<String> trainTxt = generateTxt(tweets, trainIndexes);
        ArrayList<String> testTxt = generateTxt(tweets, testIndexes);

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

    public static TrainTest generateEqualTrainAndTestFiles(float trainPerc, ArrayList<Tweet> tweets) {
        int totTweets = tweets.size();
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
            trainIndexes.add(neutral.get(ThreadLocalRandom.current().nextInt(0, neutral.size())));
        while(trainIndexes.size() != neutralTrainSize+positiveTrainSize)
            trainIndexes.add(positive.get(ThreadLocalRandom.current().nextInt(0, positive.size())));
        while(trainIndexes.size() != neutralTrainSize+positiveTrainSize+negativeTrainSize)
            trainIndexes.add(negative.get(ThreadLocalRandom.current().nextInt(0, negative.size())));
        tempIndexes = new TreeSet<>(neutral);
        tempIndexes.addAll(positive);
        tempIndexes.addAll(negative);
        tempIndexes.removeAll(trainIndexes);
        testIndexes = new TreeSet<>(tempIndexes);
        assert testIndexes.size() == (neutralTestSize + positiveTestSize + negativeTestSize);

        //Generate Train and Test txt
        ArrayList<String> trainTxt = generateTxt(tweets, trainIndexes);
        ArrayList<String> testTxt = generateTxt(tweets, testIndexes);

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

    private static void crossValidation(int kFold, ArrayList<Tweet> tweets) {
        int totTweets = tweets.size();
        int foldSize, lastFoldSize;
        if(totTweets % kFold == 0)
            foldSize = totTweets / kFold;
        else
            foldSize = (int) Math.floor(totTweets / kFold);

        //Count neutral,positive and negative
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
        assert neutral.size() >= kFold;
        assert positive.size() >= kFold;
        assert negative.size() >= kFold;

        int neuPerFold = (int) Math.floor(neutral.size() / kFold);
        int posPerFold = (int) Math.floor(positive.size() / kFold);
        int negPerFold = (int) Math.floor(negative.size() / kFold);

        //Compose folds with random data
        ArrayList<TreeSet<Integer>> folds = new ArrayList<>();
        for(int k = 0; k < kFold; k++) {
            TreeSet<Integer> temp = new TreeSet<>();
            while(temp.size() != neuPerFold) {
                int randIdx = ThreadLocalRandom.current().nextInt(0, neutral.size());
                temp.add(neutral.get(randIdx));
                neutral.remove(randIdx);
            }
            TreeSet<Integer> fold = new TreeSet<>(temp);
            temp.clear();
            while(temp.size() != posPerFold) {
                int randIdx = ThreadLocalRandom.current().nextInt(0, positive.size());
                temp.add(positive.get(randIdx));
                positive.remove(randIdx);
            }
            fold.addAll(temp);
            temp.clear();
            while(temp.size() != negPerFold) {
                int randIdx = ThreadLocalRandom.current().nextInt(0, negative.size());
                temp.add(negative.get(randIdx));
                negative.remove(randIdx);
            }
            fold.addAll(temp);
            if(k == kFold - 1){
                //add remaining
                if(!neutral.isEmpty())
                    fold.addAll(neutral);
                if(!positive.isEmpty())
                    fold.addAll(positive);
                if(!negative.isEmpty())
                    fold.addAll(negative);
            }
            folds.add(k, fold);
        }
        for(int k = 0; k < kFold; k++) {
            ArrayList<String> testTxt = generateTxt(tweets, folds.get(k));
            TreeSet<Integer> temp = new TreeSet<>();
            for(int fold = 0; fold < kFold; fold++)
                if(fold != k)
                    temp.addAll(folds.get(fold));
            ArrayList<String> trainTxt = generateTxt(tweets, temp);
            try{
                Files.write(Paths.get("data/cv/mytrain"+k+".txt"), trainTxt, Charset.defaultCharset());
                Files.write(Paths.get("data/cv/mytest"+k+".txt"), testTxt, Charset.defaultCharset());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private static ArrayList<String> generateTxt(ArrayList<Tweet> tweets, TreeSet<Integer> indexes) {
        ArrayList<String> txt = new ArrayList<>();
        Iterator<Integer> it = indexes.iterator();
        while(it.hasNext()){
            int documentID = it.next();
            Tweet t = tweets.get(documentID);
            txt.add("d" + documentID + " " + t.getText() + "\n");
        }
        return txt;
    }

    public static int[][] getConfusionMatrix(ArrayList<Tweet> tweets, boolean useTrainIndexes) throws Exception {
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
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(" ");
                if(!line.isEmpty() && values.length == 5) {
                    try {
                        String document = values[1].substring(1);
                        int documentID = Integer.parseInt(document);
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
                        lineCount++;
                    } catch (Exception e) {
                        System.out.println("LineError: " + line + " LineCount: " + lineCount);
                        e.printStackTrace();
                        throw new Exception("ArrayIndexOutOfBounsException or NumberFormatException");
                    }
                }
            }
            reader.close();
            return confMat;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedReader getReader(String filename) throws IOException {
//        final Charset ENCODING = StandardCharsets.UTF_8;
//        InputStream is = new FileInputStream(filename);
//        return new BufferedReader(new InputStreamReader(is, ENCODING));
        File f = new File(filename);
        return new BufferedReader(new FileReader(f));
    }

    public static int getTrueClass(String sentiment) {
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

    private static ArrayList<Tweet> removeRetweets(ArrayList<Tweet> tweets) {
        ArrayList<Tweet> ts = new ArrayList<>();
        for(Tweet t : tweets) {
            String text = t.getText();
            String[] a = text.split(" ");
            if(!a[0].equals("RT")) //not a retweet
                ts.add(t);
        }
        return ts;
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

    public static void countDateSentiment(ArrayList<Tweet> tweets) {
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
