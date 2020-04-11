package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli;

import jecoli.algorithm.components.algorithm.IAlgorithmResult;
import jecoli.algorithm.components.algorithm.IAlgorithmStatistics;
import jecoli.algorithm.components.configuration.InvalidConfigurationException;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.operator.container.ReproductionOperatorContainer;
import jecoli.algorithm.components.operator.reproduction.linear.IntegerAddMutation;
import jecoli.algorithm.components.operator.reproduction.linear.LinearGenomeRandomMutation;
import jecoli.algorithm.components.operator.reproduction.linear.TwoPointCrossOver;
import jecoli.algorithm.components.operator.reproduction.linear.UniformCrossover;
import jecoli.algorithm.components.operator.selection.TournamentSelection2;
import jecoli.algorithm.components.randomnumbergenerator.DefaultRandomNumberGenerator;
import jecoli.algorithm.components.randomnumbergenerator.IRandomNumberGenerator;
import jecoli.algorithm.components.representation.integer.IntegerRepresentationFactory;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import jecoli.algorithm.components.representation.linear.ILinearRepresentationFactory;
import jecoli.algorithm.components.representation.linear.LinearRepresentation;
import jecoli.algorithm.components.solution.ISolution;
import jecoli.algorithm.components.solution.ISolutionContainer;
import jecoli.algorithm.components.solution.ISolutionFactory;
import jecoli.algorithm.components.solution.ISolutionSet;
import jecoli.algorithm.components.statistics.StatisticsConfiguration;
import jecoli.algorithm.components.terminationcriteria.ITerminationCriteria;
import jecoli.algorithm.components.terminationcriteria.IterationTerminationCriteria;
import jecoli.algorithm.multiobjective.archive.components.ArchiveManager;
import jecoli.algorithm.multiobjective.archive.components.InsertionStrategy;
import jecoli.algorithm.multiobjective.archive.components.ProcessingStrategy;
import jecoli.algorithm.multiobjective.archive.plotting.IPlotter;
import jecoli.algorithm.multiobjective.archive.trimming.ITrimmingFunction;
import jecoli.algorithm.multiobjective.archive.trimming.ZitzlerTruncation;
import jecoli.algorithm.multiobjective.nsgaII.NSGAIIConfiguration;
import jecoli.algorithm.singleobjective.evolutionary.RecombinationParameters;
import pt.uminho.algoritmi.netopt.SystemConf;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation.NFVEvaluationMO;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.AlgorithmInterface;
import pt.uminho.algoritmi.netopt.ospf.optimization.jecoli.algorithm.OSPFNSGAII;
import pt.uminho.algoritmi.netopt.ospf.simulation.*;
import pt.uminho.algoritmi.netopt.ospf.simulation.exception.DimensionErrorException;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolution;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.ASolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.AbstractSolutionSet;
import pt.uminho.algoritmi.netopt.ospf.simulation.solution.IntegerSolution;

import java.util.Iterator;
import java.util.List;

public class JecoliNFV
{
    private NetworkTopology topology;
    private NFServicesMap servicesMap;
    private NFRequestsMap requestsMap;
    private String servicesConfiguration; // filename

    private AlgorithmInterface<Integer> algorithm;
    private IAlgorithmResult<ILinearRepresentation<Integer>> results;
    private IAlgorithmStatistics<ILinearRepresentation<Integer>> statistics;

    private String info;
    protected IRandomNumberGenerator randomNumberGenerator;
    protected ArchiveManager<Integer, ILinearRepresentation<Integer>> archive;
    private int NUMObjectives = 2;
    private int MAXServicesSolutions;
    private int MINServicesSolutions;
    private int maxServicesPenalization;

    public JecoliNFV(NetworkTopology topology, NFRequestsMap requestsMap, NFServicesMap services, int lowerBound, int upperBound, String file, int maxServicesPenalization) {
        this.topology = topology.copy();
        this.requestsMap = requestsMap;
        this.servicesMap = services;
        this.algorithm = null;
        this.results = null;
        this.statistics = null;
        this.randomNumberGenerator = new DefaultRandomNumberGenerator();
        this.MINServicesSolutions = lowerBound;
        this.MAXServicesSolutions = upperBound;
        this.servicesConfiguration = file;
        this.maxServicesPenalization = maxServicesPenalization;

    }

    /**
     * Runs the optimization
     *
     * @throws Exception
     */
    public void run() throws Exception {
        results = algorithm.run();
        statistics = results.getAlgorithmStatistics();
        statistics.getSolutionContainer().getNumberOfSolutions();
    }

    /**
     * Cancels the optimization
     */
    public void cancel() {
        try {
            results = algorithm.cancel();
            statistics = results.getAlgorithmStatistics();
            statistics.getSolutionContainer().getNumberOfSolutions();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     * @throws Exception
     *
     *             builds the reproduction operators container for EA and MOEA
     */
    public ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> getContainer()
            throws Exception {

        double tpco=SystemConf.getPropertyDouble("ea.twoPointCrossover", 0.25);
        double uco=SystemConf.getPropertyDouble("ea.uniformCrossover", 0.25);
        double rm=SystemConf.getPropertyDouble("ea.randomMutation", 0.25);
        double im=SystemConf.getPropertyDouble("ea.incrementalMutation", 0.25);
        if(tpco+uco+rm+im!=1 || tpco<0 || uco<0 || rm<0 || im<0){
            tpco=0.25;uco=0.25;rm=0.25;im=0.25;
        }


        ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> operatorContainer = new ReproductionOperatorContainer<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
        operatorContainer.addOperator(tpco, new TwoPointCrossOver<Integer>());
        operatorContainer.addOperator(uco, new UniformCrossover<Integer>());
        operatorContainer.addOperator(rm, new LinearGenomeRandomMutation<Integer>(3));
        operatorContainer.addOperator(im, new IntegerAddMutation(3));
        return operatorContainer;
    }

    /**
     *
     * Builds the initial population to be used by SOEA/MOEA. The number of
     * objectives is defined in the <code>ISolutionFactory</code>
     *
     * @param params
     * @param solutionFactory
     * @return ISolutionSet
     * @throws DimensionErrorException
     *
     *
     */
    private ISolutionSet<ILinearRepresentation<Integer>> buildInitialPopulation(ParamsNFV params,
                                                                                ISolutionFactory<ILinearRepresentation<Integer>> solutionFactory) throws DimensionErrorException {

        // solution set
        ISolutionSet<ILinearRepresentation<Integer>> newSolutionSet;
        int numberOfObjective = solutionFactory.getNumberOfObjectives();

        int populationSize = params.getPopulationSize();
        int fromOldPopulationSize = 0;

        // number of solution obtained from a given previous solution
        if (params.hasInitialPopulation()) {

            double percentage = params.getInitialPopulationPercentage() / 100;
            if (percentage < 1.0) {

                fromOldPopulationSize = Math.min((int) (percentage * params.getPopulationSize()),
                        params.getInitialPopulation().getNumberOfSolutions());
            } else {
                fromOldPopulationSize = params.getInitialPopulation().getNumberOfSolutions();
            }
        }

        // build solution set
        int q;
        int p;
        if (fromOldPopulationSize > populationSize) {
            q = 0;
            p = populationSize;
        } else {
            q = populationSize - fromOldPopulationSize;
            p = fromOldPopulationSize;
        }

        newSolutionSet = solutionFactory.generateSolutionSet(q, new DefaultRandomNumberGenerator());

        if (p > 0) {
            Population clonedPop = params.getInitialPopulation().copy(numberOfObjective);
            List<IntegerSolution> l = clonedPop.getLowestValuedSolutions(p);
            Iterator<IntegerSolution> it = l.iterator();
            while (it.hasNext())
                newSolutionSet.add(SolutionParser.convert(it.next(), this.NUMObjectives));
        }

        return newSolutionSet;
    }

    /**
     * Pre-configuration for NSGAII
     *
     * @param params
     * @return
     * @throws Exception
     * @throws InvalidConfigurationException
     */
    public NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> preConfigureNSGAII(
            ParamsNFV params) throws Exception, InvalidConfigurationException {

        NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = new NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>>();
        configuration.setStatisticsConfiguration(new StatisticsConfiguration());
        configuration.setRandomNumberGenerator(randomNumberGenerator);
        IntegerRepresentationFactory solutionFactory = new IntegerRepresentationFactory(topology.getDimension(),
                MAXServicesSolutions, MINServicesSolutions, this.NUMObjectives);
        configuration.setSolutionFactory(solutionFactory);
        configuration.setNumberOfObjectives(this.NUMObjectives);

        configuration.setPopulationSize(params.getPopulationSize());

        configuration.getStatisticConfiguration().setNumberOfBestSolutionsToKeepPerRun(params.getPopulationSize());

        ISolutionSet<ILinearRepresentation<Integer>> newSolutions = buildInitialPopulation(params, solutionFactory);
        configuration.setInitialPopulation(newSolutions);
        configuration.setPopulationInitialization(false);

        ITerminationCriteria terminationCriteria = new IterationTerminationCriteria(params.getNumberGenerations());
        configuration.setTerminationCriteria(terminationCriteria);

        RecombinationParameters recombinationParameters = new RecombinationParameters(0, params.getPopulationSize(), 0,
                true);
        configuration.setRecombinationParameters(recombinationParameters);

        configuration.setSelectionOperator(
                new TournamentSelection2<ILinearRepresentation<Integer>>(1, 2, randomNumberGenerator));
        configuration.setReproductionOperatorContainer(this.getContainer());
        this.info = params.toString();

        return configuration;
    }

    /**
     * NSGAII
     *
     * @param params
     * @throws Exception
     * @throws InvalidConfigurationException
     */

    public void configureNSGAII(ParamsNFV params) throws Exception, InvalidConfigurationException {

        NSGAIIConfiguration<ILinearRepresentation<Integer>, ILinearRepresentationFactory<Integer>> configuration = this
                .preConfigureNSGAII(params);

        NFVEvaluationMO nfvEvaluation = new NFVEvaluationMO(topology,requestsMap,servicesMap, servicesConfiguration, this.maxServicesPenalization);

        configuration.setEvaluationFunction(nfvEvaluation);

        algorithm = new OSPFNSGAII(configuration);
    }

    /**
     *
     * @return the bestSolution
     */
    public int[] getBestSolutionServices() {
        ISolutionContainer<ILinearRepresentation<Integer>> c = results.getSolutionContainer();

        LinearRepresentation<Integer> rep = (LinearRepresentation<Integer>) c.getBestSolutionCellContainer(false)
                .getSolution().getRepresentation();
        int[] sol = new int[rep.getNumberOfElements()];

        for (int i = 0; i < rep.getNumberOfElements(); i++)
            sol[i] = rep.getElementAt(i);
        return sol;
    }

    public ASolution<Integer> getBestSolution() {
        ISolution<ILinearRepresentation<Integer>> s = results.getSolutionContainer().getBestSolutionCellContainer(false)
                .getSolution();
        return SolutionParser.convert(s);
    }

    public AbstractSolutionSet<Integer> getSolutionSet() {
        return algorithm.getSolutionSet();
    }

    public AbstractSolutionSet<Integer> getAchiveSolutionSet() {
        return algorithm.getAchiveSolutionSet();
    }

    public String getInfo() {
        return this.info;
    }

    public AlgorithmInterface<Integer> getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AlgorithmInterface<Integer> algorithm) {
        this.algorithm = algorithm;
    }

    public void configureDefaultArchive(ParamsNFV params) {
        archive = new ArchiveManager<Integer, ILinearRepresentation<Integer>>(this.getAlgorithm(),
                InsertionStrategy.ADD_ON_SOLUTIONSET_EVALUATION_FUNCTION_EVENT, InsertionStrategy.ADD_ALL,
                ProcessingStrategy.PROCESS_ARCHIVE_ON_ITERATION_INCREMENT);

        ITrimmingFunction<ILinearRepresentation<Integer>> trimmer = new ZitzlerTruncation<ILinearRepresentation<Integer>>(
                params.getArchiveSize(), getAlgorithm().getConfiguration().getEvaluationFunction());
        archive.addTrimmingFunction(trimmer);
    }

    public ASolutionSet<Integer> getArchive() {
        return SolutionParser.convert(archive.getArchive());
    }

    public IEvaluationFunction<ILinearRepresentation<Integer>> getEvaluationFunction() {
        return this.getAlgorithm().getConfiguration().getEvaluationFunction();
    }

    public void setPlotter(IPlotter<ILinearRepresentation<Integer>> plotter) {
        this.archive.setPlotter(plotter);
    }
}