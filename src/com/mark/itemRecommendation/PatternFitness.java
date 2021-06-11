package com.mark.itemRecommendation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FullRunningAverage;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.common.RunningAverage;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;

import org.apache.mahout.cf.taste.impl.similarity.CityBlockSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import jenes.chromosome.IntegerChromosome;
import jenes.population.Fitness;
import jenes.population.Individual;

public class PatternFitness extends Fitness<IntegerChromosome> {
	FastByIDMap<PreferenceArray> trainingPrefs;

	Map<String, Double> FitC = new HashMap<String, Double>();
	FastByIDMap<PreferenceArray> trainingPrefs2;

	Map<Long, Map<Long,Double>> userMap = new HashMap<Long, Map<Long,Double>>();
//	Map<Long, Map<Long,Double>> conMap = new HashMap<Long, Map<Long,Double>>();
//	Map<Long, Map<Long,Double>> conMapTotally = new HashMap<Long, Map<Long,Double>>();

    static RunningAverage average;
    static float resultSum;
    static float AR;
    static float Aracc;
    static int ci=0;
    File file;
	File fileFull;
	File fileTrack;
	static DataModel dm ;
	static DataModel dmFull;
	static Map<Long, String> imap;
	static List<Long> UserID ;
	long[] user_cl;
    Map<IntegerChromosome, Double> chromRCResult;
	Map<Long, Map<Long,Double>> chromRCSResult = new HashMap<Long, Map<Long,Double>>();

    
	
	PatternFitness() throws TasteException {
    	super(false);
        try {
        	
        	//full rating file for testing the result
    		fileFull=new File("data/u.data");

        	//each file contains 10 random rating from 300 random users
    		file=new File("data/url_1.data");//("data/u2.data");
//    		file=new File("data/url_2.data");//("data/u2.data");
//    		file=new File("data/url_3.data");//("data/u2.data");
//    		file=new File("data/url_4.data");//("data/u2.data");
//    		file=new File("data/url_5.data");//("data/u2.data");
//    		file=new File("data/url_6.data");//("data/u2.data");
    		
    		

    		dm = new  FileDataModel(file);
    		dmFull=new  FileDataModel(fileFull);
    		
    		
    		//creating maps trying to reduce the running time.
    		
    		LongPrimitiveIterator lpi= dm.getUserIDs();
    		UserID = new ArrayList<Long>();
    		while(lpi.hasNext()) {
    			UserID.add(lpi.next());
    		}
    		
   

			int numUsers = dmFull.getNumUsers();
			trainingPrefs = new FastByIDMap<>( 1 + (int) (numUsers));
			LongPrimitiveIterator it = dmFull.getUserIDs();
			while (it.hasNext()) {
			    long userIDq = it.nextLong();
			    PreferenceArray prefs = dmFull.getPreferencesFromUser(userIDq);
			    trainingPrefs.put(userIDq, prefs);
			    }
			
			int numUsers1 = dm.getNumUsers();
			trainingPrefs2 = new FastByIDMap<>( 1 + (int) (numUsers1));
			LongPrimitiveIterator it1 = dm.getUserIDs();
			while (it1.hasNext()) {
			    long userIDq = it1.nextLong();
			
			    PreferenceArray prefs1 = dm.getPreferencesFromUser(userIDq);
			    trainingPrefs2.put(userIDq, prefs1);
			    }			
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TasteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        
	   chromRCResult = new HashMap<IntegerChromosome, Double>();
		for( int i =0;i<UserID.size();i++) {
			
			chromRCSResult.put(UserID.get(i), new HashMap<Long, Double>());
			for( int j =0;j<=SimilarityMetricRecom.RC_COUNT;j++) {
				chromRCSResult.get(UserID.get(i)).put((long) j,-1.0);
				}}
			


         }

 

@Override
 public void evaluate(Individual<IntegerChromosome> individual)  {

	 IntegerChromosome chrom = individual.getChromosome();
	 
	 //if this chromosome has been evaluated before it's fitness will not be computed 
	 if(FitC.containsKey(chrom.toString())) {
		 individual.setScore(FitC.get(chrom.toString()));
		 
	 }
	 else {
		 double a;
		 int s=0;   

		 int l=chrom.length();
		 for (int i=0; i<l;i++) {
			 s+=chrom.getValue(i);
		 }
		 //if the chromosome containing less or more than 10 items will be omitted in next generations because the assigned score is too high
		 if(SimilarityMetricRecom.RC_COUNT<s) {
			 a=1200;//s*10;
		 }
		 else if(s<SimilarityMetricRecom.RC_COUNT) {
			 a=1200;//(SimilarityMetricRecom.RC_COUNT-s)*10;
		 }
		 else {     
		 Aracc=0;
		
			 a=getRMSEofRecommender1(trainingPrefs,chrom);
		 }
  
		 individual.setScore(a);
	
		 FitC.put(chrom.toString(), a);

 } 
	 }

	
	
	public  double getRMSEofRecommender1(FastByIDMap<PreferenceArray> trainingPrefs,IntegerChromosome chrom) {
		 
		double result=0;
		double result2=0;
		 //if this chromosome has been evaluated before it's fitness will not be computed 
		if(chromRCResult.containsKey(chrom)) {

			return chromRCResult.get(chrom);
			}
		try {	
			
			int c=0;
		    resultSum=0;		   
		    RunningAverage rcall = new FullRunningAverage();   

		     
		    for (Long j :UserID) {
		    	int rcount=0;
		    	AR=0;
		    	
		    	c+=1;
//		    	if(c>283)
//		    		break;
		    	Long userID=j;
				PreferenceArray prefForU= trainingPrefs.get(userID);
			    Map<Long, Float> hmap = new HashMap<Long, Float>();
			    int numPrefs = prefForU.length();

			    
			    Map<Long, Float> hmap2 = new HashMap<Long, Float>();

			    for (int i = 0; i < numPrefs; i++) {
			    	float r= prefForU.get(i).getValue();
			    	if(r>SimilarityMetricRecom.threshold) {
			    		rcount++;
			    	}
				    hmap.put(prefForU.get(i).getItemID(),r);}
			    
			    for(int k=0;k<chrom.length();k++) {
			    	int genParameter=chrom.getValue(k);  
			    	if(genParameter==0)
			    		continue;
			    	UserSimilarity sim2 = null;
			    	ItemSimilarity sim1 = null;
				    average = new FullRunningAverage();   

			    	if(k==3) {
			    		sim2= new LogLikelihoodSimilarity(dm);
			    		result=this.recommend3(userID,genParameter, sim2,  hmap,hmap2,false,false);
			    		
			    	}
			    		
			    	else
			    	if(k==1) {
			    		
			    		sim2= new EuclideanDistanceSimilarity(dm);
			    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,false);
			    	}
			    		
			    	else if(k==0) {
			    		sim2= new SpearmanCorrelationSimilarity(dm);
			    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,false);
			    	}else if(k==4) { 
			    		sim2= new  PearsonCorrelationSimilarity(dm);
			    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,false);
			    	}else if(k==2) { 
			    		sim2= new  TanimotoCoefficientSimilarity(dm);

			    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,false);
			    }else if(k==5) { 
		    		sim2= new  UncenteredCosineSimilarity(dm);

		    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,true);
		    	}else if(k==6) { 
		    		sim2= new  CityBlockSimilarity(dm);

		    		result=recommend3(userID,genParameter, sim2,  hmap,hmap2,false,true);
		    	}
			    	result2+=result;
			    }
			    Aracc+=AR;
			    if(rcount!=0)
			    rcall.addDatum(AR/rcount);

			    }
//		    double f=rcall.getAverage();
//		    System.out.println("r="+rcall.getAverage());

			    } catch (TasteException e) {
					System.out.println("there was an error");
					e.printStackTrace();
				 }
//		chromRCResult.put(chrom,(double) result);//(-Aracc/(10*UserID.size())));
		double precision=-Aracc/(SimilarityMetricRecom.RC_COUNT*UserID.size());
		System.out.println(result2);
		
		chromRCResult.put(chrom,(double)(result2));
//		return result;// -Aracc/(10*UserID.size());
		return result2;

	}
	
   
    	public  double recommend3(Long userID,int genParameter,Refreshable sim, Map<Long, Float> hmap, Map<Long, Float> hmap2,boolean item_item,boolean sort) throws TasteException {
    		if(genParameter==0)
    			return computeFinalEvaluation();
    		GenericUserBasedRecommender recommender;

    		UserNeighborhood neighborhood =new NearestNUserNeighborhood(44,(UserSimilarity)sim,dm);
    		recommender = new GenericUserBasedRecommender(dm, neighborhood, (UserSimilarity) sim);
    		    	
					List<RecommendedItem> recommendations = recommender.recommend(userID, 44);
			    	
			    	double result=0;
			    	int lenRated=0;
			    	for (RecommendedItem recommendation : recommendations) {
//			    		float prediction = recommendation.getValue();
//			    		System.out.println(prediction);
			    		if(!hmap2.containsKey(recommendation.getItemID())) {
		    				hmap2.put(recommendation.getItemID(),recommendation.getValue());
			    		if(hmap.containsKey(recommendation.getItemID())) {
			    			if(hmap.get(recommendation.getItemID())>SimilarityMetricRecom.threshold) {
			    			AR+=1;
			    			
			    			}
			    			processOneEstimate(recommendation.getValue(),hmap.get(recommendation.getItemID()));

			    			lenRated++;
			    				}
			    				if(lenRated>=genParameter)
			    					break;
			    				}
			    			}
			    		if(lenRated==0) {ci++;
			    		}
		    result= computeFinalEvaluation();
		    return result;
			    }
        

        
    static void processOneEstimate(float estimatedPreference, float realPref) {
	    double diff = realPref - estimatedPreference;
	    average.addDatum(diff * diff);
	  }
	  
    static double computeFinalEvaluation() {
    	if(average.getCount()==0)
    		return 0;
    	//return average.getCount()*average.getAverage();
	    return Math.sqrt(average.getAverage());
	  }
};
    //m
   