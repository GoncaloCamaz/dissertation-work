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

public class NFVEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFVState state;
    private String filename;
    private int cplexTimeLimit;
    private double alpha;
    private ParamsNFV.EvaluationAlgorithm algorithm;


    public NFVEvaluationMO(NetworkTopology topology, NFVState state, String filename, int cplexTimeLimit, ParamsNFV.EvaluationAlgorithm alg, double alpha)
    {
        super(false);
        this.topology = topology;
        this.state = state;
        this.filename = filename;
        this.alpha = alpha;
        this.cplexTimeLimit = cplexTimeLimit;
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
    public Double[] evaluateMO(ILinearRepresentation<Integer> solutionRepresentation)
    {
        Double[] resultList = new Double[2];
        NFNodesMap nodes = decode(solutionRepresentation, topology.getDimension());
        this.state.setNodes(nodes);
        OptimizationResultObject object = new OptimizationResultObject(nodes.getNodes().size());
        double penalizationVal = 0;

        if(this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI))
        {
            NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, state,this.cplexTimeLimit, this.alpha);

            object = solver.optimize();
        }
        else
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, state,this.cplexTimeLimit);
            object = solver.optimize();
        }

        penalizationVal = checkIfSolutions(object);

        if (penalizationVal > 0)
        {
            resultList[0] = penalizationVal;
            resultList[1] = penalizationVal;
        }
        else {
            resultList[0] = object.getLoadValue();
            resultList[1] = Double.valueOf(object.getNumberOfServicesDeployed());
        }

        return resultList;
    }

    private double checkIfSolutions(OptimizationResultObject object)
    {
        double ret = 0;

        if(object.isAllservicesDeployed() == false || object.hasSolution()  == false)
        {
            ret = Double.MAX_VALUE;
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
}
