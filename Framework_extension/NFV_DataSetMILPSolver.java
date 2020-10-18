package pt.uminho.algoritmi.netopt.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.OnlineNFRequest;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NFV_DataSetMILPSolver
{
    private NetworkTopology topology;
    private NFVState state;
    private List<OnlineNFRequest> requests;
    private int cplexTimeLimit;
    private int currentRequestID;
    private double alpha;
    private boolean mptcpenabled;

    public NFV_DataSetMILPSolver(NetworkTopology topology, NFVState state, List<OnlineNFRequest> requests, int currentRequestID, int cplexTimeLimit, double alpha)
    {
        this.topology = topology;
        this.state = state;
        this.requests = requests;
        this.currentRequestID = currentRequestID;
        this.cplexTimeLimit = cplexTimeLimit;
        this.alpha = alpha;
        this.mptcpenabled = false;
    }

    public OptimizationResultObject solve() throws IloException
    {
        OptimizationResultObject obj = new OptimizationResultObject(topology.getDimension());
        obj = optimize();
        return obj;
    }

    private OptimizationResultObject optimize()
    {
        double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
        Map<Integer, NFService> serv = this.state.getServices().getServices();
        Map<Integer, NFRequest> r = decodeOnlineRequests(this.requests);
        Map<Integer, NFNode> n = this.state.getNodes().getNodes();
        OptimizationResultObject res = new OptimizationResultObject(n.size());
        try {
            res = optimize(cp, serv, r, n, this.alpha);
        } catch (IloException e) {
            e.printStackTrace();
        }

        return res;
    }

    private OptimizationResultObject optimize(double[][] cp, Map<Integer, NFService> serv, Map<Integer, NFRequest> r, Map<Integer, NFNode> n, double alpha) throws IloException {
        IloCplex cplex = new IloCplex();
        cplex.setName("Multi commodity flow Phi and Node optimization");
        // Set of arcs regarding the topology
        Arcs arcs = new Arcs();

        cplex.setParam(IloCplex.Param.TimeLimit, this.cplexTimeLimit);
        // number of nodes
        int nodesNumber = topology.getDimension();
        // number of requests in map requests
        int requestNumber = r.size();
        // number of services in services map
        int servicesNumber = serv.size();

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
                if (cp[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, cp[i][j]);
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

        for (NFRequest request : r.values()) {
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
        for (NFNode node : n.values()) {
            int nodeID = node.getId();
            gamma_n.put(nodeID, cplex.numVar(0, Double.MAX_VALUE, "Gamma_" + nodeID));
        }

        // the r(n) variables
        HashMap<Integer, IloNumVar> r_n = new HashMap<>();
        for (NFNode node : n.values()) {
            int nodeID = node.getId();
            r_n.put(nodeID, cplex.numVar(0, Double.MAX_VALUE, "r_" + nodeID));
        }

        // Binary Variables
        // =====================================================================

        // alpha: a;
        // a[i][n][s] i -> request id; n -> node; s -> service
        // a either 1 or 0 if n is the node that will execute the s service for
        // the i request
        IloIntVar[][][] a = new IloIntVar[this.currentRequestID+1][nodesNumber][servicesNumber];
        for (NFRequest req : r.values()) {
            int reqID = req.getId();
            for (NFNode nd : n.values()) {
                int nodeID = nd.getId();
                for (NFService s : serv.values()) {
                    int sID = s.getId();
                    a[reqID][nodeID][sID] = cplex.intVar(0, 1, "alpha_" + reqID + "_" + nodeID + "_" + sID);
                    if (!nd.getAvailableServices().contains(sID))
                        cplex.addEq(a[reqID][nodeID][sID],0);
                }
            }
        }

        for(OnlineNFRequest request : this.requests)
        {
            if(request.getRequest().getId() != this.currentRequestID)
            {
                int[] result = request.getProcessmentLocation();
                List<Integer> requestedServices = request.getRequest().getServiceList();
                for(int i = 0; i < requestedServices.size(); i++)
                {
                    cplex.addEq(a[request.getRequest().getId()][result[i]][requestedServices.get(i)],0);
                }
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
            for (NFRequest request : r.values()) {
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
        for (NFRequest request : r.values()) {
            int rID = request.getId();
            for (int serviceID : request.getServiceList()) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                for (NFNode node : n.values()) {
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
        for (NFNode node : n.values()) {
            List<Integer> servicesAvailable = node.getAvailableServices();
            IloLinearNumExpr exp = cplex.linearNumExpr();
            for (NFRequest request : r.values()) {
                for (Integer servID : servicesAvailable) {
                    exp.addTerm(request.getBandwidth(), a[request.getId()][node.getId()][servID]);
                }
            }
            cplex.addEq(r_n.get(node.getId()), exp, "EQ12_Node_" + node.getId());
        }


        // EQUATION 15-20
        for (NFNode node : n.values()) {
            for (int j = 0; j < points.length; j++) {
                IloLinearNumExpr exp = cplex.linearNumExpr();
                exp.addTerm(1, gamma_n.get(node.getId()));
                exp.addTerm(-1 * slopes[j], r_n.get(node.getId()));
                double bi = -1 * points[j] * node.getProcessCapacity();
                cplex.addGe(exp, bi, "EQ15_20");
            }
        }

        // traffic conservation

        for (NFRequest request : r.values()) {
            for (NFRequestSegment s : request.getRequestSegments()) {
                for (NFNode nd : n.values()){

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

        if(this.mptcpenabled == false)
        {
            HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>> beta = new HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>>();

            for (NFRequest request : r.values()) {
                for (NFRequestSegment s : request.getRequestSegments()) {
                    HashMap<Arc, IloIntVar> l = new HashMap<>();
                    for (Arc arc : arcs) {
                        int source = arc.getFromNode();
                        int dest = arc.getToNode();
                        l.put(arc, cplex.intVar(0, 1, "beta_"+s.getRequestID()+"_"+s.getFrom()+ "_"+s.getTo() + "_" + source + "_" + dest));
                    }
                    beta.put(s,l);
                }
            }

            // EQUATION 20
            // laik = baik * xik
            for (Arc arc : arcs) {
                for (NFRequest request : r.values()) {
                    for (NFRequestSegment s : request.getRequestSegments()) {
                        IloLinearNumExpr la = cplex.linearNumExpr();
                        la.addTerm(s.getBandwidth(), beta.get(s).get(arc));
                        cplex.addEq(s_loads.get(s).get(arc), la, "EQ20_Arc_" + arc.getFromNode() + "_" + arc.getToNode() + "_Req_" + request.getId() + "_Seg_" + s.getFrom() + "_" + s.getTo());
                    }
                }
            }
        }



        // Saves the model
        cplex.exportModel("phigammaSolver.lp");
        OptimizationResultObject object = new OptimizationResultObject(nodesNumber);
        // Solve
        cplex.solve();

        double res = cplex.getObjValue();
        double[][] u = new double[topology.getDimension()][topology.getDimension()];
        double[] links = new double[topology.getNumberEdges()];

        for(int i = 0; i < topology.getDimension(); i++)
            for(int j = 0; j < topology.getDimension(); j++)
                u[i][j] = 0;

        for (Arc arc : arcs) {
            double utilization = cplex.getValue(l_a.get(arc));
            u[arc.getFromNode()][arc.getToNode()] = utilization;
            double result = utilization/arc.getCapacity();
            result =  Math.floor(result*100) / 100;
            links[arc.getIndex()] = result;
        }
        object.setLinksLoad1D(links);


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
        for(NFRequest request : r.values())
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
        for (NFNode node : n.values()) {
            double utNode = cplex.getValue(r_n.get(node.getId()));
            val += cplex.getValue(gamma_n.get(node.getId()));
            double result =  utNode/node.getProcessCapacity();
            result = Math.floor(result*100)/100;
            uNodes[node.getId()] = result;
        }
        object.setGammaValue(val / nodesNumber);

        object.setNodeUtilization(uNodes);
        object.setLoadValue(res);

        NetworkLoads networkload = new NetworkLoads(u, topology);
        networkload.printLoads();

        int[] serviceProcessmentLocation = new int[serv.size()];
        NFRequest request = new NFRequest();
        for(OnlineNFRequest reqs : requests)
        {
            if(reqs.getRequest().getId() == this.currentRequestID)
            {
                request = reqs.getRequest();
            }
        }

        List<Integer> requestedServices = request.getServiceList();

        for(int i = 0; i < servicesNumber; i++)
        {
            if(requestedServices.contains(i))
            {
                for(NFNode node : this.state.getNodes().getNodes().values())
                {
                    if(cplex.getValue(a[request.getId()][node.getId()][i]) > 0)
                    {
                        serviceProcessmentLocation[i] = node.getId();
                    }
                }
            }
            else
            {
                serviceProcessmentLocation[i] = -1;
            }
        }
        object.setServiceProcessmentLocation(serviceProcessmentLocation);

        return object;
    }

    private Map<Integer, NFRequest> decodeOnlineRequests(List<OnlineNFRequest> requests)
    {
        Map<Integer, NFRequest> reqs = new HashMap<>();

        for(OnlineNFRequest request : requests)
        {
            if(request.getDuration() > 0)
            {
                NFRequest r = request.getRequest();
                reqs.put(r.getId(), r);
            }
        }

        return reqs;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public void setTopology(NetworkTopology topology) {
        this.topology = topology;
    }

    public NFVState getState() {
        return state;
    }

    public void setState(NFVState state) {
        this.state = state;
    }

    public List<OnlineNFRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<OnlineNFRequest> requests) {
        this.requests = requests;
    }

    public int getCplexTimeLimit() {
        return cplexTimeLimit;
    }

    public void setCplexTimeLimit(int cplexTimeLimit) {
        this.cplexTimeLimit = cplexTimeLimit;
    }

    public int getCurrentRequestID() {
        return currentRequestID;
    }

    public void setCurrentRequestID(int currentRequestID) {
        this.currentRequestID = currentRequestID;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }
}
