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
 *  @author V�tor Pereira
 ******************************************************************************/

package pt.uminho.algoritmi.netopt.cplex;

import ilog.cplex.*;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.cplex.utils.SourceDestinationPair;
import pt.uminho.algoritmi.netopt.nfv.*;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.OnlineNFRequest;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestsConfigurationMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkLoads;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

import ilog.concert.*;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.Simul;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import static pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver.saveToCSV;
import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;

public class NFV_MCFPhiSolver {

	private NetworkTopology topology;
	private NFServicesMap services;
	private NetworkLoads loads;
	private NFRequestsMap NFRequestsMap;
	private NFNodesMap nodesMap;
	private int cplexTimeLimit;
	private boolean mptcpenabled;
	private double alpha;
	private boolean saveLoads;
	private boolean saveConfigurations;
	private boolean randomSRRequests;

	public NFV_MCFPhiSolver(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap r,
							NFNodesMap n) {
		this.topology = topology;
		this.services = servicesMap;
		this.NFRequestsMap = r;
		this.nodesMap = n;
		this.cplexTimeLimit = 6400;
		this.alpha = 0.5;
		this.mptcpenabled = true;
		this.setSaveLoads(true);
		this.saveConfigurations = false;
	}

	public NFV_MCFPhiSolver(NetworkTopology topology, NFVState state, int timelimit, double alpha, boolean mptcp) {
		this.topology = topology;
		this.services = state.getServices();
		this.NFRequestsMap = state.getRequests();
		this.nodesMap = state.getNodes();
		this.cplexTimeLimit = timelimit;
		this.alpha = alpha;
		this.mptcpenabled = mptcp;
		this.setSaveLoads(true);
		this.saveConfigurations = false;
	}

	public static void main(String[] args) {
		String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
		String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
		String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
		String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration.json";
		String requests = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidos300.csv";

		try {
		//	InputStream inputStream = new FileInputStream(topoFile);
		//	NetGraph netgraph = readGML(inputStream);
		//	NetworkTopology topology = new NetworkTopology(netgraph);
			NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
			NFVState state = new NFVState(servicesFile, requests);
			NFServicesMap services = state.getServices();
			NFNodesMap map = state.getNodes();
			NFRequestsMap req = state.getRequests();
			Arcs arcs = new Arcs();

			double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
			int nodesNumber = map.getNodes().size();
			int arcID = 0;
			for (int i = 0; i < nodesNumber; i++)
				for (int j = 0; j < nodesNumber; j++) {
					if (capacity[i][j] > 0) {
						Arc a = new Arc(arcID, i, j, capacity[i][j]);
						arcID++;
						arcs.add(a);
						topology.getNetGraph().setBandwidth(i,j,capacity[i][j]);
					}
				}

			NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, services, req, map);
			solver.setMptcpenabled(true);
			solver.setSaveLoads(true);
			solver.setRandomSRRequests(false);
			solver.setSaveConfigurations(true);
			solver.setCplexTimeLimit(60);
			OptimizationResultObject obj = solver.optimize();
		//	saveToCSV(obj,arcs,map,"Phi_allAvailable");
		//	OSPFWeights res = new OSPFWeights(topology.getDimension());
		//	ConfigurationSolutionSaver.saveToJSON(obj,arcs,map,"Result",res,0,0);

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

	public OptimizationResultObject optimize() {
		return optimize(this.topology, this.services, this.NFRequestsMap, this.nodesMap, this.alpha);
	}


	/**
	 * @throws IloException
	 */
	public OptimizationResultObject optimize(NetworkTopology topology, NFServicesMap servicesMap, NFRequestsMap req,
											 NFNodesMap nodes, double alphaVal)  {
		double[][] cp = topology.getNetGraph().createGraph().getCapacitie();
		Map<Integer, NFService> serv = servicesMap.getServices();
		Map<Integer, NFRequest> r = req.getRequestMap();
		Map<Integer, NFNode> n = nodes.getNodes();
		OptimizationResultObject res = new OptimizationResultObject(n.size());
		try{
			res = optimize(cp, serv, r, n, alphaVal);
		}
		catch(IloException e){
			e.printStackTrace();
		}

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
											 Map<Integer, NFRequest> requests, Map<Integer, NFNode> nodes, double alphaVal) throws IloException {

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

		if(this.randomSRRequests == true)
		{
			Random random = new Random();

			for(NFRequest request : requests.values())
			{
				List<Integer> requested = request.getServiceList();
				for(int i = 0; i < requested.size(); i++)
				{
					int randomNode = random.nextInt(nodesNumber);

					for(int j = 0; j < nodesNumber; j++)
					{
						if(randomNode != j)
						{
							cplex.addEq(a[request.getId()][j][requested.get(i)],0);
						}
					}
				}
			}
		}

		if(this.mptcpenabled == false)
		{
			HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>> beta = new HashMap<NFRequestSegment,HashMap<Arc, IloIntVar>>();
			System.out.println("Applying Betas");

			for (NFRequest request : requests.values()) {
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
				for (NFRequest request : requests.values()) {
					for (NFRequestSegment s : request.getRequestSegments()) {
						IloLinearNumExpr la = cplex.linearNumExpr();
						la.addTerm(s.getBandwidth(), beta.get(s).get(arc));
						cplex.addEq(s_loads.get(s).get(arc), la, "EQ20_Arc_" + arc.getFromNode() + "_" + arc.getToNode() + "_Req_" + request.getId() + "_Seg_" + s.getFrom() + "_" + s.getTo());
					}
				}
			}
		}

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
			simul.setLoads(loads);
			object.setLinkLoads(u);

			double[] uNodes = new double[nodesNumber];
			double val = 0;
			for (NFNode node : nodes.values()) {
				double utNode = cplex.getValue(r_n.get(node.getId()));
				val += cplex.getValue(gamma_n.get(node.getId()));
				uNodes[node.getId()] = utNode;
			}
			object.setGammaValue(val);

			object.setNodeUtilization(uNodes);
			object.setLoadValue(res);

			this.loads = new NetworkLoads(u, topology);
			HashMap<Integer, Integer> servicesDeployed = new HashMap<>();
			servicesDeployed = getServicesDeployed(this.nodesMap.getNodes());
			object.setServicesDeployed(servicesDeployed);

			double phiValue = simul.congestionMeasure(loads,demands);
			double mlu = this.loads.getMLU();
			System.out.println("MLU" + mlu);
			object.setPhiValue(phiValue);
			System.out.println("PHI: " + phiValue);
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

	public boolean isMptcpenabled() {
		return mptcpenabled;
	}

	public void setMptcpenabled(boolean mptcpenabled) {
		this.mptcpenabled = mptcpenabled;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public boolean isRandomSRRequests() {
		return randomSRRequests;
	}

	public void setRandomSRRequests(boolean randomSRRequests) {
		this.randomSRRequests = randomSRRequests;
	}
}