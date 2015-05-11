package DataAnalysis;

import java.util.List;

import de.daslaboratorium.machinelearning.classifier.*;

public class NaiveBayesClassification {
	
	private Classifier<Double, Integer> bayes;
	private int successes = 0;
	private int falseT1 = 0;
	private int falseT2 = 0;
	
	/**
	 * Builds Naive Bayes Classifier wrapper class
	 */
	public NaiveBayesClassification() {
		bayes = new BayesClassifier<Double, Integer>();
		bayes.setMemoryCapacity(2000);
	}
	
	/**
	 * Teaches the classifier using a set of records. In our case,
	 * the categories we want to classify records in are victory and
	 * defeat of Team 1.
	 * @param		CSV		Set of records.
	 */
	public void learn(CSVExtractor CSV) {
		for (List<Double> record : CSV.getVictories()) {
			bayes.learn(1, record);
		}
		
		for (List<Double> record : CSV.getDefeats()) {
			bayes.learn(0, record);
		}
	}
	
	/**
	 * Classifies an unknown record as part of a specific set. Then,
	 * uses the validation set to determine successes and failures.
	 * @param 		CSV		Set of records.
	 */
	public void classify(CSVExtractor CSV) {
		List<List<Double>> records = CSV.getUnknown();
		
		for (int i = 0 ; i < records.size() ; i++) {
			List<Double> record = records.get(i);
			Integer category = bayes.classify(record).getCategory();
			
			// If it's a victory but was classified as defeat, it fails
			if (CSV.getVictories().contains(record) && category.equals(0)) {
				falseT2++; //System.out.println(record);
			// If it's a defeat but was classified as a victory, it fails
			} else if (CSV.getDefeats().contains(record) && category.equals(1)) {
				falseT1++; //System.out.println(record);
			// Otherwise it's a success
			} else
				successes++;
		}
	}
	
	/**
	 * Returns successes.
	 * @return		Number of successes.
	 */
	public int getSuccess() {
		return successes;
	}
	
	/**
	 * Returns failed predictions that Team 1 would be victorious.
	 * @return		Number of failures.
	 */
	public int getFalseT1() {
		return falseT1;
	}
	
	/**
	 * Returns failed predictions that Team 2 would be victorious.
	 * @return		Number of failures.
	 */
	public int getFalseT2() {
		return falseT2;
	}
	
	/**
	 * Returns the success rate of the classifier.
	 * @return		String value representing success rate.
	 */
	public Double getSuccessRate() {
		return (double)(successes)/(double)(successes+falseT1+falseT2);
	}
	
}
