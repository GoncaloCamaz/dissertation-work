package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;

import java.util.ArrayList;
import java.util.List;

public class Request
{
    private String requestID;
    private Flow flow;
    private LabelPath path;
    private List<NetNode> segPath;

    public Request(String requestID, Flow flow, LabelPath path) {
        this.requestID = requestID;
        this.flow = flow;
        this.path = path;
        this.segPath = new ArrayList<>();
    }

    public Request(String requestID, Flow flow, List<NetNode> list) {
        this.requestID = requestID;
        this.flow = flow;
        this.segPath = list;
    }

    public List<NetNode> getSegPath() {
        return segPath;
    }

    public void setSegPath(List<NetNode> segPath) {
        this.segPath = segPath;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public Flow getFlow() {
        return flow;
    }

    public void setFlow(Flow flow) {
        this.flow = flow;
    }

    public LabelPath getPath() {
        return path;
    }

    public void setPath(LabelPath path) {
        this.path = path;
    }
}
