package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFServicesMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class NFVEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFRequestsMap requestsMap;
    private NFNodesMap nodesMap;
    private NFServicesMap servicesMap;


    public NFVEvaluationMO(NetworkTopology topology, NFRequestsMap requests, NFNodesMap nodes, NFServicesMap services)
    {
        super(false);
        this.topology = topology;
        this.requestsMap = requests;
        this.nodesMap = nodes;
        this.servicesMap = services;
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
        NFNodesMap nodes = decode(solutionRepresentation);
        MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, servicesMap, requestsMap, nodes);
        OptimizationResultObject object = solver.optimize();

        // penalization added if there are services not available
        // and if all nodes of the topology pocess implemented services
        int penalizationVal = 0;
        if(!object.allServicesAvailable())
        {
            penalizationVal = 10000;
        }
        else
        {
            if(object.isAllNodesWServices())
            {
                penalizationVal = 1000;
            }
        }

        resultList[0] = new Double(object.getPhiValue());
        resultList[1] = new Double(object.getGammaValue()) + penalizationVal;

        return resultList;
    }

    public NFNodesMap decode(ILinearRepresentation<Integer> solution)
    {
        NFNodesMap nodes = new NFNodesMap();
        int numberOfNodes = nodes.getNodes().size();
        int[] result = new int[numberOfNodes];
        SolutionParser parser = new SolutionParser();

        for(int i = 0; i < numberOfNodes; i++)
        {
            result[i] = solution.getElementAt(i);
        }

        nodes = parser.solutionParser(result);

        return nodes;
    }
}
