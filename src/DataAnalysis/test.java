package DataAnalysis;

import java.io.IOException;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;

public class test {
	private static String loc = "../cs420/matches{$}.csv";
	
	/**
	 * NaiveBayes creation & validation
	 * @param validation
	 * @throws IOException
	 */
	private static void validateOn(int validation) throws IOException {
		CSVExtractor training = new CSVExtractor();
		// Read in training data.
		for (int i = 1; i <= 10; i++) {
			if (i == validation)
				continue;
			training.extract(loc.replace("{$}", Integer.toString(i)), false);
		}
		
		CSVExtractor validationSet = new CSVExtractor();
		// Read in validation data as unknown so we can test against it.
		validationSet.extract(loc.replace("{$}", Integer.toString(validation)), true);
		
		NaiveBayesClassification nbclass = new NaiveBayesClassification();
		
		// Teach the classifier using all of the training data.
		nbclass.learn(training);
		// Classify the "unknown" data and compare against validation.
		nbclass.classify(validationSet);
		
		System.out.println("---VALIDATION SET NUMBER " + validation + "--------------");
		System.out.printf("Successfully Classified: %d\n", nbclass.getSuccess());
		System.out.println("\tMISCLASSIFICATION");
		System.out.printf("T1 Victory: %d\tT2 Victory: %d\n", nbclass.getFalseT1(), nbclass.getFalseT2());
		System.out.printf("Success Rate of Classification: %.2f\n", nbclass.getSuccessRate());
		System.out.println("----------------------------------------\n");
	}
	
	private static void knnValidate() throws Exception {
		DataSource source = new DataSource(loc.replace("{$}", "_avg"));
		Instances ins = source.getDataSet();

		KnnClassification k = new KnnClassification(13, ins);
		k.validate();
		
		System.out.println("---VALIDATION OF K-NN------------------");
		System.out.printf("Successfully Classified: %d\n", k.getSuccess());
		System.out.printf("Success Rate of Classification: %.2f\n", k.getSuccessRate());
		System.out.printf("Avg. Success Rate of Validation: %.2f\n", k.getAvgSuccessRate());
		System.out.println("----------------------------------------\n");
	}
	
	public static void main(String[] args){ 
		try {
			for (int i = 1; i <= 10; i++) {
				validateOn(i);
			}
			
			knnValidate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
