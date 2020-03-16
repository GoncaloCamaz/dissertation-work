package pt.uminho.algoritmi.netopt.nfv;

import java.util.ArrayList;
import java.util.List;

public class NFNode
{
    private int id;
    private int processCapacity;
    private List<NFService> availableServices;

    public NFNode()
    {
        this.id = 0;
        this.processCapacity = 1000;
        this.availableServices = new ArrayList<>();
    }

    public NFNode(int id, int processCapacity, List<NFService> availableServices) {
        this.id = id;
        this.processCapacity = processCapacity;
        this.availableServices = availableServices;
    }

    public NFNode(NFNode node)
    {
        this.id = node.getId();
        this.processCapacity = node.getProcessCapacity();
        this.availableServices = node.getAvailableServices();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProcessCapacity() {
        return processCapacity;
    }

    public void setProcessCapacity(int processCapacity) {
        this.processCapacity = processCapacity;
    }

    public List<NFService> getAvailableServices() {
        return availableServices;
    }

    public void setAvailableServices(List<NFService> availableServices) {
        this.availableServices = availableServices;
    }
}
