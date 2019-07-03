/* Copyright 2016 Michael Sladoje and Mike Schälchli. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package ch.zhaw.facerecognitionlibrary.Recognition;

import android.content.Context;
import android.util.Log;
import ch.zhaw.facerecognitionlibrary.Helpers.FileHelper;
import ch.zhaw.facerecognitionlibrary.Helpers.OneToOneMap;
import ch.zhaw.facerecognitionlibrary.Helpers.PreferencesHelper;
import org.opencv.core.Mat;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static ch.zhaw.facerecognitionlibrary.Helpers.FileHelper.SVM_PATH;

/***************************************************************************************
 *    Title: AndroidLibSvm
 *    Author: yctung
 *    Date: 16.09.2015
 *    Code version: -
 *    Availability: https://github.com/
 *
 ***************************************************************************************/

public class SupportVectorMachine implements Recognition {
    PreferencesHelper preferencesHelper;
    private FileHelper fh;
    private File trainingFile;
    private File predictionFile;
    private File testFile;
    private List<String> trainingList;
    private List<String> testList;
    private OneToOneMap<String, Integer> labelMap;
    private OneToOneMap<String, Integer> labelMapTest;
    private int method;

    public SupportVectorMachine(Context context, int method) {
        preferencesHelper = new PreferencesHelper(context);
        fh = new FileHelper();
        trainingFile = fh.createSvmTrainingFile();
        predictionFile = fh.createSvmPredictionFile();
        testFile = fh.createSvmTestFile();
        trainingList = new ArrayList<>();
        testList = new ArrayList<>();
        labelMap = new OneToOneMap<String, Integer>();
        labelMapTest = new OneToOneMap<String, Integer>();
        this.method = method;
        File file = new File(SVM_PATH + "labelMap_train");
        if (file.exists()) {
            loadFromFile();
        } else {
            labelMap.put("Unknown", 1);
            saveToFile();
        }
        if (method == RECOGNITION) {
            loadFromFile();
        }
        //loadFromFile();
    }

    public SupportVectorMachine(File trainingFile, File predictionFile) {
        fh = new FileHelper();
        this.trainingFile = trainingFile;
        this.predictionFile = predictionFile;
        trainingList = new ArrayList<>();
    }

    // link jni library
    static {
        System.loadLibrary("jnilibsvm");
    }

    // connect the native functions
    private native void jniSvmTrain(String cmd);

    private native void jniSvmPredict(String cmd);

    @Override
    public boolean train() {

        fh.saveStringList(trainingList, trainingFile);

        // linear kernel -t 0
        String svmTrainOptions = preferencesHelper.getSvmTrainOptions();
        // Log.e("svmtrain",svmTrainOptions);
        String training = trainingFile.getAbsolutePath();
        String model = trainingFile.getAbsolutePath() + "_model";
        jniSvmTrain("-t 0 -b 1" + " " + training + " " + model);

        saveToFile();
        return true;
    }

    public boolean trainProbability(String svmTrainOptions) {
        fh.saveStringList(trainingList, trainingFile);

        String training = trainingFile.getAbsolutePath();
        String model = trainingFile.getAbsolutePath() + "_model";
        jniSvmTrain(svmTrainOptions + " -b 1" + " " + training + " " + model);

        return true;
    }

    @Override
    public String recognize(Mat img, String expectedLabel) {
        try {
            FileWriter fw = new FileWriter(predictionFile, false);
            String line = imageToSvmString(img, expectedLabel);
            //String probability=recognizeProbability(line);
            //Log.e("probability=",probability);
            testList.add(line);
            fw.append(line);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prediction = predictionFile.getAbsolutePath();
        String model = trainingFile.getAbsolutePath() + "_model";
        String output = predictionFile.getAbsolutePath() + "_output";

        if (predictionFile.exists() && trainingFile.exists() && new File(trainingFile.getAbsolutePath() + "_model").exists()) {
            try {
                jniSvmPredict("-b 1 " + prediction + " " + model + " " + output);
                BufferedReader buf = new BufferedReader(new FileReader(output));
                buf.readLine();
                String S = buf.readLine();
                String read_op[] = S.split(" ");
                Log.e("iLabel", S);
                int iLabel = Integer.parseInt(read_op[0]);
                buf.close();
            /*Double confidence=norm2();
            Log.e("confidence",""+confidence);
            if(confidence>80.0) {*/
                return labelMap.getKey(iLabel);
            /*}else{
                return "unknown person";
            }*/

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String recognizeProbability(String svmString) {
        try {
            FileWriter fw = new FileWriter(predictionFile, false);
            fw.append(svmString);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String prediction = predictionFile.getAbsolutePath();
        String model = trainingFile.getAbsolutePath() + "_model";
        String output = predictionFile.getAbsolutePath() + "_output";
        jniSvmPredict("-b 1 " + prediction + " " + model + " " + output);

        try {
            BufferedReader buf = new BufferedReader(new FileReader(output));
            // read header line
            String probability = buf.readLine() + "\n";
            Log.e("probability1", probability);
            // read content line
            probability = probability + buf.readLine();
            buf.close();
            return probability;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveToFile() {
        if (method == TRAINING) {
            fh.saveLabelMapToFile(SVM_PATH, labelMap, "train");
        } else {
            fh.saveLabelMapToFile(SVM_PATH, labelMapTest, "test");
        }
    }

    @Override
    public void saveTestData() {
        fh.saveStringList(testList, testFile);
    }

    @Override
    public void loadFromFile() {
        labelMap = fh.getLabelMapFromFile(SVM_PATH);
    }

    @Override
    public void addImage(Mat img, String label, boolean featuresAlreadyExtracted) {
        // Ignore featuresAlreadyExtracted because either SVM get the features from TensorFlow or Caffe, or it takes the image reshaping method (image itself)
        if (method == TRAINING) {
            trainingList.add(imageToSvmString(img, label));
        } else {
            testList.add(imageToSvmString(img, label));
        }
    }

    public void addImage(String svmString, String label) {
        trainingList.add(label + " " + svmString);
    }

    public Mat getFeatureVector(Mat img) {
        return img.reshape(1, 1);
    }

    private String imageToSvmString(Mat img, String label) {
        int iLabel = 0;
        if (method == TRAINING) {
            if (labelMap.containsKey(label)) {
                iLabel = labelMap.getValue(label);
            } else {
                iLabel = labelMap.size() + 1;
                labelMap.put(label, iLabel);
            }
        } else {
            if (labelMapTest.containsKey(label)) {
                iLabel = labelMapTest.getValue(label);
            } else {
                iLabel = labelMapTest.size() + 1;
                labelMapTest.put(label, iLabel);
            }
        }
        String result = String.valueOf(iLabel);
        return result + getSvmString(img);
    }

    public String getSvmString(Mat img) {
        img = getFeatureVector(img);
        String result = "";
       /* ArrayList<String> trainfeatures = sharedPreferenceHandler.getListString(SharedPrefKeys.TRAIN_FEATURE);
        if(method==TRAINING) {
            if (trainfeatures == null)
                trainfeatures = new ArrayList<>();
            for (int i = 0; i < img.cols(); i++) {
                if (trainfeatures.get(i) == "" || trainfeatures.get(i) == null) {
                    trainfeatures.add(i, String.valueOf(img.get(0, i)[0]));
                } else {
                    trainfeatures.add(i, String.valueOf(Double.parseDouble(trainfeatures.get(i)) + img.get(0, i)[0]));
                }

                result = result + " " + i + ":" + img.get(0, i)[0];
            }
            int n=sharedPreferenceHandler.getInt(SharedPrefKeys.N_TRAINING_EXAMPLES)+1;
            sharedPreferenceHandler.saveInt(SharedPrefKeys.N_TRAINING_EXAMPLES,n);
            sharedPreferenceHandler.saveStringList(SharedPrefKeys.TRAIN_FEATURE, trainfeatures);
        }else{*/
        //ArrayList<String> testfeatures=new ArrayList<>();
        for (int i = 0; i < img.cols(); i++) {
            //testfeatures.add(i, String.valueOf(img.get(0, i)[0]));
            result = result + " " + i + ":" + img.get(0, i)[0];
        }
        //sharedPreferenceHandler.saveStringList(SharedPrefKeys.TEST_FEATURE, testfeatures);
        //}
        return result;
    }

//    public double norm2() {
//        ArrayList<String> trainfeatures = sharedPreferenceHandler.getListString(SharedPrefKeys.TRAIN_FEATURE);
//        ArrayList<String> testfeatures = sharedPreferenceHandler.getListString(SharedPrefKeys.TEST_FEATURE);
//        int n_examples=sharedPreferenceHandler.getInt(SharedPrefKeys.N_TRAINING_EXAMPLES);
//        int n_features=testfeatures.size();
//        double[] x=new double[n_features];
//        for(int j=0;j<n_features;j++){
//            x[j]=(Double.parseDouble(trainfeatures.get(j))/n_examples)-Double.parseDouble(trainfeatures.get(j));
//        }
//        double norm = 0.0;
//
//        for (double n : x) {
//            norm += n * n;
//        }
//
//        norm = Math.sqrt(norm);
//
//        return norm;
//    }
}
