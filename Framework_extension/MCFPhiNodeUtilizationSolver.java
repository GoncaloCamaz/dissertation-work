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
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

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
        HashMap<Arc, List<IloNumVar>> xa = new HashMap<>();

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

        int arcID = 0;
        for (int i = 0; i < nodesNumber; i++)
            for (int j = 0; j < nodesNumber; j++)
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcs.add(a);
                    List<IloNumVar> x = new ArrayList<>();
                    for (NFRequest request : requests.values())
                    {
                        // cplex x_ request id _ origin node from arc a _ destination node from arc a
                        // u -> id pedido;
                        x.add(cplex.numVar(0, Double.MAX_VALUE, "x" + request.getId() + "_" + a.getFromNode() + "_" + a.getToNode()));
                    }
                    xa.put(a, x);
                }

        // the l(a) variables, load of arc a, being a = (u,v)
        HashMap<SourceDestinationPair, IloNumVar> l_a = new HashMap<>();
        for (Arc arc : arcs)
        {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            SourceDestinationPair pair = new SourceDestinationPair(source,dest);
            l_a.put(pair,cplex.numVar(0, Double.MAX_VALUE, "l_" + source + "_" + dest));

        }

        // the Phi(a) variables
        HashMap<SourceDestinationPair, IloNumVar> phi_a = new HashMap<>();
        for (Arc arc : arcs)
        {
            int source = arc.getFromNode();
            int dest = arc.getToNode();
            SourceDestinationPair pair = new SourceDestinationPair(source,dest);
            phi_a.put(pair,cplex.numVar(0, Double.MAX_VALUE, "Phi_" + source + "_" + dest));

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

        // variáveis alpha: a e beta: b
        // a[i][n][s] i -> request id; n -> node; s -> service
        // a either 1 or 0 if n is the node to execute the s service for the i request
        IloNumVar a[][][] = new IloNumVar[requestNumber][nodesNumber][servicesNumber];
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
                        a[reqID][nodeID][sID] = cplex.numVar(0,1, IloNumVarType.Int,"alpha_" + reqID + "_" + nodeID + "_" + sID);
                    }
                    else
                    {
                        a[req.getId()][nd.getId()][s.getId()] = cplex.numVar(0,0,IloNumVarType.Int,"alpha_" + req.getId() + "_" + nd.getId() + "_" + s.getId());
                    }
                }

            }
        }

        // b[i][from][to] i -> request id; from -> arc node id; to -> arc node id
        IloNumVar b[][][] = new IloNumVar[requestNumber][nodesNumber][nodesNumber];
        for(NFRequest req : requests.values())
        {
            int id = req.getId();
            for(Arc arc : arcs)
            {
                int from = arc.getFromNode();
                int to = arc.getToNode();
                b[id][from][to] = cplex.numVar(0,1, IloNumVarType.Int,"beta_" + id + "_" + from + "_" + to);
            }
        }

        // equation 2
        //
        for(NFRequest req : requests.values())
        {
            for(Arc arc : arcs)
            {
                cplex.prod(b[req.getId()][arc.getFromNode()][arc.getToNode()], req.getBandwidth());
            }
        }

        // equation 4
        for(NFNode node : nodes.values())
        {

        }

        // OBJECTIVE FUNCTION: alpha* phi + (1-alpha) * gamma
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
                    List<IloNumVar> x = xa.get(arc);
                    for(IloNumVar n : x)
                        ev.addTerm(-1, n);
                }
                // - xi
                for (Arc arc : fromV) {
                    // request x_i
                    List<IloNumVar> x = xa.get(arc);
                    for(IloNumVar n : x)
                        ev.addTerm(1, n);
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


        // link loads are the sum of flows traveling over it, l(a) =
        for(Arc arc : arcs)
        {
            IloLinearNumExpr la = cplex.linearNumExpr();
            int source = arc.getFromNode(); int destination = arc.getToNode();
            SourceDestinationPair pair = new SourceDestinationPair(source, destination);
            IloNumVar li = l_a.get(pair);
            List<IloNumVar> x = xa.get(arc);
            for(IloNumVar n : x)
            {
                la.addTerm(1,n);
            }
            cplex.addEq(li,la);
        }

        // TODO EQUATION 12
        // utilization costs sum at each node n
        for(NFRequest request : requests.values())
        {
            for(Arc arc : arcs)
            {

            }
        }

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

        // EQUATION 5 - 10
        for (Arc arc : arcs) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                SourceDestinationPair pair = new SourceDestinationPair(arc.getFromNode(),arc.getToNode());
                exp.addTerm(1, phi_a.get(pair));
                exp.addTerm(-1 * slopes[j], l_a.get(pair));
                double bi = -1 * points[j] * arc.getCapacity();
                cplex.addGe(exp, bi);
            }
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
                SourceDestinationPair pair = new SourceDestinationPair(arc.getFromNode(), arc.getToNode());
                double utilization = cplex.getValue(l_a.get(pair));
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
