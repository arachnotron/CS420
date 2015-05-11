package DataAnalysis;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;

public class KnnClassification {
	private IBk classifier;
	private Instances instances;
	
	public KnnClassification(int k, Instances i) {
		classifier = new IBk(k);
		instances = i;
		
		instances.setClassIndex(0);
		classifier.setCrossValidate(true);
	}
	
	public void classify() throws Exception {
		classifier.buildClassifier(instances);
	}
	
	public void validate(Instances I) throws Exception {
		for (int i = 0; i < I.numInstances(); i++) {
			classifier.classifyInstance(I.instance(i));
		}
	}
}
