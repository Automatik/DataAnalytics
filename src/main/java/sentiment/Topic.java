package sentiment;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Topic {

    public static int MAX_TOPICS = 3;

    public static void main(String[] args) {
        //leggere tutti i file di cvresults, sia di test che di train
        //per ciascun file estrarre confusion matrix e 3 topic * 3 sentiment
        String nTopic, nIter, nTWords, beta, lang;
        int isStem;
        String consoleOutput = "ConsoleOutput.txt";
        ArrayList<String> params = new ArrayList<>();
        ArrayList<String> jstFile = new ArrayList<>();
        String headerJstFile = "id,topic,iterazioni,word,beta,isStem,lang,Train,Test,NeutralTrain,PositiveTrain,NegativeTrain,NeutralTest,PositiveTest,NegativeTest";
        jstFile.add(headerJstFile);
        int id = 1;
        try{
            BufferedReader reader = Analysis.getReader(consoleOutput);
            String line;
            while((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                params.add(values[0]);
                String[] ps = values[0].split("_");
                if(ps[0].startsWith("n")) {
                    nTopic = ps[0].substring(2);
                    nIter = ps[1].substring(2);
                    nTWords = ps[2].substring(2);
                    beta = ps[3].substring(1);
                    isStem = (ps[4].equals("S")) ? 1 : 0;
                    if(isStem == 0)
                        lang = "All";
                    else lang = "It";
                } else {
                    lang = (ps[0].equals("It")) ? "It" : "All";
                    nTopic = ps[1].substring(2);
                    nIter = ps[2].substring(2);
                    nTWords = ps[3].substring(2);
                    beta = ps[4].substring(1);
                    isStem = (ps[5].equals("S")) ? 1 : 0;
                }
//                int nTopic = Integer.parseInt(ps[0].substring(2));
//                int nIter = Integer.parseInt(ps[1].substring(2));
//                int nTWords = Integer.parseInt(ps[2].substring(2));
//                float beta = Float.parseFloat(ps[3].substring(1));
                //String isStemming = ps[4];
                String meanTrain = values[1];
                String meanTest = values[2];

                String newJstRow = id + "," + nTopic + "," + nIter + "," + nTWords + "," + beta + "," + isStem + "," + lang + "," + meanTrain
                        + "," + meanTest;
                jstFile.add(newJstRow);
                id++;
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        //Parse Folders
        ArrayList<String> topicTrainFile = new ArrayList<>();
        ArrayList<String> topicTestFile = new ArrayList<>();
        String headerTopicFile = "id,Label,Topic,Word,Prob";
        topicTrainFile.add(headerTopicFile);
        topicTestFile.add(headerTopicFile);
        String sentimentTrainFile = "trainConfMat.txt";
        String sentimentTestFile = "testConfMat.txt";
        String wordsTrainFile = "final.twords";
        String wordsTestFile = "final_final.newtwords";
        for(String param : params) {
            String trainFolderDir = Analysis.myResultsDir + "train/" + param;
            String testFolderDir = Analysis.myResultsDir + "test/" + param;

            String[] ps = param.split("_");
            if(ps[0].startsWith("n")) {
                nTopic = ps[0].substring(2);
                nIter = ps[1].substring(2);
                nTWords = ps[2].substring(2);
                beta = ps[3].substring(1);
                isStem = (ps[4].equals("S")) ? 1 : 0;
                if(isStem == 0)
                    lang = "All";
                else lang = "It";
            } else {
                lang = (ps[0].equals("It")) ? "It" : "All";
                nTopic = ps[1].substring(2);
                nIter = ps[2].substring(2);
                nTWords = ps[3].substring(2);
                beta = ps[4].substring(1);
                isStem = (ps[5].equals("S")) ? 1 : 0;
            }
            String jstRow = nTopic + "," + nIter + "," + nTWords + "," + beta + "," + isStem + "," + lang;
            int index = getIndexOfParams(jstFile, jstRow);
            assert index >= 0;
            jstRow = jstFile.get(index);
            try {
                BufferedReader reader = Analysis.getReader(trainFolderDir + "/" + sentimentTrainFile);
                String line = reader.readLine(); //consume first line;
                line = reader.readLine();
                int[] sentiTrain= parseSentiment(line);
                reader.close();
                reader = Analysis.getReader(testFolderDir + "/" + sentimentTestFile);
                line = reader.readLine(); // consume first line;
                line = reader.readLine();
                int[] sentiTest = parseSentiment(line);
                reader.close();
                jstRow += "," + sentiTrain[0] + "," + sentiTrain[1] + "," + sentiTrain[2] + "," +
                        sentiTest[0] + "," + sentiTest[1] + "," + sentiTest[2];
                jstFile.set(index, jstRow);

                int idParam = Integer.parseInt(jstRow.split(",")[0]);
                String topicRow = idParam + ",";
                int currentLabel = 0, currentTopic = 0;
                reader = Analysis.getReader(trainFolderDir + "/" + wordsTrainFile);
                while((line = reader.readLine()) != null) {
                    if(line.startsWith("Label")) {
                        String[] vs = line.split("_");
                        currentLabel = Integer.parseInt(vs[0].split("Label")[1]);
                        currentTopic = Integer.parseInt(vs[1].split("Topic")[1]);
                    } else {
                        if(currentTopic < MAX_TOPICS) {
                            String[] vs = line.split(" "); //"          "
                            String word = vs[0];
                            String prob = vs[vs.length - 1];
                            String newRow = topicRow + getLabel(currentLabel) + "," + currentTopic + "," + word + "," + prob;
                            topicTrainFile.add(newRow);
                        }
                    }
                }
                reader.close();
                reader = Analysis.getReader(testFolderDir + "/" + wordsTestFile);
                currentLabel = currentTopic = 0;
                while((line = reader.readLine()) != null) {
                    if(line.startsWith("Label")){
                        currentLabel = Integer.parseInt(line.split(" ")[1].split("th")[0]);
                    } else if(line.startsWith("Topic")) {
                        currentTopic = Integer.parseInt(line.split(" ")[1].split("th")[0]);
                    } else {
                        if(currentTopic < MAX_TOPICS) {
                            String[] vs = line.split("\t")[1].split(" "); //"   "
                            String word = vs[0];
                            String prob = vs[vs.length - 1];
                            String newRow = topicRow + getLabel(currentLabel) + "," + currentTopic + "," + word + "," + prob;
                            topicTestFile.add(newRow);
                        }
                    }
                }
                reader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        try {
            Files.write(Paths.get("JST.csv"), jstFile, Charset.defaultCharset());
            Files.write(Paths.get("TopicTrain.csv"), topicTrainFile, Charset.defaultCharset());
            Files.write(Paths.get("TopicTest.csv"), topicTestFile, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static int[] parseSentiment(String matrix) {
        String removeExternalBrackets = matrix.substring(1,matrix.length()-1);
        String[] rows = removeExternalBrackets.split("]");
        String[] row1 = rows[0].split(", ");
        int neu1 = Integer.parseInt(row1[0].substring(1)); //Avoid the [
        int pos1 = Integer.parseInt(row1[1]);
        int neg1 = Integer.parseInt(row1[2]);
        String[] row2 = rows[1].split(", ");
        int neu2 = Integer.parseInt(row2[1].substring(1)); //Avoid the [
        int pos2 = Integer.parseInt(row2[2]);
        int neg2 = Integer.parseInt(row2[3]);
        String[] row3 = rows[2].split(", ");
        int neu3 = Integer.parseInt(row3[1].substring(1));
        int pos3 = Integer.parseInt(row3[2]);
        int neg3 = Integer.parseInt(row3[3]);

        int[] result = new int[3];
        result[0] = neu1 + neu2 + neu3;
        result[1] = pos1 + pos2 + pos3;
        result[2] = neg1 + neg2 + neg3;
        return result;
    }

    private static int getIndexOfParams(ArrayList<String> jstFile, String jstRow) {
        for(int i = 0; i < jstFile.size(); i++){
            if(jstFile.get(i).contains(jstRow))
                return i;
        }
        return -1;
    }

    private static String getLabel(int label) {
        String lab = "";
        switch (label) {
            case Analysis.NEUTRAL: lab = "neutral"; break;
            case Analysis.POSITIVE: lab = "positive"; break;
            case Analysis.NEGATIVE: lab = "negative"; break;
        }
        return lab;
    }

}
