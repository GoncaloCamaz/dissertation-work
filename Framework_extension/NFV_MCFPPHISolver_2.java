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
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NFV_MCFPPHISolver_2
{
    private NetworkTopology topology;
    private NFServicesMap services;
    private NetworkLoads loads;
    private NFRequestsMap nfRequestsMap;
    private NFNodesMap nodesMap;
    private int cplexTimeLimit;
    private double alpha;
    private boolean saveLoads;
    private boolean saveConfigurations;

    public NFV_MCFPPHISolver_2(NetworkTopology topology, NFVState state, int timelimit, double alpha) {
        this.topology = topology;
        this.services = state.getServices();
        this.nfRequestsMap = state.getRequests();
        this.nodesMap = state.getNodes();
        this.cplexTimeLimit = timelimit;
        this.alpha = alpha;
        this.setSaveLoads(true);
        this.saveConfigurations = false;
    }

    public NFV_MCFPPHISolver_2(NetworkTopology topology, NFServicesMap services, NetworkLoads loads, NFRequestsMap nfRequestsMap, NFNodesMap nodesMap, int cplexTimeLimit, double alpha, boolean saveLoads, boolean saveConfigurations) {
        this.topology = topology;
        this.services = services;
        this.loads = loads;
        this.nfRequestsMap = nfRequestsMap;
        this.nodesMap = nodesMap;
        this.cplexTimeLimit = cplexTimeLimit;
        this.alpha = alpha;
        this.saveLoads = saveLoads;
        this.saveConfigurations = saveConfigurations;
    }

    public static void main(String[] args) throws Exception {
        String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
        String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
        String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_Abilene.json";
        String requests = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosAbilene_300.csv";

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile, requests);

        NFV_MCFPPHISolver_2 solver = new NFV_MCFPPHISolver_2(topology,state,30,0.5);
        OptimizationResultObject res = solver.optimize();
        System.out.println("Done");
    }

    public OptimizationResultObject optimize(){
        return optimize(this.topology, this.services, this.nfRequestsMap, this.nodesMap, this.alpha);
    }

    private OptimizationResultObject optimize(NetworkTopology topology, NFServicesMap services, NFRequestsMap nfRequestsMap, NFNodesMap nodesMap, double alpha)
    {
        OptimizationResultObject res = new OptimizationResultObject(nodesMap.getNodes().size());
        double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
        Map<Integer, NFService> serv = services.getServices();
        Map<Integer, NFRequest> r = nfRequestsMap.getRequestMap();
        Map<Integer, NFNode> n = nodesMap.getNodes();

        try{
            res = optimize(cp, serv, r, n, alpha);
        }
        catch(IloException e){
            e.printStackTrace();
        }

        return res;
    }

    private OptimizationResultObject optimize(double[][] capacity, Map<Integer, NFService> services,
                                              Map<Integer, NFRequest> requests, Map<Integer, NFNode> nodes, double alphaVal) throws IloException
    {
        IloCplex cplex = new IloCplex();
        cplex.setName("Multi commodity flow Phi and Node optimization");

        // Set of arcs regarding the topology
        Arcs arcs = new Arcs();
        // variable for objective function
        double alpha = alphaVal;
        System.out.println("Alpha: " + alpha);
        cplex.setParam(IloCplex.Param.TimeLimit, this.cplexTimeLimit);
        // number of nodes
        int nodesNumber = topology.getDimension();
        // number of requests in map requests
        int requestNumber = requests.size();
        // number of services in services map
        int servicesNumber = services.size();

        //Creates the result object
        OptimizationResultObject object = new OptimizationResultObject(nodesNumber);

        // Convex piecewise linear function Phi
        // As the problem is a minimization problem, the penalizing function can
        // be defined as a set of constraints for each arc
        double[] points = new double[6];
        points[0] = 0.0;
        points[1] = 2.0 / 3;
        points[2] = 16.0 / 3;
        points[3] = 178.0 / 3;
        points[4] = 1468.0 / 3;
        points[5] = 16318.0 / 3;
        double[] slopes = new double[] { 1, 3, 10, 70, 500, 5000 };

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

        // the Phi(a) variables
        HashMap<Arc, IloNumVar> phi_a = new HashMap<>();
        for (Arc arc : arcs) {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            phi_a.put(arc, cplex.numVar(0, Double.MAX_VALUE, "Phi_" + source + "_" + dest));
        }

        // the gamma(n) variables
        HashMap<Integer, IloNumVar> gamma_n = new HashMap<>();
        for (NFNode node : nodes.values()) {
            int nodeID = node.getId();
            gamma_n.put(nodeID, cplex.numVar(0, Double.MAX_VALUE, "Gamma_" + nodeID));
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
                }
            }
        }

        HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>> beta = new HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>>();

        for (NFRequest request : requests.values()) {
            for (NFRequestSegment s : request.getRequestSegments()) {
                HashMap<Arc, IloIntVar> l = new HashMap<>();
                for (Arc arc : arcs) {
                    int source = arc.getFromNode();
                    int dest = arc.getToNode();
                    l.put(arc, cplex.intVar(0, 1, "beta_"+s.getRequestID()+"_"+s.getFrom()+" "+"_"+s.getTo() + source + "_" + dest));
                }
                beta.put(s,l);
            }
        }



        // OBJECTIVE FUNCTION: alpha* phi + (1-alpha) * gamma
        // ============================================

        // minimize the sum of all Phi(a)
        IloLinearNumExpr obj = cplex.linearNumExpr();
        double norm1 = alpha / arcsNumber;
        for (IloNumVar ph : phi_a.values()) {
            obj.addTerm(norm1, ph);
        }
        // minimize the sum of node utilization gamma(n)
        double norm2 = (1 - alpha) / nodesNumber;
        for (IloNumVar gm : gamma_n.values()) {
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
                    la.addTerm(1, s_loads.get(s).get(arc));
                }
            }
            cplex.addEq(li, la, "EQ1_Arc_" + arc.getFromNode() + "_" + arc.getToNode());
        }

        // EQUATION 20
        // laik = baik * xik
        for (Arc arc : arcs) {
            for (NFRequest request : requests.values()) {
                for (NFRequestSegment s : request.getRequestSegments()) {
                    IloLinearNumExpr la = cplex.linearNumExpr();
                    la.addTerm(request.getBandwidth(), beta.get(s).get(arc));
                    cplex.addEq(s_loads.get(s).get(arc), la, "EQ20_Arc_" + arc.getFromNode() + "_" + arc.getToNode() + "_Req_" + request.getId() + "_Seg_" + s.getFrom() + "_" + s.getTo());
                }
            }
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

        // EQUATION 5 - 10
        for (Arc arc : arcs) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, phi_a.get(arc));
                exp.addTerm(-1 * slopes[j], l_a.get(arc));
                double bi = -1 * points[j] * arc.getCapacity();
                cplex.addGe(exp, bi, "EQ5_10");
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


        // EQUATION 15-20
        for (NFNode node : nodes.values()) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, gamma_n.get(node.getId()));
                exp.addTerm(-1 * slopes[j], r_n.get(node.getId()));
                double bi = -1 * points[j] * node.getProcessCapacity();
                cplex.addGe(exp, bi, "EQ15_20");
            }
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
        cplex.exportModel("phigammaSolver.lp");
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

            for (Arc arc : arcs) {
                double utilization = cplex.getValue(l_a.get(arc));
                u[arc.getFromNode()][arc.getToNode()] = utilization;
            }


            /** Normalized PHI Value demands conversion**/
            NetworkLoads loads = new NetworkLoads(u, topology);
            Simul simul = new Simul(topology);

            /**Initialization**/
            double[][] demands = new double[topology.getDimension()][topology.getDimension()];
            for(int i = 0; i < topology.getDimension(); i++)
            {
                for(int j = 0; j < topology.getDimension(); j++)
                {
                    demands[i][j] = 0;
                }
            }

            /**Conversion**/
            for(NFRequest request : requests.values())
            {
                for (NFRequestSegment s : request.getRequestSegments()) {
                    HashMap<Arc, IloNumVar> l = s_loads.get(s);
                    for (Arc arc : arcs) {
                        if(cplex.getValue(l.get(arc))>0)
                        {
                            demands[arc.getFromNode()][arc.getToNode()] += request.getBandwidth();
                        }
                    }
                }
            }
            object.setPhiValue(simul.congestionMeasure(loads,demands));
            object.setLinkLoads(u);

            double[] uNodes = new double[nodesNumber];
            double val = 0;
            for (NFNode node : nodes.values()) {
                double utNode = cplex.getValue(r_n.get(node.getId()));
                val += cplex.getValue(gamma_n.get(node.getId()));
                uNodes[node.getId()] = utNode;
            }
            object.setGammaValue(val / nodesNumber);

            object.setNodeUtilization(uNodes);
            object.setLoadValue(res);

            this.loads = new NetworkLoads(u, topology);
            this.loads.printLoads();

            HashMap<Integer, Integer> servicesDeployed = new HashMap<>();
            servicesDeployed = getServicesDeployed(this.nodesMap.getNodes());
            object.setServicesDeployed(servicesDeployed);

            boolean nodesInfo = allServicesDeployed(this.nodesMap.getNodes());
            object.setAllservicesDeployed(nodesInfo);
        }

        NFVRequestsConfigurationMap configurationMap = new NFVRequestsConfigurationMap();
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

    private boolean allServicesDeployed(Map<Integer, NFNode> nodes) {
        boolean ret = true;

        List<Integer> servicesAux = new ArrayList<>();
        for(NFService service : services.getServices().values())
        {
            servicesAux.add(service.getId());
        }

        for(NFNode node : nodes.values())
        {
            List<Integer> aux = node.getAvailableServices();
            for(Integer i : aux)
            {
                if(servicesAux.contains(i))
                {
                    servicesAux.remove(i);
                }
            }
        }
        if(servicesAux.size() != 0)
            ret = false;

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

    public NFRequestsMap getNFRequestsMap() {
        return nfRequestsMap;
    }

    public void setNFRequestsMap(NFRequestsMap nfRequestsMap) {
        this.nfRequestsMap = nfRequestsMap;
    }

    public NFNodesMap getNodesMap() {
        return nodesMap;
    }

    public void setNodesMap(NFNodesMap nodesMap) {
        this.nodesMap = nodesMap;
    }

    public int getCplexTimeLimit() {
        return cplexTimeLimit;
    }

    public void setCplexTimeLimit(int cplexTimeLimit) {
        this.cplexTimeLimit = cplexTimeLimit;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public boolean isSaveLoads() {
        return saveLoads;
    }

    public void setSaveLoads(boolean saveLoads) {
        this.saveLoads = saveLoads;
    }

    public boolean isSaveConfigurations() {
        return saveConfigurations;
    }

    public void setSaveConfigurations(boolean saveConfigurations) {
        this.saveConfigurations = saveConfigurations;
    }
}
