package pt.uminho.algoritmi.netopt.nfv.optimization;

import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.PopulationNFV;
import pt.uminho.algoritmi.netopt.ospf.comparator.DominanceComparator;
import pt.uminho.algoritmi.netopt.ospf.comparator.ParetoDominanceComparator;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.util.Iterator;

public class NondominatedPopulationNFV extends PopulationNFV implements Iterable<IntegerSolution>
{
    /**
     * The dominance comparator used by this non-dominated population.
     */
    private final DominanceComparator comparator;

    /**
     *  Minimum distance between solutions on the Pareto front
     */
    private double minimumDistance=1.0;
    private boolean filterMinimalDistance=false;

    /**
     * Constructs an empty non-dominated population using the Pareto dominance
     * relation.
     */
    public NondominatedPopulationNFV() {
        this(new ParetoDominanceComparator());
    }

    /**
     * Constructs an empty non-dominated population using the specified
     * dominance relation.
     *
     * @param comparator the dominance relation used by this non-dominated
     *        population
     */
    public NondominatedPopulationNFV(DominanceComparator comparator) {
        super();
        this.comparator = comparator;
    }


    public NondominatedPopulationNFV(ASolutionSet<Integer> population) {
        this();
        addAll(population);
    }

    /**
     * Constructs a non-dominated population using the specified dominance
     * comparator and initialized with the specified solutions.
     *
     * @param comparator the dominance relation used by this non-dominated
     *        population
     * @param iterable the solutions used to initialize this non-dominated
     *        population
     */
    public NondominatedPopulationNFV(DominanceComparator comparator, Iterator<IntegerSolution> it){
        this(comparator);
        addAll(it);
    }

    /**
     * If {@code newSolution} is dominates any solution or is non-dominated with
     * all solutions in this population, the dominated solutions are removed and
     * {@code newSolution} is added to this population. Otherwise,
     * {@code newSolution} is dominated and is not added to this population.
     */

    public boolean add(IntegerSolution newSolution) {
        Iterator<IntegerSolution> iterator = iterator();

        while (iterator.hasNext()) {
            IntegerSolution oldSolution = iterator.next();
            int flag = comparator.compare(newSolution, oldSolution);

            if (flag < 0) {
                iterator.remove();
            } else if (flag > 0) {
                return false;
            } else if (this.filterMinimalDistance && distance(newSolution, oldSolution) < this.minimumDistance) {
                return false;
            }
        }
        return super.add(newSolution);
    }

    /**
     * Adds the specified solution to the population, bypassing the
     * non-domination check. This method should only be used when a
     * non-domination check has been performed elsewhere, such as in a subclass.
     * <p>
     * <b>This method should only be used internally, and should never be made
     * public by any subclasses.</b>
     *
     * @param newSolution the solution to be added
     * @return true if the population was modified as a result of this operation
     */
    protected boolean forceAddWithoutCheck(IntegerSolution newSolution) {
        return super.add(newSolution);
    }

    /**
     * Returns the Euclidean distance between two solutions in objective space.
     *
     * @param s1 the first solution
     * @param s2 the second solution
     * @return the distance between the two solutions in objective space
     */
    protected double distance(IntegerSolution s1, IntegerSolution s2) {
        double distance = 0.0;

        for (int i = 0; i < s1.getNumberOfObjectives(); i++) {
            distance += Math.pow(s1.getFitnessValue(i) - s2.getFitnessValue(i), 2.0);
        }

        return Math.sqrt(distance);
    }

    /**
     * Returns the dominance comparator used by this non-dominated population.
     *
     * @return the dominance comparator used by this non-dominated population
     */
    public DominanceComparator getComparator() {
        return comparator;
    }

    public double getMinimumDistance() {
        return minimumDistance;
    }

    public void setMinimumDistance(double minimumDistance) {
        this.minimumDistance = minimumDistance;
    }

    public boolean isFilterMinimalDistance() {
        return filterMinimalDistance;
    }

    public void setFilterMinimalDistance(boolean filterMinimalDistance) {
        this.filterMinimalDistance = filterMinimalDistance;
    }
}
