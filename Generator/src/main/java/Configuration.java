import java.util.ArrayList;
import java.util.List;

public class Configuration
{
    private int id;
    private int originNodeID;
    private int bandwidthConsumption;
    private List<Node> serviceNodes;

    public Configuration(int id)
    {
        this.id = id;
        this.originNodeID = 0;
        this.bandwidthConsumption = 0;
        this.serviceNodes = new ArrayList();
    }

    public Configuration(int id, int originNodeID, int bandwidthConsumption, List<Integer> nodesWServices, int maxNodes) {
        this.id = id;
        this.originNodeID = originNodeID;
        this.bandwidthConsumption = bandwidthConsumption;
        this.serviceNodes = new ArrayList<>();
        int num = 0;
    }

    public Configuration()
    {
        this.id = 0;
        this.originNodeID = 0;
        this.bandwidthConsumption = 0;
        this.serviceNodes = new ArrayList();
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

    public void insertRandomServices(int maxNodes, int random ,List<Integer> nodesWServices)
    {
        int num = 0;
        while(num < maxNodes)
        {
            Node n = new Node(num+1, 0);
            this.serviceNodes.add(n);
            num++;
        }

        for(Integer i : nodesWServices)
        {
            Node node = new Node(i, 1); //TODO IF NODES WITH SERVICES WRONG IN 1, DECREASE 1
            this.serviceNodes.set(i,node);
        }
    }
}
