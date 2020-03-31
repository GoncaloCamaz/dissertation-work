package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFServicesMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;


public class IntegerEvaluationNFV extends AbstractEvaluationFunction<ILinearRepresentation<Integer>>
{
    NetworkTopology topology;
    NFNodesMap nodesMap;
    NFRequestsMap requestsMap;
    NFServicesMap servicesMap;

    public IntegerEvaluationNFV(NetworkTopology topology, NFNodesMap nodes, NFServicesMap services, NFRequestsMap requestsMap)
    {
        super(false);
        this.topology = topology;
        this.nodesMap = nodes;
        this.servicesMap = services;
        this.requestsMap = requestsMap;
    }

    @Override
    public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {

    }

    @Override
    public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy() throws Exception {
        NFNodesMap nodesMapcopy = new NFNodesMap(this.nodesMap);
        NFServicesMap servicesMapcopy = new NFServicesMap(this.servicesMap);
        NFRequestsMap requestsMapcopy = new NFRequestsMap(this.requestsMap);
        IntegerEvaluationNFV copy = new IntegerEvaluationNFV(this.topology.copy(), nodesMapcopy, servicesMapcopy, requestsMapcopy);
        return copy;
    }

    @Override
    public int getNumberOfObjectives() {
        return 1; //nodes utilization
    }

    @Override
    public double evaluate(ILinearRepresentation<Integer> solutionRepresentation) throws Exception {
        NFNodesMap nodesMap = new NFNodesMap();
        SolutionParser parser = new SolutionParser();
       // nodesMap = parser.solutionParser(solutionRepresentation);
        return 0;
    }
}
