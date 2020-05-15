package pt.uminho.algoritmi.netopt.nfv;

import java.util.ArrayList;
import java.util.List;

public class NFRequest
{
    private int id;
    private int source;
    private int destination;
    private int bandwidth;
    private List<Integer> serviceList;
    private List<NFRequestSegment> segments;
    

    public NFRequest()
    {
        this.id = 0;
        this.source = 0;
        this.destination = 0;
        this.bandwidth = 0;
        this.serviceList = new ArrayList<>();
        this.segments = new ArrayList<>();
        
    }

    public NFRequest(int id, int source, int destination, int bandwidth, List<Integer> serviceList) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.bandwidth = bandwidth;
        this.serviceList = serviceList;
        this.segments = new ArrayList<>();
        if(serviceList.size()>0){
        	this.segments.add(new NFRequestSegment(id,source,serviceList.get(0),true,false));
        	for(int i=0;i<serviceList.size()-1;i++){
        		this.segments.add(new NFRequestSegment(id,serviceList.get(i),serviceList.get(i+1)));
        	}
        	this.segments.add(new NFRequestSegment(id,serviceList.get(serviceList.size()-1),destination,false,true));
        }
        else
        	this.segments.add(new NFRequestSegment(id,source,destination,true,true));
        for(NFRequestSegment s:this.segments)
        	System.out.println(s);
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

    public List<Integer> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Integer> serviceList) {
        this.serviceList = serviceList;
    }
    
    public List<NFRequestSegment> getRequestSegments() {
        return this.segments;
    }

    public void setRequestSegments(List<NFRequestSegment> segments) {
        this.segments = segments;
    }
}
