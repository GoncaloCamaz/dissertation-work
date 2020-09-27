package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiSolver;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.EASolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class NFVEvaluationSO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFVState state;
    private String filename;
    private double maxServicesPenalization;
    private double alpha;
    private int cplexTimeLimit;
    private ParamsNFV.EvaluationAlgorithm algorithm;


    public NFVEvaluationSO(NetworkTopology topology, NFVState state, String filename, double maxServicesPenalization, double alpha,int cplexTimeLimit, ParamsNFV.EvaluationAlgorithm alg)
    {
        super(false);
        this.topology = topology;
        this.state = state;
        this.filename = filename;
        this.maxServicesPenalization = maxServicesPenalization;
        this.cplexTimeLimit = cplexTimeLimit;
        this.alpha = alpha;
        this.algorithm = alg;
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
        OptimizationResultObject object = new OptimizationResultObject(nodes.getNodes().size());
        double penalizationVal = 0;

        if(this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI_MPTCP))
        {
            NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, state,this.cplexTimeLimit, this.alpha, true);
            object = solver.optimize();
        }
        else if (this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.MLU_MPTCP))
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, state,this.cplexTimeLimit, true);
            object = solver.optimize();
        }
        else if(this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI))
        {
            NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, state,this.cplexTimeLimit, this.alpha,false);
            object = solver.optimize();
        }
        else
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, state,this.cplexTimeLimit,false);
            object = solver.optimize();
        }

        penalizationVal += checkIfSolutions(object);
        if(penalizationVal == 0) {
            penalizationVal += getPenalization(object, this.maxServicesPenalization);
        }

        resultList[0] = object.getPhiValue() + object.getMlu() + penalizationVal;
        resultList[1] = object.getGammaValue() + object.getMnu() + penalizationVal;

        return resultList;
    }

    private double checkIfSolutions(OptimizationResultObject object)
    {
        double ret = 0;
        if(!object.isAllservicesDeployed())
        {
            ret = Double.MAX_VALUE;
        }
        return ret;
    }

    private double getPenalization(OptimizationResultObject object, double maxServices)
    {
        double ret = 0;

        int servicesDeployed = object.getNumberOfServicesDeployed();

        if((maxServices < servicesDeployed))
            ret += (servicesDeployed-maxServices)*10000;


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

    public double getMaxServicesPenalization() {
        return maxServicesPenalization;
    }

    public void setMaxServicesPenalization(double maxServicesPenalization) {
        this.maxServicesPenalization = maxServicesPenalization;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public int getCplexTimeLimit() {
        return cplexTimeLimit;
    }

    public void setCplexTimeLimit(int cplexTimeLimit) {
        this.cplexTimeLimit = cplexTimeLimit;
    }

    public ParamsNFV.EvaluationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(ParamsNFV.EvaluationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setMaxServicesPenalization(int maxServicesPenalization) {
        this.maxServicesPenalization = maxServicesPenalization;
    }
}
