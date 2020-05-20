package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;

import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;

import java.awt.*;
import java.util.List;

public class NFVWeightsEvaluation extends AbstractEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private List<Request> requests;


    public NFVWeightsEvaluation(NetworkTopology topology, List<Request> requests)
    {
        super(false);
        this.topology = topology;
        this.requests = requests;
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
        return 1;
    }

    @Override
    public double evaluate(ILinearRepresentation<Integer> solutionRepresentation) throws Exception
    {
        double result = 0;
        int nodes = topology.getDimension();
        int edges = topology.getNumberEdges();
        int weights[] = decode(solutionRepresentation, edges);
        OSPFWeights weightsOSPF = new OSPFWeights(nodes);
        weightsOSPF.setWeights(weights,this.topology);

        SRSimulator simulator = new SRSimulator(topology,weightsOSPF);

        for(Request r : this.requests)
        {
            simulator.addFlow(r.getFlow(), r.getPath());
        }

        result = simulator.getCongestionValue();// new Double(object.getPhiValue());

        return result;
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
}
