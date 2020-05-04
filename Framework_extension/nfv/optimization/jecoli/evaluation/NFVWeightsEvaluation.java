package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class NFVWeightsEvaluation extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFVState state;
    private int cplexTimeLimit;


    public NFVWeightsEvaluation(NetworkTopology topology, NFVState state, int cplexTimeLimit)
    {
        super(false);
        this.topology = topology;
        this.state = state;
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
        int edges = topology.getNumberEdges();
        int weights[] = decode(solutionRepresentation, edges);
        topology.applyWeights(weights);

        MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, state,this.cplexTimeLimit);
        OptimizationResultObject object = solver.optimize();

        resultList[0] = new Double(object.getPhiValue());
        resultList[1] = new Double(object.getGammaValue());

        return resultList;
    }

    public int[] decode(ILinearRepresentation<Integer> solution, int edges)
    {
        int[] result = new int[edges];

        for(int i = 0; i < edges; i++)
        {
            result[i] = solution.getElementAt(i);
        }

        return result;

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

}
