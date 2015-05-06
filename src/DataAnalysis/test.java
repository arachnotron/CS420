package DataAnalysis;

import java.io.IOException;

public class test {
	
	public static void main(String[] args){ 
		try {
			// Read in training data.
			CSVExtractor training = new CSVExtractor();
			training.extract("C://Users/C/Desktop/git_proj/cs420/matches1.csv", false);
			training.extract("C://Users/C/Desktop/git_proj/cs420/matches2.csv", false);
			training.extract("C://Users/C/Desktop/git_proj/cs420/matches3.csv", false);
			training.extract("C://Users/C/Desktop/git_proj/cs420/matches4.csv", false);
			training.extract("C://Users/C/Desktop/git_proj/cs420/matches5.csv", false);
			
			CSVExtractor validation = new CSVExtractor();
			// Read in validation data as unknown so we can test against it.
			validation.extract("C://Users/C/Desktop/git_proj/cs420/matches6.csv", true);
			validation.extract("C://Users/C/Desktop/git_proj/cs420/matches7.csv", true);

			NaiveBayesClassification nbclass = new NaiveBayesClassification();
			
			// Teach the classifier using all of the training data.
			nbclass.learn(training);
			nbclass.learn(training);
			// Classify the "unknown" data and compare against validation.
			nbclass.classify(validation);
			
			System.out.println("----------------------------------------");
			System.out.printf("Successfully Classified: %d\n", nbclass.getSuccess());
			System.out.println("\tMISCLASSIFICATION");
			System.out.printf("T1 Victory: %d\tT2 Victory: %d\n", nbclass.getFalseT1(), nbclass.getFalseT2());
			System.out.printf("Success Rate of Classification: %.2f\n", nbclass.getSuccessRate());
			System.out.println("----------------------------------------");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
