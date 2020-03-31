package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFServicesMap;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class HybridEvaluationNFV extends IntegerEvaluationNFV
{
    public HybridEvaluationNFV(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap requestsMap, NFNodesMap nodesMap)
    {
        super(topology, nodesMap, servicesMap, requestsMap);
    }

    protected int[] decodeWeights(ILinearRepresentation<Integer> solution)
    {
        int edges = this.topology.getNumberEdges();
        int[] res = new int[edges];
        for(int i = 0; i < edges; i++)
        {
            res[i] = solution.getElementAt(i);
        }
        return res;
    }

    protected int[] decodeNodes(ILinearRepresentation<Integer> solution)
    {
        int nodes = nodesMap.getNodes().size();
        int[] ret = new int[nodes];
        return ret;
    }

}
