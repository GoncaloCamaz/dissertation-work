package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;

public class Request
{
    private String requestID;
    private Flow flow;
    private LabelPath path;

    public Request(String requestID, Flow flow, LabelPath path) {
        this.requestID = requestID;
        this.flow = flow;
        this.path = path;
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
