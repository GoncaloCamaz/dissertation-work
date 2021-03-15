package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;

import java.util.ArrayList;
import java.util.List;

public class SolutionDecoders
{
    public static Request decodeRequests(NFVRequestConfiguration req)
    {
        int origin = req.getRequestOrigin();
        int destination = req.getRequestDestination();
        double bandwidth = req.getBandwidth();
        List<Integer> srPath = req.genSRPath();
        int old = -1;
        List<Flow> flows = new ArrayList<>();
        for(int i = 0; i <srPath.size(); i++)
        {
            int node = srPath.get(i);
            if(old == -1)
            {
                Flow f = new Flow(origin,node, Flow.FlowType.NFV,false, bandwidth);
                flows.add(f);
                old = node;
            }
            else
            {
                Flow f = new Flow(old,node, Flow.FlowType.NFV,false, bandwidth);
                flows.add(f);
                old = node;
            }
        }
        Flow f = new Flow(old, destination, Flow.FlowType.NFV, false, bandwidth);
        flows.add(f);

        List<Flow> updatedFlows = new ArrayList<>();
        for(Flow fl : flows)
        {
            if(fl.getSource() != fl.getDestination())
            {
                updatedFlows.add(fl);
            }
        }
        Request request = new Request(req.getRequestID(),flows);

        return request;
    }

    public static double decodeNodesProcessCapacity(NetworkTopology topology, NFVState state)
    {
        double nodesCapacity = 0;
        boolean breakcycle = false;
        for(int i = 0; i < topology.getDimension() && !breakcycle; i++)
        {
            if(state.getNodes().getNodes().get(i).getProcessCapacity() > 0)
            {
                nodesCapacity = state.getNodes().getNodes().get(i).getProcessCapacity();
                breakcycle = true;
            }
        }

        return nodesCapacity;
    }
}
