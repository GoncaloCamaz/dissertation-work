package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.ospf.simulation.Population;

import java.util.Iterator;
import java.util.List;

public class ParamsNFV
{
    public enum  AlgorithmSelectionOption{
        SOEA("Single Objective"),NSGAII("NSGAII");
        private final String name;
        AlgorithmSelectionOption(String name){
            this.name=name;
        }

        public String toString(){
            return name;
        }
    }

    public enum TerminationCriteria {
        ITERATION("Iteration"), FITNESS("Fitness");
        private final String name;

        TerminationCriteria(String name){
            this.name=name;
        }

        public String toString(){
            return name;
        }
    }

    // variables

    private double alfa;
    private double beta;
    int populationSize;
    int archiveSize;
    int numberGenerations;

    /**
     * Previous population
     */
    private Population population;

    /**
     * Percentage of previous population used
     */
    private double percentage;
    /**
     *  stop criteria value
     */
    private double criteriaValue;
    private TerminationCriteria criteria;

    public ParamsNFV()
    {
        this.alfa=0.5;
        this.beta=1;
        this.populationSize=100;
        this.archiveSize=100;
        this.numberGenerations = 200;
        this.percentage=0.0;
        this.setCriteriaValue(0.0);
        this.setCriteria(TerminationCriteria.ITERATION);
    }

    public void setCriteria(TerminationCriteria criteria) {
        this.criteria = criteria;
    }

    public double getAlfa() {
        return alfa;
    }

    public void setAlfa(double alfa) {
        this.alfa = alfa;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;
    }

    public int getNumberGenerations() {
        return numberGenerations;
    }

    public void setNumberGenerations(int numberGenerations) {
        this.numberGenerations = numberGenerations;
    }

    public boolean hasInitialPopulation(){
        return (this.percentage>0 && this.population!=null && this.population.getNumberOfSolutions()>0);
    }

        public void setInitialPopulation(Population population) {
        this.population=population;
    }

    public Population getInitialPopulation()
    {
        return this.population;
    }

    public void setInitialPopulationPercentage(double percentage) {
        this.percentage=percentage;
    }

    public double getInitialPopulationPercentage() {
        return this.percentage;
    }

    public TerminationCriteria getCriteria() {
        return criteria;
    }


    public double getCriteriaValue() {
        return criteriaValue;
    }

    public void setCriteriaValue(double criteriaValue) {
        this.criteriaValue = criteriaValue;
    }

    public double getBeta() {
        return this.beta;
    }

    public void setBeta(double beta)
    {
        this.beta=beta;
    }


    public static int[] convertIntegers(List<Integer> integers)
    {
        int[] ret = new int[integers.size()];
        Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = iterator.next().intValue();
        }
        return ret;
    }

    public ParamsNFV copy(){
        ParamsNFV p= new ParamsNFV();
        p.setAlfa(this.alfa);
        p.setArchiveSize(this.archiveSize);
        p.setBeta(this.beta);
        p.setCriteria(this.criteria);
        p.setInitialPopulation(this.population);
        p.setInitialPopulationPercentage(this.percentage);
        p.setNumberGenerations(numberGenerations);
        p.setPopulationSize(populationSize);
        return p;
    }
}
