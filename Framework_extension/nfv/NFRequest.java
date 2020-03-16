package pt.uminho.algoritmi.netopt.nfv;

import java.util.ArrayList;
import java.util.List;

public class NFRequest
{
    private int id;
    private int source;
    private int destination;
    private int bandwidth;
    private List<NFService> serviceList;

    public NFRequest()
    {
        this.id = 0;
        this.source = 0;
        this.destination = 0;
        this.bandwidth = 0;
        this.serviceList = new ArrayList<>();
    }

    public NFRequest(int id, int source, int destination, int bandwidth, List<NFService> serviceList) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.bandwidth = bandwidth;
        this.serviceList = serviceList;
    }

    public NFRequest(NFRequest request)
    {
        this.id = request.getId();
        this.source = request.getSource();
        this.destination = request.getDestination();
        this.bandwidth = request.getBandwidth();
        this.serviceList = request.getServiceList();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public List<NFService> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<NFService> serviceList) {
        this.serviceList = serviceList;
    }
}
