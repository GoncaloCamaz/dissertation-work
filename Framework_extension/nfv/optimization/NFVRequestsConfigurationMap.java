package pt.uminho.algoritmi.netopt.nfv.optimization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NFVRequestsConfigurationMap
{
    private Map<Integer, NFVRequestConfiguration> configurations;
    private Map<Integer, List<Integer>> serviceDeployment;

    public NFVRequestsConfigurationMap()
    {
        this.configurations = new HashMap<>();
        this.serviceDeployment = new HashMap<>();
    }

    public NFVRequestsConfigurationMap(Map<Integer, NFVRequestConfiguration> configurations, Map<Integer, List<Integer>> serviceDeployment) {
        this.configurations = configurations;
        this.serviceDeployment = serviceDeployment;
    }

    public Map<Integer, NFVRequestConfiguration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(Map<Integer, NFVRequestConfiguration> configurations) {
        this.configurations = configurations;
    }

    public Map<Integer, List<Integer>> getServiceDeployment() {
        return serviceDeployment;
    }

    public void setServiceDeployment(Map<Integer, List<Integer>> serviceDeployment) {
        this.serviceDeployment = serviceDeployment;
    }

    public void addConfiguration(int reqID, NFVRequestConfiguration configuration)
    {
        this.configurations.put(reqID, configuration);
    }
}
