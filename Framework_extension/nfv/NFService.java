package pt.uminho.algoritmi.netopt.nfv;

public class NFService
{
    private int id;
    private String name;
    private int cost;

    public NFService()
    {
        this.id = 0;
        this.name = "";
        this.cost = 0;
    }

    public NFService(int id, String name, int cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public NFService(NFService service)
    {
        this.id = service.getId();
        this.name = service.getName();
        this.cost = service.getCost();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
