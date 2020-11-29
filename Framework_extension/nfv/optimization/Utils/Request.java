package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;

import java.util.ArrayList;
import java.util.List;

public class Request
{
    private int requestID;
    private List<Flow> flows;
    private LabelPath path;
    private List<NetNode> segPath;

    public Request(int requestID, List<Flow> flows)
    {
        this.requestID = requestID;
        this.flows = flows;
        this.segPath = new ArrayList<>();
    }

    public Request(int requestID, List<Flow> flow, LabelPath path) {
        this.requestID = requestID;
        this.flows = flow;
        this.path = path;
        this.segPath = new ArrayList<>();
    }

    public Request(int requestID, List<Flow> flow, List<NetNode> list) {
        this.requestID = requestID;
        this.flows = flow;
        this.segPath = list;
    }

    public List<NetNode> getSegPath() {
        return segPath;
    }

    public void setSegPath(List<NetNode> segPath) {
        this.segPath = segPath;
    }

    public int getRequestID() {
        return requestID;
    }

    public void setRequestID(int requestID) {
        this.requestID = requestID;
    }

    public List<Flow> getFlow() {
        return flows;
    }

    public void setFlow(List<Flow> flow) {
        this.flows = flow;
    }

    public LabelPath getPath() {
        return path;
    }

    public void setPath(LabelPath path) {
        this.path = path;
    }
}
