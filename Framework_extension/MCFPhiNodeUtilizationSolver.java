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
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

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
    private boolean saveLoads;

    public MCFPhiNodeUtilizationSolver(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap r , NFNodesMap n) {
        this.topology = topology;
        this.services = servicesMap;
        this.NFRequestsMap = r;
        this.nodesMap = n;
        this.setSaveLoads(false);
    }

    public static void main(String[] args) {
        String nodesFile = args[0];
        String edgesFile = args[1];
        String servicesFile = args[2];
        String nodesInfoFile = args[3];
        String requests = args[4];

        try {
            NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
            NFServicesMap services = new NFServicesMap(servicesFile);
            NFNodesMap map = new NFNodesMap(nodesInfoFile);
            NFRequestsMap req = new NFRequestsMap(requests);
            MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, services, req, map);
            solver.setSaveLoads(true);
            solver.optimize();
            NetworkLoads loads= solver.getNetworkLoads();
            //System.out.println("Congestion = "+loads.getCongestion());
            System.out.println("MLU = "+loads.getMLU());
        } catch (Exception e) {
        }

    }

    public double optimize() throws IloException {
        return this.optimize(this.topology, this.services, this.NFRequestsMap, this.nodesMap);
    }

    /**
     * @throws IloException
     */
    public double optimize(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap req, NFNodesMap nodes) throws IloException {
        double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
        Map<Integer, NFService> serv = servicesMap.getServices();
        Map<Integer, NFRequest> r = req.getRequestMap();
        Map<Integer, NFNode> n = nodes.getNodes();
        double res = optimize(cp, serv, r, n);
       // Simul s = new Simul(topology);
        //double uncap = s.phiUncap(demands);
        //double normalized = res / uncap;
        return res;
    }

    /**
     * @param capacity double[][] with the capacity of each arc
     * @param services Map with all the services available
     * @param requests Map with the requests
     * @param nodes Map with all the nodes
     * @throws IloException
     */
    public double optimize(double[][] capacity, Map<Integer, NFService> services, Map<Integer,NFRequest> requests, Map<Integer, NFNode> nodes) throws IloException {

        IloCplex cplex = new IloCplex();
        cplex.setName("Multi commodity flow Phi and Node optimization");

        // assuming that there are multiple requests with same origin and destination
        HashMap<Arc, HashMap<Integer,IloNumVar>> xia = new HashMap<>();
        // Set of arcs regarding the topology
        Arcs arcs = new Arcs();
        // variable for objective function
        double alpha = 0.5;

        // number of nodes
        int nodesNumber = nodes.size();
        // number of requests in map requests
        int requestNumber = requests.size();
        // number of services in services map
        int servicesNumber = services.size();
        // number of arcs; Arc a => int fromNode; int toNode; double capacity; int index;
        int arcsNumber = arcs.getNumberOfArcs();

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
                    // node ID starts at 1
                    Arc a = new Arc(arcID, i+1, j+1, capacity[i][j]);
                    arcID++;
                    arcs.add(a);
                    HashMap<Integer, IloNumVar> xi = new HashMap<>();
                    for (NFRequest request : requests.values())
                    {
                        int reqID = request.getId();
                        int fromNode = a.getFromNode();
                        int toNode = a.getToNode();
                        xi.put(reqID, cplex.numVar(0, request.getBandwidth(), "x" + reqID + "_" + fromNode + "_" + toNode));
                    }
                    xia.put(a, xi);
                }

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
            gamma_n.put(nodeID,cplex.numVar(0, Double.MAX_VALUE, "Gamma_" + node));
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
                    if(nd.getAvailableServices().contains(s))
                    {
                        // service available at node nd, so the a may assume values from 0 to 1
                        a[reqID][nodeID][sID] = cplex.intVar(0,1,"alpha_" + reqID + "_" + nodeID + "_" + sID);
                    }
                    else
                    {
                        // service not available at node nd, so the a can only be 0
                        a[req.getId()][nd.getId()][s.getId()] = cplex.intVar(0,0,"alpha_" + req.getId() + "_" + nd.getId() + "_" + s.getId());
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
        // double alpha = 0.5 (defined at the top)
        // minimize the sum of all Phi(a)
        IloLinearNumExpr obj = cplex.linearNumExpr();
        for (IloNumVar ph : phi_a.values())
        {
            obj.addTerm(alpha, ph);
        }
        // minimize the sum of node utilization gamma(n)
        for(IloNumVar gm : gamma_n.values())
        {
            obj.addTerm(1-alpha, gm);
        }
        cplex.addMinimize(obj);

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
                IloNumVar x = xia.get(arc).get(request.getId()); // all IloNumVar associated to Arc arc
                la.addTerm(1,x);
            }
            cplex.addEq(li,la);
        }

        // EQUATION 2
        // the amount of trafffic associated to a request should not be splitted
        for(Arc arc : arcs)
        {
            HashMap<Integer, IloNumVar> xi = xia.get(arc);
            for(Integer i : xi.keySet())
            {
                IloNumVar xi_aux = xi.get(i);
                cplex.prod(xi_aux,b[i][arc.getFromNode()][arc.getToNode()]);
            }
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
                    IloNumVar x = xia.get(arc).get(req.getId());
                    ev.addTerm(-1, x);
                }
                // - xi
                for (Arc arc : fromV) {
                    // request x_i
                    IloNumVar x = xia.get(arc).get(req.getId());
                    ev.addTerm(1, x);
                }
                // if v is a producer, consumer or transient node
                if (nd.getId() == req.getSource())
                    cplex.addEq(ev, dst);
                else if (nd.getId() == req.getDestination())
                    cplex.addEq(ev, -1*dst);
                else
                    cplex.addEq(ev, 0);
            }
        }

        // EQUATION 4
        // The traffic associated to the request mey only be executed once at the node that implements
        // the service required
        // a[i][n][s] i -> request id; n -> node; s -> service
        for(Arc arc : arcs)
        {
            for(NFRequest request : requests.values())
            {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                for(NFService serv : services.values())
                {

                    // on this version of the problem, the cost associated to the Service can
                    // be considered as Ck(request) = request.getBandwidth()
                    IloNumVar xiAux = xia.get(arc).get(request.getId());
                    int arcToNode = arc.getToNode();
                    int requestID = request.getId();
                    int sID = serv.getId();
                    cplex.sum(cplex.prod(xia.get(arc).get(requestID), a[requestID][arcToNode][sID]), exp);
                }
                cplex.addEq(exp,xia.get(arc).get(request.getId()));
            }
        }

        // EQUATION 5 - 10
        for (Arc arc : arcs) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, phi_a.get(arc));
                exp.addTerm(-1 * slopes[j], l_a.get(arc));
                double bi = -1 * points[j] * arc.getCapacity();
                cplex.addGe(exp, bi);
            }
        }

        // EQUATION 12
        for(NFNode node : nodes.values())
        {
            List<Arc> arcsToN = arcs.getAllArcsTo(node.getId());
            IloNumVar rn = cplex.numVar(0,Double.MAX_VALUE);
            IloNumExpr exp = cplex.linearNumExpr();
            for(Arc arc : arcsToN)
            {
                List<NFService> servicesAvailable = node.getAvailableServices();
                for(NFService serv : servicesAvailable)
                {
                    for(NFRequest request : requests.values())
                    {
                        // custo de cK = pedido
                        int arcToNode = arc.getToNode();
                        int requestID = request.getId();
                        int sID = serv.getId();
                        cplex.sum(cplex.prod(cplex.prod(a[requestID][arcToNode][sID],xia.get(arc).get(requestID)),xia.get(arc).get(requestID)), exp);
                    }
                }
            }
            cplex.addEq(r_n.get(node.getId()),exp);
        }

        // EQUATION 13-18
        for (NFNode node : nodes.values()) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, gamma_n.get(node.getId()));
                exp.addTerm(-1 * slopes[j], r_n.get(node.getId()));
                double bi = -1 * points[j] * node.getProcessCapacity();
                cplex.addGe(exp, bi);
            }
        }

        // Saves the model
        // cplex.exportModel("lpex1.lp");

        // Solve
        cplex.solve();
        double res = cplex.getObjValue();

        if (this.saveLoads) {
            double[][] u = new double[topology.getDimension()][topology.getDimension()];
            for (Arc arc : arcs) {
                double utilization = cplex.getValue(l_a.get(arc));
                u[arc.getFromNode()][arc.getToNode()] = utilization;
            }
            this.loads = new NetworkLoads(u,topology);
            this.loads.printLoads();
            //Simul simul = new Simul(topology);
            //double congestion = simul.congestionMeasure(loads, this.demands);
            //System.out.println(congestion);
            //this.loads.setCongestion(congestion);
        }

        cplex.end();
        return res;
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
