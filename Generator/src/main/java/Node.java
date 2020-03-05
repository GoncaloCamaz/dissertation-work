public class Node
{
    private int nodeID;
    private int withservice;

    public Node()
    {
        this.nodeID = 0;
        this.withservice = 0;
    }

    public Node(int id, int service)
    {
        this.nodeID = id;
        this.withservice = service;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getWithservice() {
        return withservice;
    }

    public void setWithservice(int withservice) {
        this.withservice = withservice;
    }
}
