package pt.uminho.algoritmi.netopt.nfv.optimization.Utils;

import java.util.List;

public class IGPWeightsOptimizationInputObject
{
    private double milpResult;
    private List<Request> requestList;

    public IGPWeightsOptimizationInputObject(double milpResult, List<Request> requestList) {
        this.milpResult = milpResult;
        this.requestList = requestList;
    }

    public double getMilpResult() {
        return milpResult;
    }

    public void setMilpResult(double milpResult) {
        this.milpResult = milpResult;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }
}
