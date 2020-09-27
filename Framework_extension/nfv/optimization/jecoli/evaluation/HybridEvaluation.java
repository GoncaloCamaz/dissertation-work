package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiSolver;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestConfiguration;
import pt.uminho.algoritmi.netopt.nfv.optimization.NFVRequestsConfigurationMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.ParamsNFV;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.EASolutionParser;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.SRSimulator;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;

import java.util.ArrayList;
import java.util.List;

public class HybridEvaluation extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFVState state;
    private EASolutionParser parser;
    private OptimizationResultObject configurationSolution;
    private int cplexTimeLimit;
    private double alpha;
    private ParamsNFV.EvaluationAlgorithm algorithm;
    private List<Request> requests;

    public HybridEvaluation(NetworkTopology topology, NFVState state, String filename, int cplexTimeLimit, ParamsNFV.EvaluationAlgorithm alg, double alpha)
    {
        super(false);
        this.topology = topology;
        this.state = state;
        this.parser = new EASolutionParser(filename);
        this.cplexTimeLimit = cplexTimeLimit;
        this.algorithm = alg;
        this.alpha = alpha;
        this.requests = new ArrayList<>();
        this.configurationSolution = new OptimizationResultObject(topology.getDimension());
    }


    protected int[] decodeConfiguration(ILinearRepresentation<Integer> solution)
    {
        int size = topology.getDimension();
        int[] result = new int[size];

        for(int i = 0; i < size; i++)
        {
            result[i] = solution.getElementAt(i);
        }

        return result;
    }

    protected int[] decodeWeights(ILinearRepresentation<Integer> solution)
    {
        int initial = topology.getDimension();
        int[] result = new int[topology.getNumberEdges()];
        int j = 0;

        for(int i = initial; i < initial+ topology.getNumberEdges(); i++, j++)
        {
            result[j] = solution.getElementAt(i);
        }

        return result;
    }

    @Override
    public Double[] evaluateMO(ILinearRepresentation<Integer> solutionRepresentation) throws Exception {
        Double[] resultList = new Double[2];

        double resultConfiguration = evaluateConfiguration(solutionRepresentation);

        if(resultConfiguration == Double.MAX_VALUE)
        {
            resultList[0] = Double.MAX_VALUE;
            resultList[1] = Double.MAX_VALUE;
        }
        else
        {
            double resultWeights = evaluateWeights(solutionRepresentation);

            resultList[0] = resultConfiguration;
            resultList[1] = resultWeights;
        }

        return resultList;
    }

    private double evaluateWeights(ILinearRepresentation<Integer> solution) throws Exception {
        double result = 0;
        int nodes = topology.getDimension();

        NFVRequestsConfigurationMap configurationMap = this.configurationSolution.getNfvRequestsConfigurationMap();
        for(NFVRequestConfiguration req : configurationMap.getConfigurations().values())
        {
            this.requests.add(decodeRequests(req));
        }

        int numberOfRequests = requests.size();
        int weights[] = decodeWeights(solution);
        OSPFWeights weightsOSPF = new OSPFWeights(nodes);
        weightsOSPF.setWeights(weights,this.topology);

        SRSimulator simulator = new SRSimulator(topology,weightsOSPF);

        for(int i = 0; i < numberOfRequests ; i++)
        {
            Request r = requests.get(i);
            simulator.addFlow(r.getFlow(), r.getPath());
        }
        result = simulator.getCongestionValue();

        return result;
    }

    private Request decodeRequests(NFVRequestConfiguration req)
    {
        int origin = req.getRequestOrigin();
        NetNode source = new NetNode(origin);
        int destination = req.getRequestDestination();
        NetNode dest = new NetNode(destination);
        double bandwidth = req.getBandwidth();
        List<Integer> srPath = req.genSRPath();
        LabelPath path = new LabelPath(source,dest);

        ArrayList<Segment> segments = new ArrayList<>();
        int old = -1;
        int it = 0;
        for(Integer i : srPath)
        {
            if(i != old)
            {
                if(i != origin && i != destination || i == origin && old != -1 || i == destination && destination != origin && it < srPath.size())
                {
                    Segment s = new Segment(i.toString(), Segment.SegmentType.NODE);
                    s.setDstNodeId(i);
                    if( old == -1)
                    {
                        s.setSrcNodeId(origin);
                    }
                    else
                    {
                        s.setSrcNodeId(old);
                    }
                    segments.add(s);
                }
                old = i;
            }
            it++;
        }

        path.setLabels(segments);
        Flow flow = new Flow(req.getRequestID(), origin, destination, Flow.FlowType.NFV,false, bandwidth);
        Request request = new Request(req.getRequestID(),flow,path);

        return request;
    }

    private double evaluateConfiguration(ILinearRepresentation<Integer> solution)
    {
        NFNodesMap nodes = new NFNodesMap();
        nodes = this.parser.solutionParser(decodeConfiguration(solution));
        this.state.setNodes(nodes);

        OptimizationResultObject object = new OptimizationResultObject(nodes.getNodes().size());
        double penalizationVal = 0;

        if(this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI_MPTCP))
        {
            NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, state,this.cplexTimeLimit, this.alpha, true);
            solver.setSaveConfigurations(true);
            object = solver.optimize();
        }
        else if (this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.MLU_MPTCP))
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, state,this.cplexTimeLimit, true);
            solver.setSaveConfigurations(true);
            object = solver.optimize();
        }
        else if (this.algorithm.equals(ParamsNFV.EvaluationAlgorithm.PHI))
        {
            NFV_MCFPhiSolver solver = new NFV_MCFPhiSolver(topology, state,this.cplexTimeLimit, this.alpha,false);
            solver.setSaveConfigurations(true);
            object = solver.optimize();
        }
        else
        {
            NFV_MCFPMLUSolver solver = new NFV_MCFPMLUSolver(topology, state,this.cplexTimeLimit, false);
            solver.setSaveConfigurations(true);
            object = solver.optimize();
        }

        penalizationVal = checkIfSolutions(object);

        return (penalizationVal > 0 ? penalizationVal : object.getLoadValue());
    }

    private double checkIfSolutions(OptimizationResultObject object)
    {
        double ret = 0;

        if(!object.isAllservicesDeployed() || !object.hasSolution())
        {
            ret = Double.MAX_VALUE;
        }
        else
        {
            this.setConfigurationSolution(object);
        }
        return ret;
    }

    @Override
    public void verifyInputData() throws InvalidEvaluationFunctionInputDataException {

    }

    @Override
    public IEvaluationFunction<ILinearRepresentation<Integer>> deepCopy() throws Exception {
        return null;
    }

    @Override
    public int getNumberOfObjectives() {
        return 2;
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

    public EASolutionParser getParser() {
        return parser;
    }

    public void setParser(EASolutionParser parser) {
        this.parser = parser;
    }

    public OptimizationResultObject getConfigurationSolution() {
        return configurationSolution;
    }

    public void setConfigurationSolution(OptimizationResultObject configurationSolution) {
        this.configurationSolution = configurationSolution;
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

    public ParamsNFV.EvaluationAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(ParamsNFV.EvaluationAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }
}
