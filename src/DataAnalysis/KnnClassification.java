package DataAnalysis;
import java.util.ArrayList;

import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.Instance;

public class KnnClassification {
	private IBk classifier;
	private Instances instances;
	
	private int success = 0;
	private int failure = 0;
	private double avgSuccessRate = 0;
	
	public KnnClassification(int k, Instances i) throws Exception {
		classifier = new IBk(k);
		instances = i;
		
		instances.setClassIndex(0);
		classifier.setCrossValidate(true);
	}
	
	public void validate() throws Exception {
		int totalSuccess = 0;
		int totalFailure = 0;
		double avgSuccessRate = 0;
		
		// 10-fold cross validation
		for (int i = 0; i < 10; i++) {
			int success = 0;
			int failure = 0;
			
			ArrayList<Instance> AI = new ArrayList<Instance>();
			
			// remove first subset
			for (int n = 0; n < instances.numInstances()/10; n ++) {
				AI.add(instances.instance(0));
				instances.delete(0);
			}
			
			classifier.buildClassifier(instances);
			
			// validate first subset
			for (Instance I : AI) {
				if (classifier.classifyInstance(I) == I.classValue())
					success++;
				else
					failure++;
			}
			
			// add subset to end
			for (Instance I : AI) {
				instances.add(I);
			}
			
			totalSuccess += success;
			totalFailure += failure;
			
			avgSuccessRate += (double)success/(double)(success+failure);
		}
		
		this.avgSuccessRate = avgSuccessRate/10;
		success = totalSuccess;
		failure = totalFailure;
	}
	
	public int getSuccess() {
		return success;
	}
	
	public int getFailure() {
		return failure;
	}
	
	public double getSuccessRate() {
		return (double)success / (double)(success+failure);
	}
	
	public double getAvgSuccessRate() {
		return avgSuccessRate;
	}
}
