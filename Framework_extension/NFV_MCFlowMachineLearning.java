package pt.uminho.algoritmi.netopt.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.ml.DataSetEntry;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NFV_MCFlowMachineLearning
{
    private NetworkTopology topology;
    private NFVState state;
    private DataSetEntry entry;
    private int cplexTimeLimit;
    private double alpha;

    public NFV_MCFlowMachineLearning(NetworkTopology topology, NFVState state, DataSetEntry entry, int cplexTimeLimit, double alpha)
    {
        this.topology = topology;
        this.state = state;
        this.cplexTimeLimit = cplexTimeLimit;
        this.alpha = alpha;
        this.entry = entry;
    }

    public int[] solve() throws IloException {
        int[] ret = new int[this.state.getServices().getServices().size()];

        ret = optimize();

        return ret;
    }

    private int[] optimize() throws IloException {
        Arcs arcs = decodeLinksCapacity(this.topology.getGraph().getCapacitie(), this.entry.getLinksState(), this.topology.getDimension());
        NFNodesMap nodesMap = decodeNodeCapacity(this.state.getNodes().getNodes(), this.entry.getNodesState());
        NFRequestsMap requestsMap = decodeRequests(this.entry.getOrigin(), this.entry.getDestination(), this.entry.getBandwidth(),
                this.entry.getRequests(), this.state.getServices().getServices());

        int[] result = optimize(arcs, this.state.getServices().getServices(), requestsMap.getRequestMap(), nodesMap.getNodes(), this.alpha);

        return result;
    }

    public int[] optimize(Arcs arcs, Map<Integer, NFService> services,
                          Map<Integer, NFRequest> requests, Map<Integer, NFNode> nodes, double alphaVal) throws IloException {

        IloCplex cplex = new IloCplex();
        int[] serviceProcessmentLocation = new int[services.size()];
        for(int i = 0; i < services.size(); i++)
        {
            serviceProcessmentLocation[i] = -1;
        }
        cplex.setName("Multi commodity flow Phi and Node optimization");
        this.state.getServices().setServices(services);
        this.state.getRequests().setRequestList(requests);
        this.state.getNodes().setNodes(nodes);

        // variable for objective function
        double alpha = alphaVal;

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
        double[] slopes = new double[]{1, 3, 10, 70, 500, 5000};

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


        HashMap<NFRequestSegment, HashMap<Arc, IloNumVar>> s_loads = new HashMap<NFRequestSegment, HashMap<Arc, IloNumVar>>();

        for (NFRequest request : requests.values()) {
            for (NFRequestSegment s : request.getRequestSegments()) {
                HashMap<Arc, IloNumVar> l = new HashMap<>();
                for (Arc arc : arcs) {
                    int source = arc.getFromNode();
                    int dest = arc.getToNode();
                    l.put(arc, cplex.numVar(0, Double.MAX_VALUE, "l_" + s.getRequestID() + "_" + s.getFrom() + " " + "_" + s.getTo() + source + "_" + dest));
                }
                s_loads.put(s, l);
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
                        cplex.addEq(a[reqID][nodeID][sID], 0);
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
            for (NFRequest request : requests.values()) {
                for (NFRequestSegment s : request.getRequestSegments()) {
                    la.addTerm(1, s_loads.get(s).get(arc));
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
                for (NFNode nd : nodes.values()) {

                    List<Arc> toV = arcs.getAllArcsTo(nd.getId());
                    List<Arc> fromV = arcs.getAllArcsFrom(nd.getId());
                    IloLinearNumExpr ev = cplex.linearNumExpr();
                    for (Arc arc : toV) {
                        ev.addTerm(-1, s_loads.get(s).get(arc));
                    }
                    for (Arc arc : fromV) {
                        ev.addTerm(1, s_loads.get(s).get(arc));
                    }


                    if (s.isRequestSource() && nd.getId() == s.getFrom() && s.isRequestDestination() && nd.getId() == s.getTo()) {
                        cplex.addEq(ev, 0);
                    } else if (s.isRequestSource() && nd.getId() != s.getFrom() && s.isRequestDestination() && nd.getId() != s.getTo()) {
                        cplex.addEq(ev, 0);
                    } else if (s.isRequestSource() && nd.getId() == s.getFrom()) {
                        if (!s.isRequestDestination())
                            ev.addTerm(1 * s.getBandwidth(), a[s.getRequestID()][nd.getId()][s.getTo()]);
                        cplex.addEq(ev, s.getBandwidth());
                    } else if (s.isRequestDestination() && nd.getId() == s.getTo()) {
                        if (!s.isRequestSource())
                            ev.addTerm(-1 * s.getBandwidth(), a[s.getRequestID()][nd.getId()][s.getFrom()]);
                        cplex.addEq(ev, -1 * s.getBandwidth());
                    } else {
                        if (!s.isRequestSource())
                            ev.addTerm(-1 * s.getBandwidth(), a[s.getRequestID()][nd.getId()][s.getFrom()]);
                        if (!s.isRequestDestination())
                            ev.addTerm(1 * s.getBandwidth(), a[s.getRequestID()][nd.getId()][s.getTo()]);
                        cplex.addEq(ev, 0);
                    }
                }
            }
        }

        // Solve
        cplex.solve();
        //===================================================================================================

        // SAVE RESULTS
        NFRequest request = requests.get(0);
        List<Integer> requestedServices = request.getServiceList();

        for(int i = 0; i < servicesNumber; i++)
        {
            if(requestedServices.contains(i))
            {
                for(NFNode node : nodes.values())
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

        cplex.end();
        return serviceProcessmentLocation;
    }

    /**
     * Transforms linear array of 0 and 1 values into a NFRequestsMap
     * @param origin
     * @param destination
     * @param bw
     * @param servicesRequired
     * @param servicesAvailable
     * @return
     */
    private NFRequestsMap decodeRequests(int origin, int destination, double bw, int[] servicesRequired, Map<Integer, NFService> servicesAvailable)
    {
        NFRequestsMap requestsMap = new NFRequestsMap();

        List<Integer> serviceList = new ArrayList<>();

        for(int i = 0; i < servicesAvailable.size(); i++)
        {
            if(servicesRequired[i] == 1)
            {
                serviceList.add(i);
            }
        }
        NFRequest request = new NFRequest(0,origin,destination,bw,serviceList);

        Map<Integer,NFRequest> rList = new HashMap<>();
        rList.put(request.getId(),request);

        requestsMap.setRequestList(rList);

        return requestsMap;
    }

    /**
     * Transforms percentage from linear array into Arcs
     * @param topoCapacities
     * @param percentages
     * @param topoSize
     * @return
     */
    private Arcs decodeLinksCapacity(double[][] topoCapacities, double[] percentages, int topoSize)
    {
        Arcs arcs = new Arcs();
        int arcID = 0;

        for(int i = 0; i < topoSize; i++)
        {
            for(int j = 0; j < topoSize; j++)
            {
                if(topoCapacities[i][j] > 0)
                {
                    double newcapacity = calculateCapacity(topoCapacities[i][j],percentages[arcID]);
                    Arc arc = new Arc(arcID,i,j,newcapacity);
                    arcs.add(arc);
                    arcID++;
                }
            }
        }

        return arcs;
    }

    /**
     * Transforms percentage from linear array into NFNodesMap
     * @param percentages
     * @param nodes
     * @return
     */
    private NFNodesMap decodeNodeCapacity(Map<Integer, NFNode> nodes, double[] percentages)
    {
        NFNodesMap nodesMap = new NFNodesMap();
        int nodeID = 0;

        for(NFNode node : nodes.values())
        {
            double newcapacity = calculateCapacity(node.getProcessCapacity(),percentages[nodeID]);
            node.setProcessCapacity(newcapacity);
            nodeID++;
        }
        nodesMap.setNodes(nodes);

        return nodesMap;
    }

    /**
     * Receives the full capacity of the node/link and its utilization percantage
     * @param fullcapacity
     * @param percentage
     * @return capacity left on the node/link
     */
    private double calculateCapacity(double fullcapacity, double percentage)
    {
        double percentageLeft = 1 - percentage;

        return fullcapacity * percentageLeft;
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

    public DataSetEntry getEntry() {
        return entry;
    }

    public void setEntry(DataSetEntry entry) {
        this.entry = entry;
    }
}