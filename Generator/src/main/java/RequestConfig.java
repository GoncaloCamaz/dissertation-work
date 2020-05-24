import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RequestConfig
{
    private int id;
    private int originNodeID;
    private int destinationNodeID;
    private double bandwidthConsumption;
    private List<Integer> servicesRequested;

    public RequestConfig(int id)
    {
        this.id = id;
        this.originNodeID = 0;
        this.destinationNodeID = 0;
        this.bandwidthConsumption = 0;
        this.servicesRequested = new ArrayList<>();
    }

    public RequestConfig(int id, int originNodeID, int destinationNodeID , double bandwidthConsumption) {
        this.id = id;
        this.originNodeID = originNodeID;
        this.destinationNodeID = destinationNodeID;
        this.bandwidthConsumption = bandwidthConsumption;
        this.servicesRequested = new ArrayList<>();
    }

    public RequestConfig()
    {
        this.id = 0;
        this.originNodeID = 0;
        this.destinationNodeID = 0;
        this.bandwidthConsumption = 0;
        this.servicesRequested = new ArrayList<>();
    }

    public int getDestinationNodeID() {
        return destinationNodeID;
    }

    public void setDestinationNodeID(int destinationNodeID) {
        this.destinationNodeID = destinationNodeID;
    }

    public void setServicesRequested(List<Integer> servicesRequested) {
        this.servicesRequested = servicesRequested;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOriginNodeID() {
        return originNodeID;
    }

    public void setOriginNodeID(int originNodeID) {
        this.originNodeID = originNodeID;
    }

    public double getBandwidthConsumption() {
        return bandwidthConsumption;
    }

    public void setBandwidthConsumption(double bandwidthConsumption) {
        this.bandwidthConsumption = bandwidthConsumption;
    }

    public List<Integer> getServicesRequested() {
        return servicesRequested;
    }

    /**
     *
     * @param random
     * @param services
     */
    public void insertRandomServices(int random,List<Integer> services)
    {
        int num = 0;
        int i = 0;

        Collections.shuffle(services);
        while(num < random)
        {
            int toAdd = services.get(num);
            servicesRequested.add(toAdd);
            num++;
        }
        while(servicesRequested.size() < services.size())
        {
            servicesRequested.add(-1);
        }
    }
}
