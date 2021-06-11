package com.mark.itemRecommendation;

import java.util.ArrayList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;

import jenes.GeneticAlgorithm;
import jenes.chromosome.IntegerChromosome;
import jenes.population.Individual;
import jenes.population.Population;
import jenes.population.Population.Statistics.Group;
import jenes.stage.AbstractStage;
import jenes.stage.operator.common.OnePointCrossover;
import jenes.stage.operator.common.SimpleMutator;
import jenes.stage.operator.common.TournamentSelector;
import jenes.utils.Random;


public class SimilarityMetricRecom {
	private static final int MAX_INT = 10;

	public static int POPULATION_SIZE=100;
	public static int CHROMOSOME_LENGTH=5;
	public static int RC_COUNT=10;
	public static int GENERATION_LIMIT=100;
	public static int threshold=3;  
		
	
    public static void main(String[] args) throws TasteException {
    	double acc=0;
    	acc= getChrom();

    		
    }
    													
	@SuppressWarnings("null")
	public static  IntegerChromosome setRandom(IntegerChromosome chrom1) {
		/*
		 *making initial population 
 		 *creating a set of five random number and  normalizing their sum to 10  
		 *Creating a Chromosome with assigning a random number from the set of numbers to each gene in the Chromosome
		 */
   	 List<Integer> randomNum = new ArrayList<Integer>();
   	 int all=0;
     for (int i=0;i<CHROMOSOME_LENGTH;i++) {
    	 int r=Random.getInstance().nextInt(0, RC_COUNT + 1);
    	 randomNum.add(r);
    	 all+=r;
     }

   	 List<Integer> randomNumNormal = new ArrayList<Integer>();
   	 int all2=0;
     for (int i=0;i<CHROMOSOME_LENGTH-1;i++) {
    	 int s=randomNum.get(i)*RC_COUNT/all;
    	 randomNumNormal.add(s);
    	 all2+=s;
     }
	 randomNumNormal.add(RC_COUNT-all2);
     for (int i=0;i<CHROMOSOME_LENGTH;i++) {
	         int random = (int) ((Math.random() * randomNumNormal.size()));
	         Integer index = randomNumNormal.get(random);
	         randomNumNormal.remove(random);
	         chrom1.setValue(i,index);
     }
		 return chrom1;
	}
	
	
	public static double getChrom() throws TasteException{
		/*
		 * creating the initial population of GA and setting the ending condition and
		 *  mutation and cross over Operators and Fitness function(in patternfitness.java file)
		 */

     IntegerChromosome chrom = new IntegerChromosome(CHROMOSOME_LENGTH,0,MAX_INT);
     Individual<IntegerChromosome> sample = new Individual<IntegerChromosome>(chrom);

     Population<IntegerChromosome> pop = new Population<IntegerChromosome>(sample,0);
     
     Random.getInstance().setTimeSeed();
     
    
     for(int ml=0;ml<POPULATION_SIZE;ml++) {
    	 IntegerChromosome chrom1 = new IntegerChromosome(CHROMOSOME_LENGTH,0,MAX_INT);
    	 chrom1=setRandom(chrom1); 
    	 //if you want to test a population with your selected chomosome use this instead of setRandom chrom1.

//         chrom1.setValue(0,10);
//         chrom1.setValue(1,0);
//         chrom1.setValue(2,0);
//         chrom1.setValue(3,0);
//         chrom1.setValue(4,0);
    
         pop.add(new Individual<IntegerChromosome>(chrom1));
     	}


     for (int mk=0;mk<pop.size();mk++) {
    	 System.out.println(pop.getIndividual(mk));
     }
     
     PatternFitness fit = new PatternFitness();
     
     GeneticAlgorithm<IntegerChromosome> ga = new GeneticAlgorithm<IntegerChromosome>(fit, pop, GENERATION_LIMIT) {
    	 
    	 @SuppressWarnings("deprecation")
    	 @Override
         protected boolean end() {
    		 jenes.population.Population.Statistics stat = this.getCurrentPopulation().getStatistics();
    		 Group legals = stat.getGroup(Population.LEGALS);
             System.out.println("legals:"+legals.getStDev()[0]+" -"+Population.BEST);

    	     return (stat.getGroup(Population.LEGALS).getMin()[0] <= -1);//|(legals.getStDev()[0]<0.001));//if you want to stop the GA before generation limit, use this part.
    	    }
     };
     
     ga.setRandomization(0);
     
     AbstractStage<IntegerChromosome> selection = new TournamentSelector<IntegerChromosome>(3);
     AbstractStage<IntegerChromosome> crossover = new  OnePointCrossover<IntegerChromosome>(0.8);
     AbstractStage<IntegerChromosome> mutation = new   SimpleMutator<IntegerChromosome>(0.02);
     ga.addStage(selection);
     ga.addStage(crossover);
     ga.addStage(mutation);
     
    ga.setElitism(1);

     ga.evolve();
     
     Population.Statistics<IntegerChromosome> stats = ga.getCurrentPopulation().getStatistics();
     GeneticAlgorithm.Statistics algostats = ga.getStatistics();

     
     Group<IntegerChromosome> legals = stats.getGroup(Population.LEGALS);
     Individual<IntegerChromosome> solution = legals.get(0);

     System.out.println("a:"+algostats.getGenerations()+" -"+PatternFitness.ci);

     System.out.println("Solution: ");
     System.out.println( solution );
     System.out.format("found in %d ms.\n", algostats.getExecutionTime() );
     
      return solution.getScore();

	}
}
