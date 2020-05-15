package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver2;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.EASolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class NFVEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFVState state;
    private String filename;
    private int maxServicesPenalization;
    private int cplexTimeLimit;


    public NFVEvaluationMO(NetworkTopology topology, NFVState state, String filename, int maxServicesPenalization, int cplexTimeLimit)
    {
        super(false);
        this.topology = topology;
        this.state = state;
        this.filename = filename;
        this.maxServicesPenalization = maxServicesPenalization;
        this.cplexTimeLimit = cplexTimeLimit;
    }

    @Override
    public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {
    }

    @Override
    public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy() throws Exception {
        return null;
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
    }

    @Override
    public Double[] evaluateMO(ILinearRepresentation<Integer> solutionRepresentation) throws Exception
    {
        Double[] resultList = new Double[2];
        NFNodesMap nodes = decode(solutionRepresentation, topology.getDimension());
        this.state.setNodes(nodes);
        MCFPhiNodeUtilizationSolver2 solver = new MCFPhiNodeUtilizationSolver2(topology, state,this.cplexTimeLimit);
        OptimizationResultObject object = solver.optimize();

        double penalizationVal = 0;
        penalizationVal += getPenalization(object,this.maxServicesPenalization);

        resultList[0] = object.getPhiValue();
        resultList[1] = object.getGammaValue() + penalizationVal;

        return resultList;
    }

    private double getPenalization(OptimizationResultObject object, int maxServices)
    {
        double ret = 0;

        int servicesDeployed = object.getNumberOfServicesDeployed();
        if(!object.hasSolution())
        {
            ret = Double.MAX_VALUE;
        }

        if(maxServices < servicesDeployed)
        {
            ret += (servicesDeployed-maxServices)*10000;
        }

        return ret;
    }

    public NFNodesMap decode(ILinearRepresentation<Integer> solution, int numberOfNodes)
    {
        NFNodesMap nodes = new NFNodesMap();
        int[] result = new int[numberOfNodes];
        EASolutionParser parser = new EASolutionParser(this.filename);

        for(int i = 0; i < numberOfNodes; i++)
        {
            result[i] = solution.getElementAt(i);
        }

        nodes = parser.solutionParser(result);

        return nodes;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public void setTopology(NetworkTopology topology) {
        this.topology = topology;
    }

    public NFVState getState() {
        return state;
    }

    public void setState(NFVState state) {
        this.state = state;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getMaxServicesPenalization() {
        return maxServicesPenalization;
    }

    public void setMaxServicesPenalization(int maxServicesPenalization) {
        this.maxServicesPenalization = maxServicesPenalization;
    }
}
