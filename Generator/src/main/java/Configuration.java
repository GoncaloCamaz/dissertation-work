import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Configuration
{
    private int id;
    private int originNodeID;
    private int destinationNodeID;
    private int bandwidthConsumption;
    private List<Node> serviceNodes;

    public Configuration(int id)
    {
        this.id = id;
        this.originNodeID = 0;
        this.destinationNodeID = 0;
        this.bandwidthConsumption = 0;
        this.serviceNodes = new ArrayList<>();
    }

    public Configuration(int id, int originNodeID, int destinationNodeID ,int bandwidthConsumption) {
        this.id = id;
        this.originNodeID = originNodeID;
        this.destinationNodeID = destinationNodeID;
        this.bandwidthConsumption = bandwidthConsumption;
        this.serviceNodes = new ArrayList<>();
    }

    public Configuration()
    {
        this.id = 0;
        this.originNodeID = 0;
        this.destinationNodeID = 0;
        this.bandwidthConsumption = 0;
        this.serviceNodes = new ArrayList<>();
    }

    public int getDestinationNodeID() {
        return destinationNodeID;
    }

    public void setDestinationNodeID(int destinationNodeID) {
        this.destinationNodeID = destinationNodeID;
    }

    public void setServiceNodes(List<Node> serviceNodes) {
        this.serviceNodes = serviceNodes;
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

    public int getBandwidthConsumption() {
        return bandwidthConsumption;
    }

    public void setBandwidthConsumption(int bandwidthConsumption) {
        this.bandwidthConsumption = bandwidthConsumption;
    }

    public List<Node> getServiceNodes() {
        return serviceNodes;
    }

    public void insertRandomServices(int maxNodes, int random)
    {
        int num = 0;
        int service;
        while(num < maxNodes)
        {
            if(num < random)
            {
                service = 1;
            }
            else
            {
                service = 0;
            }
            Node n = new Node(num, service);
            this.serviceNodes.add(n);
            num++;
        }
        Collections.shuffle(this.serviceNodes);
    }

    public void insertAllOne(int maxNodes)
    {
        int num = 0;
        while(num < maxNodes)
        {
            Node n = new Node(num+1, 1);
            this.serviceNodes.add(n);
            num++;
        }
    }
}
