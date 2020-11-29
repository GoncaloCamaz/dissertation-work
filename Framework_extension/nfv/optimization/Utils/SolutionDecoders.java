package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;

import java.util.ArrayList;
import java.util.List;

public class SolutionDecoders
{
    public static Request decodeRequests(NFVRequestConfiguration req)
    {
        int origin = req.getRequestOrigin();
        NetNode source = new NetNode(origin);
        int destination = req.getRequestDestination();
        NetNode dest = new NetNode(destination);
        double bandwidth = req.getBandwidth();
        List<Integer> srPath = req.genSRPath();
        LabelPath path = new LabelPath(source,dest);

        ArrayList<Segment> segments = new ArrayList<>();
        int old = -1;
        int it = 0;
        int i;
        List<Flow> flows = new ArrayList<>();
        for(int index = 0; index < srPath.size(); index++)
        {
            i = srPath.get(index);
            if(i != old)
            {
                if(i != origin && i != destination || i == origin && old != -1 || i == destination && destination != origin && it < srPath.size())
                {
                    Segment s = new Segment(String.valueOf(i), Segment.SegmentType.NODE);
                    s.setDstNodeId(i);
                    if( old == -1)
                    {
                        s.setSrcNodeId(origin);
                    }
                    else
                    {
                        s.setSrcNodeId(old);
                    }
                    segments.add(s);
                    Flow f = new Flow(s.getSrcNodeId(), s.getDstNodeId(), Flow.FlowType.NFV, false, bandwidth);
                    flows.add(f);
                }
                old = i;
            }
            it++;
        }
        path.setLabels(segments);
        Request request = new Request(req.getRequestID(),flows,path);

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
