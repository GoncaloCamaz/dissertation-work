package pt.uminho.algoritmi.netopt.nfv;

import java.util.HashMap;
import java.util.Map;

public class NFServicesMap
{
    private Map<Integer, NFService> services;
    private String filename;

    public NFServicesMap()
    {
        this.services = new HashMap<>();
        this.filename = "";
    }

    public NFServicesMap(String filename)
    {
        this.filename = filename;
        this.services = new HashMap<>();
    }

    public NFServicesMap(Map<Integer, NFService> services, String filename) {
        this.services = services;
        this.filename = filename;
    }

    public Map<Integer, NFService> getServices() {
        return services;
    }

    public void setServices(Map<Integer, NFService> services) {
        this.services = services;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
