package pt.uminho.algoritmi.netopt.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestsConfigurationMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NFV_MCFPMLUSolver
{
    private NetworkTopology topology;
    private NFServicesMap services;
    private NetworkLoads loads;
    private pt.uminho.algoritmi.netopt.nfv.NFRequestsMap NFRequestsMap;
    private NFNodesMap nodesMap;
    private int cplexTimeLimit;
    private boolean saveLoads;
    private boolean saveConfigurations;

    public NFV_MCFPMLUSolver(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap r,
                             NFNodesMap n) {
        this.topology = topology;
        this.services = servicesMap;
        this.NFRequestsMap = r;
        this.nodesMap = n;
        this.cplexTimeLimit = 86400;
        this.setSaveLoads(true);
        this.saveConfigurations = false;
    }

    public NFV_MCFPMLUSolver(NetworkTopology topology, NFVState state, int timelimit) {
        this.topology = topology;
        this.services = state.getServices();
        this.NFRequestsMap = state.getRequests();
        this.nodesMap = state.getNodes();
        this.cplexTimeLimit = timelimit;
        this.setSaveLoads(true);
        this.saveConfigurations = false;
    }

    public static void main(String[] args) {
        String nodesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";
        String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";
        String servicesFile = "frameworkConfiguration.json";
        String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidos.csv";

        try {
            NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
            NFVState state = new NFVState(servicesFile, requests);
            NFServicesMap services = state.getServices();
            NFNodesMap map = state.getNodes();
            NFRequestsMap req = state.getRequests();

            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, services, req, map);
            solver.setSaveLoads(true);
            solver.setSaveConfigurations(true);
            solver.setCplexTimeLimit(60);
            OptimizationResultObject obj = solver.optimize();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCplexTimeLimit() {
        return cplexTimeLimit;
    }

    public void setCplexTimeLimit(int cplexTimeLimit) {
        this.cplexTimeLimit = cplexTimeLimit;
    }

    public OptimizationResultObject optimize() throws IloException {
        return optimize(this.topology, this.services, this.NFRequestsMap, this.nodesMap);
    }

    /**
     * @throws IloException
     */
    public OptimizationResultObject optimize(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap req,
                                             NFNodesMap nodes) throws IloException {
        double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
        Map<Integer, NFService> serv = servicesMap.getServices();
        Map<Integer, NFRequest> r = req.getRequestMap();
        Map<Integer, NFNode> n = nodes.getNodes();
        OptimizationResultObject res = optimize(cp, serv, r, n);

        return res;
    }

    /**
     * @param capacity
     *            double[][] with the capacity of each arc
     * @param services
     *            Map with all the services available
     * @param requests
     *            Map with the requests
     * @param nodes
     *            Map with all the nodes
     * @throws IloException
     */
    public OptimizationResultObject optimize(double[][] capacity, Map<Integer, NFService> services,
                                             Map<Integer, NFRequest> requests, Map<Integer, NFNode> nodes) throws IloException {

        IloCplex cplex = new IloCplex();
        cplex.setName("Multi commodity flow Phi and Node optimization");
        // Set of arcs regarding the topology
        Arcs arcs = new Arcs();
        // variable for objective function
        double alpha = 0.5;
        cplex.setParam(IloCplex.Param.TimeLimit, this.cplexTimeLimit);
        // number of nodes
        int nodesNumber = topology.getDimension();
        // number of requests in map requests
        int requestNumber = requests.size();
        // number of services in services map
        int servicesNumber = services.size();

        int arcID = 0;
        for (int i = 0; i < nodesNumber; i++)
            for (int j = 0; j < nodesNumber; j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcs.add(a);
                }
            }

        // number of arcs; Arc a => int fromNode; int toNode; double capacity;
        // int index;
        int arcsNumber = arcs.getNumberOfArcs();

        // VARIABLES=============================================================================================

        // the l(a) variables, load of arc a, being a = (u,v)
        HashMap<Arc, IloNumVar> l_a = new HashMap<>();
        for (Arc arc : arcs) {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            l_a.put(arc, cplex.numVar(0, Double.MAX_VALUE, "l_" + source + "_" + dest));
        }


        HashMap<NFRequestSegment,HashMap<Arc, IloNumVar>> s_loads = new HashMap<NFRequestSegment,HashMap<Arc, IloNumVar>>();

        for (NFRequest request : requests.values()) {
            for (NFRequestSegment s : request.getRequestSegments()) {
                HashMap<Arc, IloNumVar> l = new HashMap<>();
                for (Arc arc : arcs) {
                    int source = arc.getFromNode();
                    int dest = arc.getToNode();
                    l.put(arc, cplex.numVar(0, Double.MAX_VALUE, "l_"+s.getRequestID()+"_"+s.getFrom()+" "+"_"+s.getTo() + source + "_" + dest));
                }
                s_loads.put(s,l);
            }
        }

        // the mlu variables
        HashMap<Arc, IloNumVar> mlu_a = new HashMap<>();
        for (Arc arc : arcs) {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            mlu_a.put(arc, cplex.numVar(0, Double.MAX_VALUE, "MLU_" + source + "_" + dest));
        }

        // the gamma(n) variables
        HashMap<Integer, IloNumVar> mnu_n = new HashMap<>();
        for (NFNode node : nodes.values()) {
            int nodeID = node.getId();
            mnu_n.put(nodeID, cplex.numVar(0, Double.MAX_VALUE, "MNU_" + nodeID));
        }

        // the r(n) variables
        HashMap<Integer, IloNumVar> r_n = new HashMap<>();
        for (NFNode node : nodes.values()) {
            int nodeID = node.getId();
            r_n.put(nodeID, cplex.numVar(0, Double.MAX_VALUE, "r_" + nodeID));
        }

        // Binary Variables
        // =====================================================================

        // alpha: a;
        // a[i][n][s] i -> request id; n -> node; s -> service
        // a either 1 or 0 if n is the node that will execute the s service for
        // the i request
        IloIntVar[][][] a = new IloIntVar[requestNumber][nodesNumber][servicesNumber];
        for (NFRequest req : requests.values()) {
            int reqID = req.getId();
            for (NFNode nd : nodes.values()) {
                int nodeID = nd.getId();
                for (NFService s : services.values()) {
                    int sID = s.getId();
                    a[reqID][nodeID][sID] = cplex.intVar(0, 1, "alpha_" + reqID + "_" + nodeID + "_" + sID);
                    if (!nd.getAvailableServices().contains(sID))
                        cplex.addEq(a[reqID][nodeID][sID],0);

                    System.out.println("a r"+reqID+" "+nodeID+" "+sID);
                }
            }
        }

        // MLU & MNU functions defenition
        for(Arc arc : arcs)
        {
            IloLinearNumExpr exp = cplex.linearNumExpr();
            exp.addTerm(1/arc.getCapacity(), l_a.get(arc));
            cplex.addLe(exp, mlu_a.get(arc));
        }

        for(NFNode node : nodes.values())
        {
            if(node.getProcessCapacity() > 0)
            {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1/node.getProcessCapacity(), r_n.get(node.getId()));
                cplex.addLe(exp, mnu_n.get(node.getId()));
            }
        }


        // OBJECTIVE FUNCTION: alpha* phi + (1-alpha) * gamma
        // ============================================

        // minimize the sum of all Phi(a)
        IloLinearNumExpr obj = cplex.linearNumExpr();
        double norm1 = alpha / arcsNumber;
        for (IloNumVar ph : mlu_a.values()) {
            obj.addTerm(norm1, ph);
        }
        // minimize the sum of node utilization gamma(n)
        double norm2 = (1 - alpha) / nodesNumber;
        for (IloNumVar gm : mnu_n.values()) {
            obj.addTerm(norm2, gm);
        }
        cplex.addMinimize(obj, "Objective_Function");

        // CONSTRAINTS
        // ===================================================================================

        // EQUATION 1
        // link loads are the sum of flows traveling over it, l(a) =
        for (Arc arc : arcs) {
            IloLinearNumExpr la = cplex.linearNumExpr();
            IloNumVar li = l_a.get(arc);
            for (NFRequest request : requests.values()) {
                for (NFRequestSegment s : request.getRequestSegments()) {
                    la.addTerm(1,s_loads.get(s).get(arc));
                }
            }
            cplex.addEq(li, la, "EQ1_Arc_" + arc.getFromNode() + "_" + arc.getToNode());
        }


        // EQUATION 4
        // The traffic associated to the request may only be executed once at
        // the node that implements
        // the service required
        // a[i][n][s] i -> request id; n -> node; s -> service
        for (NFRequest request : requests.values()) {
            int rID = request.getId();
            for (int serviceID : request.getServiceList()) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                for (NFNode node : nodes.values()) {
                    exp.addTerm(1, a[request.getId()][node.getId()][serviceID]);
                }
                cplex.addEq(exp, 1, "EQ4_Request_" + rID + "_" + serviceID);
            }
        }

        // EQUATION 12
        for (NFNode node : nodes.values()) {
            List<Integer> servicesAvailable = node.getAvailableServices();
            IloLinearNumExpr exp = cplex.linearNumExpr();
            for (NFRequest request : requests.values()) {
                for (Integer servID : servicesAvailable) {
                    exp.addTerm(request.getBandwidth(), a[request.getId()][node.getId()][servID]);
                }
            }
            cplex.addEq(r_n.get(node.getId()), exp, "EQ12_Node_" + node.getId());
        }

        // traffic conservation

        for (NFRequest request : requests.values()) {
            for (NFRequestSegment s : request.getRequestSegments()) {
                for (NFNode nd : nodes.values()){

                    List<Arc> toV = arcs.getAllArcsTo(nd.getId());
                    List<Arc> fromV = arcs.getAllArcsFrom(nd.getId());
                    IloLinearNumExpr ev = cplex.linearNumExpr();
                    for (Arc arc : toV) {
                        ev.addTerm(-1, s_loads.get(s).get(arc));
                    }
                    for (Arc arc : fromV) {
                        ev.addTerm(1, s_loads.get(s).get(arc));
                    }


                    if(s.isRequestSource() && nd.getId()==s.getFrom() && s.isRequestDestination() && nd.getId()==s.getTo()){
                        cplex.addEq(ev, 0);
                    }else if(s.isRequestSource() && nd.getId()!=s.getFrom() && s.isRequestDestination() && nd.getId()!=s.getTo()){
                        cplex.addEq(ev, 0);
                    }
                    else if(s.isRequestSource() && nd.getId()==s.getFrom()){
                        if(!s.isRequestDestination())
                            ev.addTerm(1*s.getBandwidth(),a[s.getRequestID()][nd.getId()][s.getTo()]);
                        cplex.addEq(ev, s.getBandwidth());
                    }
                    else if(s.isRequestDestination() && nd.getId()==s.getTo()){
                        if(!s.isRequestSource())
                            ev.addTerm(-1*s.getBandwidth(),a[s.getRequestID()][nd.getId()][s.getFrom()]);
                        cplex.addEq(ev, -1* s.getBandwidth());
                    }
                    else{
                        if(!s.isRequestSource())
                            ev.addTerm(-1*s.getBandwidth(),a[s.getRequestID()][nd.getId()][s.getFrom()]);
                        if(!s.isRequestDestination())
                            ev.addTerm(1*s.getBandwidth(),a[s.getRequestID()][nd.getId()][s.getTo()]);
                        cplex.addEq(ev, 0);
                    }
                }
            }
        }



        // Saves the model
        cplex.exportModel("mlumnuSolver.lp");
        OptimizationResultObject object = new OptimizationResultObject(nodesNumber);
        // Solve
        cplex.solve();

        //===================================================================================================

        // SAVE RESULTS
        double res = cplex.getObjValue();
        if (this.saveLoads) {
            double[][] u = new double[topology.getDimension()][topology.getDimension()];

            for(int i = 0; i < topology.getDimension(); i++)
                for(int j = 0; j < topology.getDimension(); j++)
                    u[i][j] = 0;

            double mlu = 0;
            for (Arc arc : arcs) {
                double utilization = cplex.getValue(l_a.get(arc));
                u[arc.getFromNode()][arc.getToNode()] = utilization;
                mlu += cplex.getValue(mlu_a.get(arc));
            }

            object.setMlu(mlu/topology.getNumberEdges());
            object.setLinkLoads(u);


            double[] uNodes = new double[nodesNumber];
            double val = 0;
            for (NFNode node : nodes.values()) {
                double ut = cplex.getValue(mnu_n.get(node.getId()));
                val += ut;
                uNodes[node.getId()] = ut;
            }
            object.setMnu(val / nodesNumber);

            object.setNodeUtilization(uNodes);
            object.setLoadValue(res);

            this.loads = new NetworkLoads(u, topology);
            this.loads.printLoads();

            HashMap<Integer, Integer> servicesDeployed = new HashMap<>();
            servicesDeployed = getServicesDeployed(this.nodesMap.getNodes());
            object.setServicesDeployed(servicesDeployed);

            boolean nodesInfo = allNodesWServices(this.nodesMap.getNodes());
            object.setAllNodesWServices(nodesInfo);
        }

        NFVRequestsConfigurationMap configurationMap = new NFVRequestsConfigurationMap();
        // if true, save NFV Configurations
        if(isSaveConfigurations())
        {
            for(NFRequest request : requests.values())
            {
                NFVRequestConfiguration configuration = new NFVRequestConfiguration();
                int reqID = request.getId();

                // Set NFRequest in NFVRequestConfiguration ///////////////////
                configuration.setRequestOrigin(request.getSource());
                configuration.setRequestDestination(request.getDestination());
                configuration.setRequestID(reqID);
                configuration.setServiceOrder(request.getServiceList());
                configuration.setBandwidth(request.getBandwidth());
                ///////////////////////////////////////////////////////////////

                // Get SR path
                ArrayList<SourceDestinationPair> list = new ArrayList<>();
                for (NFRequestSegment s : request.getRequestSegments()) {
                    HashMap<Arc, IloNumVar> l = s_loads.get(s);
                    for (Arc arc : arcs) {
                        if(cplex.getValue(l.get(arc))>0)
                        {
                            SourceDestinationPair pair = new SourceDestinationPair(arc.getFromNode(),arc.getToNode());
                            list.add(pair);
                        }
                    }
                }
                configuration.setSrpath(list);

                // Nodes usage for each service in each request
                HashMap<Integer, Integer> nodesUsed = new HashMap<>();
                for(NFNode node : nodes.values())
                {
                    int nodeID = node.getId();
                    for(NFService service : services.values())
                    {
                        int sID = service.getId();
                        if(cplex.getValue(a[reqID][nodeID][sID]) > 0)
                        {
                            nodesUsed.put(sID, nodeID);
                        }
                    }
                }
                configuration.setServiceProcessment(nodesUsed);
                configurationMap.addConfiguration(reqID, configuration);
            }

            // Services Deployment Location
            HashMap<Integer, List<Integer>> servicesDeployment = new HashMap<>();
            for(NFNode node : nodes.values())
            {
                List<Integer> availableServices = new ArrayList<>();
                availableServices = node.getAvailableServices();
                int nodeID = node.getId();
                servicesDeployment.put(nodeID,availableServices);
            }
            configurationMap.setServiceDeployment(servicesDeployment);
        }
        object.setNfvRequestsConfigurationMap(configurationMap);

        cplex.end();
        return object;
    }

    private boolean allNodesWServices(Map<Integer, NFNode> nodes) {
        boolean ret = true;
        for (NFNode node : nodes.values()) {
            if (node.getAvailableServices().size() == 0) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    // returns the number of services deployed per node
    private HashMap<Integer, Integer> getServicesDeployed(Map<Integer, NFNode> nodes) {
        HashMap<Integer, Integer> services = new HashMap<>();
        for (NFNode node : nodes.values()) {
            services.put(node.getId(), node.getAvailableServices().size());
        }

        return services;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public void setTopology(NetworkTopology topology) {
        this.topology = topology;
    }

    public NFServicesMap getServices() {
        return services;
    }

    public void setServices(NFServicesMap services) {
        this.services = services;
    }

    public NetworkLoads getLoads() {
        return loads;
    }

    public void setLoads(NetworkLoads loads) {
        this.loads = loads;
    }

    public pt.uminho.algoritmi.netopt.nfv.NFRequestsMap getNFRequestsMap() {
        return NFRequestsMap;
    }

    public void setNFRequestsMap(pt.uminho.algoritmi.netopt.nfv.NFRequestsMap NFRequestsMap) {
        this.NFRequestsMap = NFRequestsMap;
    }

    public NFNodesMap getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(NFNodesMap nodesMap) {
        this.nodesMap = nodesMap;
    }

    public boolean isSaveConfigurations() {
        return saveConfigurations;
    }

    public void setSaveConfigurations(boolean saveConfigurations) {
        this.saveConfigurations = saveConfigurations;
    }

    public boolean isSaveLoads() {
        return saveLoads;
    }

    /**
     * Defines if besides the congestion value, network link loads are also
     * saved as an instance of NetworkLoads
     */
    public void setSaveLoads(boolean saveLoads) {
        this.saveLoads = saveLoads;
    }

    public NetworkLoads getNetworkLoads() {
        return this.loads;
    }
}
