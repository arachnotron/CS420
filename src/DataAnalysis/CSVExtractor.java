package DataAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CSVExtractor {
	
	private ArrayList<String> features;
	private ArrayList<List<Double>> victories;
	private ArrayList<List<Double>> defeats;
	private ArrayList<List<Double>> unknown;

	public CSVExtractor() {
		features = new ArrayList<String>();
		victories = new ArrayList<List<Double>>();
		defeats = new ArrayList<List<Double>>();
		unknown = new ArrayList<List<Double>>();
	}
	
	/**
	 * Reads a file and extracts the data.
	 * @param		filename		The name of the CSV file to read.
	 * @param		unknown			Whether or not we want the win condition to be unknown. (Validation)
	 * @throws		IOException		Thrown if the file cannot be found, or a reader fails.
	 */
	public void extract(String filename, boolean unknown) throws IOException {
		File f = new File(filename);
		
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		
		// first line is feature/attribute names
		features.addAll(Arrays.asList(br.readLine().split(",")));
		features.remove(0); // Team1Victory isn't counted, really
		
		while (br.ready()) {
			String s = br.readLine();
			String[] sa = s.split(",");
			
			Double[] da = new Double[sa.length-1];
			
			for (int i = 0 ; i < da.length ; i++) {
				// Eliminate recent win attribute; replace with team
				if ((10 <= i && 14 >=i))
					da[i] = 100.0;
				else if ((35 <= i && 39 >= i))
					da[i] = 0.0;
				else
					da[i] = Double.parseDouble(sa[i+1]);
			}
						
			// For validation, we will also add it as an "unknown" item.
			if (unknown == true)
				this.unknown.add(Arrays.asList(da));
		
			// A win is a 1, a loss is a 0. Or anything else.
			if (sa[0].equals("1"))
				victories.add(Arrays.asList(da));
			else
				defeats.add(Arrays.asList(da));
		}
		
		br.close();
		fr.close();
	}
	
	public ArrayList<List<Double>> getUnknown() {
		return unknown;
	}
	
	public ArrayList<List<Double>> getVictories() {
		return victories;
	}
	
	public ArrayList<List<Double>> getDefeats() {
		return defeats;
	}
	
	public ArrayList<String> getFeatures() {
		return features;
	}
}
