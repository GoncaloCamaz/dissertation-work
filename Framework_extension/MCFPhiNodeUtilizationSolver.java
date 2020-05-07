/*******************************************************************************
 * Copyright 2012-2019,
 *  Centro Algoritmi - University of Minho
 *
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Public License for more details.
 *
 *  You should have received a copy of the GNU Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  @author Vítor Pereira
 ******************************************************************************/

package pt.uminho.algoritmi.netopt.cplex;

import ilog.cplex.*;
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

import ilog.concert.*;

public class MCFPhiNodeUtilizationSolver {

    private NetworkTopology topology;
    private NFServicesMap services;
    private NetworkLoads loads;
    private NFRequestsMap NFRequestsMap;
    private NFNodesMap nodesMap;
    private int cplexTimeLimit;
    private boolean saveLoads;
    private boolean saveConfigurations;


    public MCFPhiNodeUtilizationSolver(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap r , NFNodesMap n) {
        this.topology = topology;
        this.services = servicesMap;
        this.NFRequestsMap = r;
        this.nodesMap = n;
        this.cplexTimeLimit = 86400;
        this.setSaveLoads(true);
        this.saveConfigurations = false;
    }

    public MCFPhiNodeUtilizationSolver(NetworkTopology topology, NFVState state, int timelimit)
    {
        this.topology = topology;
        this.services = state.getServices();
        this.NFRequestsMap = state.getRequests();
        this.nodesMap = state.getNodes();
        this.cplexTimeLimit = timelimit;
        this.setSaveLoads(true);
        this.saveConfigurations = false;
    }

    public static void main(String[] args) {
        String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
        String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
        String servicesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/frameworkConfiguration.json";
        String requests = "/Users/gcama/Desktop/Dissertacao/Work/Framework/NetOpt-master/pedidos.csv";//args[3];

        try {
            NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
            NFVState state = new NFVState(servicesFile, requests);
            NFServicesMap services = state.getServices();
            NFNodesMap map = state.getNodes();
            NFRequestsMap req = state.getRequests();

            MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, services, req, map);
            solver.setSaveLoads(true);
            solver.setCplexTimeLimit(60);
            OptimizationResultObject obj = solver.optimize();
            System.out.println(obj.toString());
        } catch (Exception e)
        {
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
    public OptimizationResultObject optimize(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap req, NFNodesMap nodes) throws IloException {
        double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
        Map<Integer, NFService> serv = servicesMap.getServices();
        Map<Integer, NFRequest> r = req.getRequestMap();
        Map<Integer, NFNode> n = nodes.getNodes();
        OptimizationResultObject res = optimize(cp, serv, r, n);

        return res;
    }

    /**
     * @param capacity double[][] with the capacity of each arc
     * @param services Map with all the services available
     * @param requests Map with the requests
     * @param nodes Map with all the nodes
     * @throws IloException
     */
    public OptimizationResultObject optimize(double[][] capacity, Map<Integer, NFService> services, Map<Integer,NFRequest> requests, Map<Integer, NFNode> nodes) throws IloException {

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
            for (int j = 0; j < nodesNumber; j++)
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcs.add(a);
                }

        // number of arcs; Arc a => int fromNode; int toNode; double capacity; int index;
        int arcsNumber = arcs.getNumberOfArcs();

        // the l(a) variables, load of arc a, being a = (u,v)
        HashMap<Arc, IloNumVar> l_a = new HashMap<>();
        for (Arc arc : arcs)
        {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            l_a.put(arc,cplex.numVar(0, Double.MAX_VALUE, "l_" + source + "_" + dest));
        }

        // the Phi(a) variables
        HashMap<Arc, IloNumVar> phi_a = new HashMap<>();
        for (Arc arc : arcs)
        {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            phi_a.put(arc,cplex.numVar(0, Double.MAX_VALUE, "Phi_" + source + "_" + dest));
        }

        // the gamma(n) variables
        HashMap<Integer, IloNumVar> gamma_n = new HashMap<>();
        for (NFNode node : nodes.values())
        {
            int nodeID = node.getId();
            gamma_n.put(nodeID,cplex.numVar(0, Double.MAX_VALUE, "Gamma_" + nodeID));
        }

        // the r(n) variables
        HashMap<Integer, IloNumVar> r_n = new HashMap<>();
        for (NFNode node : nodes.values())
        {
            int nodeID = node.getId();
            r_n.put(nodeID,cplex.numVar(0, Double.MAX_VALUE, "r_" + nodeID));
        }

        // Binary Variables
        // alpha: a; beta: b
        // a[i][n][s] i -> request id; n -> node; s -> service
        // a either 1 or 0 if n is the node that will execute the s service for the i request
        IloIntVar[][][] a = new IloIntVar[requestNumber][nodesNumber][servicesNumber];
        for(NFRequest req : requests.values())
        {
            int reqID = req.getId();
            for(NFNode nd : nodes.values())
            {
                int nodeID = nd.getId();
                for(NFService s : services.values())
                {
                    int sID = s.getId();
                    if(nd.getAvailableServices().contains(sID))
                    {
                        // service available at node nd, so the a may assume values from 0 to 1
                        a[reqID][nodeID][sID] = cplex.intVar(0,1,"alpha_" + reqID + "_" + nodeID + "_" + sID);
                    }
                    else
                    {
                        // service not available at node nd, so the a can only be 0
                        a[reqID][nodeID][sID] = cplex.intVar(0,0,"alpha_" + reqID + "_" + nodeID + "_" + sID);
                    }
                }

            }
        }

        // b[i][from][to] i -> request id; from -> arc node id; to -> arc node id
        IloIntVar[][][] b = new IloIntVar[requestNumber][nodesNumber][nodesNumber];
        for(NFRequest req : requests.values())
        {
            int id = req.getId();
            for(Arc arc : arcs)
            {
                int from = arc.getFromNode();
                int to = arc.getToNode();
                b[id][from][to] = cplex.intVar(0,1,"beta_" + id + "_" + from + "_" + to);
            }
        }

        // OBJECTIVE FUNCTION: alpha* phi + (1-alpha) * gamma
        // minimize the sum of all Phi(a)
        IloLinearNumExpr obj = cplex.linearNumExpr();
        double norm1 = alpha/arcsNumber;
        for (IloNumVar ph : phi_a.values())
        {
            obj.addTerm(norm1, ph);
        }
        // minimize the sum of node utilization gamma(n)
        double norm2 = (1-alpha)/nodesNumber;
        for(IloNumVar gm : gamma_n.values())
        {
            obj.addTerm(norm2, gm);
        }
        cplex.addMinimize(obj, "Objective_Function");

        // constraints
        // flow conservation

        // EQUATION 1
        // link loads are the sum of flows traveling over it, l(a) =
        for(Arc arc : arcs)
        {
            IloLinearNumExpr la = cplex.linearNumExpr();
            IloNumVar li = l_a.get(arc);
            for(NFRequest request : requests.values())
            {
                la.addTerm(request.getBandwidth(),b[request.getId()][arc.getFromNode()][arc.getToNode()]);
            }
            cplex.addEq(li,la,"EQ1_Arc_"+arc.getFromNode()+"_"+arc.getToNode());
        }

        // EQUATION 3
        // for all nodes v
        // n being the number of nodes
        for (NFNode nd : nodes.values())
        {
            // list of arcs that arrive at v and start at v
            List<Arc> toV = arcs.getAllArcsTo(nd.getId());
            List<Arc> fromV = arcs.getAllArcsFrom(nd.getId());
            for(NFRequest req : requests.values())
            {
                double dst = req.getBandwidth();
                IloLinearNumExpr ev = cplex.linearNumExpr();
                // xi
                for (Arc arc : toV) {
                    // request x_i
                    ev.addTerm(-1,b[req.getId()][arc.getFromNode()][arc.getToNode()]);
                }
                // - xi
                for (Arc arc : fromV) {
                    // request x_i
                    ev.addTerm(1,b[req.getId()][arc.getFromNode()][arc.getToNode()]);
                }
                // if v is a producer, consumer or transient node
                if (nd.getId() == req.getSource() && nd.getId() == req.getDestination())
                    cplex.addEq(ev, 0, "EQ3_Request_"+req.getId()+"_at_"+nd.getId());
                else if (nd.getId() == req.getSource())
                    cplex.addEq(ev, 1, "EQ3_Request_"+req.getId()+"_at_"+nd.getId());
                else if (nd.getId() == req.getDestination())
                    cplex.addEq(ev, -1, "EQ3_Request_"+req.getId()+"_at_"+nd.getId());
                else
                    cplex.addEq(ev, 0, "EQ3_Request_"+req.getId()+"_at_"+nd.getId());
            }
        }

        // EQUATION 4
        // The traffic associated to the request mey only be executed once at the node that implements
        // the service required
        // a[i][n][s] i -> request id; n -> node; s -> service
        for(NFRequest request : requests.values())
        {
            int rID = request.getId();
            for(Integer serviceID : request.getServiceList())
            {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                for (NFNode node : nodes.values())
                {
                    if(node.getAvailableServices().contains(serviceID))
                        exp.addTerm(1, a[request.getId()][node.getId()][serviceID]);
                    else
                        exp.addTerm(0, a[request.getId()][node.getId()][serviceID]);
                }
                cplex.addEq(exp, 1,"EQ4_Request_"+rID);
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
        for(NFNode node : nodes.values())
        {
            List<Integer> servicesAvailable = node.getAvailableServices();
            IloLinearNumExpr exp = cplex.linearNumExpr();
            for(NFRequest request : requests.values())
            {
                for(Integer servID : servicesAvailable)
                {
                    NFService service = services.get(servID);
                    exp.addTerm(request.getBandwidth()*2, a[request.getId()][node.getId()][service.getId()]);
                }
            }
            cplex.addEq(r_n.get(node.getId()), exp, "EQ12_Node_"+node.getId());
        }

        //EQUATION 13
        for(NFRequest request : requests.values())
        {
            for(NFNode node : nodes.values())
            {
                List<Integer> servicesAvailable = node.getAvailableServices();
                for(Integer serviceID : servicesAvailable)
                {
                    IloLinearNumExpr exp = cplex.linearNumExpr();
                    for(Arc arc : arcs.getAllArcsTo(node.getId()))
                    {
                        int toNode = arc.getToNode();
                        int fromNode = arc.getFromNode();
                        if(toNode != request.getSource())
                            exp.addTerm(1,b[request.getId()][fromNode][toNode]);
                        if(request.getSource() == toNode)
                            exp.addTerm(1,cplex.numVar(1,1));
                    }
                    cplex.addGe(exp, a[request.getId()][node.getId()][serviceID], "EQ13");
                }
            }
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

        // Saves the model
        cplex.exportModel("lpex1.lp");
        OptimizationResultObject object = new OptimizationResultObject(nodesNumber);
        // Solve
        cplex.solve();

        // SAVE RESULTS
        double res = cplex.getObjValue();
        if (this.saveLoads)
        {
            double[][] u = new double[topology.getDimension()][topology.getDimension()];
            double congestion = 0;
            for (Arc arc : arcs)
            {
                double utilization = cplex.getValue(l_a.get(arc));
                congestion += cplex.getValue(phi_a.get(arc));
                u[arc.getFromNode()][arc.getToNode()] = utilization;
            }
            object.setLinkLoads(u);
            object.setPhiValue(congestion/arcsNumber);

            double[] uNodes = new double[nodesNumber];
            double val = 0;
            for(NFNode node : nodes.values())
            {
                double ut = cplex.getValue(gamma_n.get(node.getId()));
                val += ut;
                uNodes[node.getId()] = ut;
            }
            object.setGammaValue(val/nodesNumber);
            object.setNodeUtilization(uNodes);
            object.setLoadValue(res);

            this.loads = new NetworkLoads(u,topology);
            this.loads.printLoads();


            HashMap<Integer,Integer> servicesDeployed = new HashMap<>();
            servicesDeployed = getServicesDeployed(this.nodesMap.getNodes());
            object.setServicesDeployed(servicesDeployed);

            boolean nodesInfo = allNodesWServices(this.nodesMap.getNodes());
            object.setAllNodesWServices(nodesInfo);
        }

        NFVRequestsConfigurationMap configurationMap = new NFVRequestsConfigurationMap();
        if(saveConfigurations)
        {
            for(NFRequest request : requests.values())
            {
                NFVRequestConfiguration configuration = new NFVRequestConfiguration();
                int reqID = request.getId();
                configuration.setRequestID(reqID);
                configuration.setServiceOrder(request.getServiceList());
                ArrayList<SourceDestinationPair> list = new ArrayList<>();
                for(Arc arc : arcs)
                {
                    int origin = arc.getFromNode();
                    int dest = arc.getToNode();
                    if(cplex.getValue(b[reqID][origin][dest]) > 0)
                    {
                        SourceDestinationPair pair = new SourceDestinationPair(origin,dest);
                        list.add(pair);
                    }
                }
                configuration.setSrpath(list);

                HashMap<Integer, Integer> nodesUsed = new HashMap<>();
                for(NFNode node : nodes.values())
                {
                    List<Integer> servicesAvailable = new ArrayList<>();
                    servicesAvailable = node.getAvailableServices();
                    int nodeID = node.getId();
                    for(Integer i : servicesAvailable)
                    {
                        if(cplex.getValue(a[reqID][nodeID][i]) > 0)
                        {
                            nodesUsed.put(i, nodeID);
                        }
                    }

                }
                configuration.setServiceProcessment(nodesUsed);
                configurationMap.addConfiguration(reqID, configuration);
            }
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

    private boolean allNodesWServices(Map<Integer, NFNode> nodes)
    {
        boolean ret = true;
        for(NFNode node : nodes.values())
        {
            if(node.getAvailableServices().size() == 0)
            {
                ret = false;
                break;
            }
        }
        return ret;
    }

    // returns the number of services deployed per node
    private HashMap<Integer, Integer> getServicesDeployed(Map<Integer, NFNode> nodes)
    {
        HashMap<Integer, Integer> services = new HashMap<>();
        for(NFNode node : nodes.values())
        {
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