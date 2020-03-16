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
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

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

        HashMap<Arc, IloNumVar[]> xa = new HashMap<Arc, IloNumVar[]>();

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

        for (int i = 0; i < nodesNumber; i++)
            for (int j = 0; j < nodesNumber; j++)
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(i, j, capacity[i][j]);
                    arcs.add(a);
                    IloNumVar x[] = new IloNumVar[requestNumber];
                    for (NFRequest request : requests.values())
                    {
                        // cplex x_ request id _ origin node from arc a _ destination node from arc a
                        // u -> id pedido;
                        x[request.getId()] = cplex.numVar(0, Double.MAX_VALUE, "x" + request.getId() + "_" + a.getFromNode() + "_" + a.getToNode());
                    }
                    xa.put(a, x);
                }

        // the l(a) variables, load of arc a
        IloNumVar l[] = new IloNumVar[arcsNumber];
        for (int arc = 0; arc < arcsNumber; arc++)
            l[arc] = cplex.numVar(0, Double.MAX_VALUE, "l_" + arc);

        // the Phi(a) variables
        IloNumVar phi[] = new IloNumVar[arcsNumber];
        for (int arc = 0; arc < arcsNumber; arc++)
            phi[arc] = cplex.numVar(0, Double.MAX_VALUE, "Phi_" + arc);

        // the gamma(n) variables
        IloNumVar gamma[] = new IloNumVar[nodesNumber];
        for (int nd = 0; nd < nodesNumber; nd++)
            gamma[nd] = cplex.numVar(0, Double.MAX_VALUE, "Gamma_" + nd);

        // the r(n) variables
        IloNumVar r[] = new IloNumVar[nodesNumber];
        for (int nd = 0; nd < nodesNumber; nd++)
            r[nd] = cplex.numVar(0, Double.MAX_VALUE, "r_" + nd);

        // variáveis alpha: a e beta: b
        // a[i][n][s] i -> request id; n -> node; s -> service
        // a either 1 or 0 if n is the node to execute the s service for the i request
        IloNumVar a[][][] = new IloNumVar[requestNumber][nodesNumber][servicesNumber];
        for(int i = 0; i < requestNumber; i++)
        {
            for(int n = 0; n < nodesNumber; n++)
            {
                NFNode node = nodes.get(n);
                for(int s = 0; s < servicesNumber; s++)
                {
                    NFService service = services.get(s);
                    if(node.getAvailableServices().contains(service))
                        a[i][n][s] = cplex.numVar(0,1, IloNumVarType.Int,"alpha_" + i + "_" + n + "_" + s);
                    else
                        a[i][n][s] = cplex.numVar(0,0,IloNumVarType.Int,"alpha_" + i + "_" + n + "_" + s);
                }

            }
        }

        // b[i][a] i -> request id; a -> arc id
        IloNumVar b[][] = new IloNumVar[requestNumber][arcsNumber];
        for(int i = 0; i < requestNumber; i++)
        {
            for(int arc = 0; arc < arcsNumber; arc++)
            {
                b[i][arc] = cplex.numVar(0,1, IloNumVarType.Int,"beta_" + i + "_" + arc);
            }
        }

        // equation 2
        for(int arc = 0; arc < arcsNumber; arc++)
        {
            Arc ar = arcs.getArc(arc);
            IloLinearNumExpr xi = cplex.linearNumExpr();
            IloNumVar x[] = xa.get(a);
            for(int i = 0; i < requestNumber; i++)
            {
                xi.addTerm(x[i], b[i][arc]); //TODO BETA
            }
            cplex.addEq(x[arc],xi);
        }


        // OBJECTIVE FUNCTION: alpha* phi + (1-alpha) * gamma
        // minimize the sum of all Phi(a)
        IloLinearNumExpr obj = cplex.linearNumExpr();
        for (int i = 0; i < arcsNumber; i++)
        {
            obj.addTerm(alpha, phi[i]);
        }
        // minimize the sum of node utilization gamma(n)
        for(int nd = 0; nd<nodesNumber; nd++)
        {
            obj.addTerm(1-alpha, gamma[nd]);
        }
        cplex.addMinimize(obj);

        // constraints
        // flow conservation

        // for all nodes v
        // n being the number of nodes
        for (int v = 0; v < nodesNumber; v++) {
            // list of arcs that arrive at v and start at v
            List<Arc> toV = arcs.getAllArcsTo(v);
            List<Arc> fromV = arcs.getAllArcsFrom(v);
            int i;
            for(i = 0; i < requestNumber; i++)
            {
                NFRequest req = this.NFRequestsMap.getRequestMap().get(i);
                double dst = req.getBandwidth();
                IloLinearNumExpr ev = cplex.linearNumExpr();
                // xi
                for (Arc arc : toV) {
                    IloNumVar x[] = xa.get(arc);
                    ev.addTerm(-1, x[i]);
                }
                // - xi
                for (Arc arc : fromV) {
                    IloNumVar x[] = xa.get(arc);
                    ev.addTerm(1, x[i]);
                }
                // if v is a producer, consumer or transient node
                if (v == req.getSource())
                    cplex.addEq(ev, dst);
                else if (v == req.getDestination())
                    cplex.addEq(ev, -1*dst);
                else
                    cplex.addEq(ev, 0);

            }
        }

        // link loads are the sum of flows traveling over it, l(a) =
        for (int i = 0; i < arcsNumber; i++) {
            Arc arc = arcs.getArc(i);
            IloLinearNumExpr la = cplex.linearNumExpr();
            IloNumVar x[] = xa.get(arc);
            for (int t = 0; t < requestNumber; t++) {
                la.addTerm(1, x[t]);
            }
            cplex.addEq(l[i], la);
        }

        // utilization costs sum at each node n
        for(int i = 0; i < nodesNumber; i++)
        {
            List<Arc> arcsL = arcs.getAllArcsTo(i);
            IloLinearNumExpr rn = cplex.linearNumExpr();
            for(Arc arc : arcsL)
            {
                int nodeID = arc.getToNode();
                NFNode node = nodes.get(nodeID);
                List<NFService> servicesAvailable = node.getAvailableServices();
                int size = xa.get(arc).length;
                IloNumVar request[] = new IloNumVar[size];
                request = xa.get(arc);
                for(int k = 0; k < size; k++)
                {
                    rn.addTerm(1,request[k]); // TODO MULTIPLY BY Ck * ALPHA
                }

            }
            cplex.addEq(r[i], rn);
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

        for (int i = 0; i < arcsNumber; i++) {
            Arc arc = arcs.getArc(i);
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, phi[i]);
                exp.addTerm(-1 * slopes[j], l[i]);
                double bi = -1 * points[j] * arc.getCapacity();
                cplex.addGe(exp, bi);
            }
        }

        for (int i = 0; i < nodesNumber; i++) {
            NFNode node = nodesMap.getNodes().get(i);
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, gamma[i]); 
                exp.addTerm(-1 * slopes[j], r[i]);
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
            for (int i = 0; i < arcsNumber; i++) {
                double utilization = cplex.getValue(l[i]);
                int src = arcs.getArc(i).getFromNode();
                int dst = arcs.getArc(i).getToNode();
                u[src][dst] = utilization;
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
