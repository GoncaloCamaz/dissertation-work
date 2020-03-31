package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFServicesMap
{
    private Map<Integer, NFService> services;

    public NFServicesMap()
    {
        this.services = new HashMap<>();
    }

    public NFServicesMap(Map<Integer, NFService> services) {
        this.services = services;
    }

    public NFServicesMap(NFServicesMap serv)
    {
        this.services = serv.getServices();
    }

    public Map<Integer, NFService> getServices() {
        return services;
    }

    public void setServices(Map<Integer, NFService> services) {
        this.services = services;
    }
}
