package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import pt.uminho.algoritmi.netopt.nfv.NFRequest;

public class OnlineNFRequest
{
    private NFRequest request;
    private int duration;
    private int[] processmentLocation;

    public OnlineNFRequest(NFRequest request, int duration, int numberOfServices) {
        this.request = request;
        this.duration = duration;
        this.processmentLocation = new int[numberOfServices];
    }

    public void decrementDuration()
    {
        this.duration--;
    }

    public NFRequest getRequest() {
        return request;
    }

    public void setRequest(NFRequest request) {
        this.request = request;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int[] getProcessmentLocation() {
        return processmentLocation;
    }

    public void setProcessmentLocation(int[] processmentLocation) {
        this.processmentLocation = processmentLocation;
    }
}
