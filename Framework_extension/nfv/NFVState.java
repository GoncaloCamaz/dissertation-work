package pt.uminho.algoritmi.netopt.nfv;

import org.json.simple.parser.ParseException;

import java.io.IOException;

public class NFVState
{
    private NFRequestsMap requests;
    private NFNodesMap nodes;
    private NFServicesMap services;
    private String nsfile;
    private String reqfile;

    public NFVState()
    {
        this.requests = new NFRequestsMap();
        this.nodes = new NFNodesMap();
        this.services = new NFServicesMap();
        this.nsfile = "";
        this.reqfile = "";
    }

    public NFVState(String filename, String requestsFile)
    {
        this.requests = new NFRequestsMap();
        this.nodes = new NFNodesMap();
        this.services = new NFServicesMap();
        this.nsfile = filename;
        this.reqfile = requestsFile;
        this.loadState(filename,requestsFile, false);
    }

    public NFVState(String filename, String requestsFile, boolean requestsSorted)
    {
        this.requests = new NFRequestsMap();
        this.nodes = new NFNodesMap();
        this.services = new NFServicesMap();
        this.nsfile = filename;
        this.reqfile = requestsFile;
        this.loadState(filename,requestsFile, requestsSorted);
    }

    public NFVState(String frameworkConfig)
    {
        this.requests = new NFRequestsMap();
        this.nodes = new NFNodesMap();
        this.services = new NFServicesMap();
        this.nsfile = frameworkConfig;
        this.loadState(frameworkConfig);
    }

    public NFVState(NFRequestsMap requests, NFNodesMap nodes, NFServicesMap services, String filename, String requestsfile) {
        this.requests = requests;
        this.nodes = nodes;
        this.services = services;
        this.nsfile = filename;
        this.reqfile = requestsfile;
    }

    public NFVState(NFVState state)
    {
        this.requests = state.getRequests();
        this.nodes = state.getNodes();
        this.services = state.getServices();
        this.nsfile = state.getNsfile();
        this.reqfile = state.getReqfile();
    }

    public NFRequestsMap getRequests() {
        return requests;
    }

    public void setRequests(NFRequestsMap requests) {
        this.requests = requests;
    }

    public NFNodesMap getNodes() {
        return nodes;
    }

    public void setNodes(NFNodesMap nodes) {
        this.nodes = nodes;
    }

    public NFServicesMap getServices() {
        return services;
    }

    public void setServices(NFServicesMap services) {
        this.services = services;
    }

    public String getNsfile() {
        return nsfile;
    }

    public void setNsfile(String nsfile) {
        this.nsfile = nsfile;
    }

    public String getReqfile() {
        return reqfile;
    }

    public void setReqfile(String reqfile) {
        this.reqfile = reqfile;
    }

    public void loadState(String ndsvfile, String requestsFile, boolean sorted)
    {
        try
        {
            this.nodes = NFVStateLoader.loadNodes(ndsvfile);
            this.services = NFVStateLoader.loadServices(ndsvfile);
            if(!sorted)
                this.requests = NFVStateLoader.loadRequests(requestsFile);
            else
                this.requests = NFVStateLoader.loadRequestsSorted(requestsFile);
        }
        catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    public void loadState(String ndsvfile)
    {
        try
        {
            this.nodes = NFVStateLoader.loadNodes(ndsvfile);
            this.services = NFVStateLoader.loadServices(ndsvfile);
            this.requests = new NFRequestsMap();
        }
        catch (IOException | ParseException e){
            e.printStackTrace();
        }
    }

    public void loadRequests(String requestsFile)
    {
        try
        {
            this.requests = NFVStateLoader.loadRequests(requestsFile);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

}
