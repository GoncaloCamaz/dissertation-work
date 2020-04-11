package pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.evaluation;

import jecoli.algorithm.components.evaluationfunction.AbstractMultiobjectiveEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.IEvaluationFunction;
import jecoli.algorithm.components.evaluationfunction.InvalidEvaluationFunctionInputDataException;
import jecoli.algorithm.components.representation.linear.ILinearRepresentation;
import pt.uminho.algoritmi.netopt.cplex.MCFPhiNodeUtilizationSolver;
import pt.uminho.algoritmi.netopt.nfv.NFNodesMap;
import pt.uminho.algoritmi.netopt.nfv.NFRequestsMap;
import pt.uminho.algoritmi.netopt.nfv.NFServicesMap;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.jecoli.SolutionParser;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;

public class NFVEvaluationMO extends AbstractMultiobjectiveEvaluationFunction<ILinearRepresentation<Integer>>
{
    private NetworkTopology topology;
    private NFRequestsMap requestsMap;
    private NFServicesMap servicesMap;
    private String filename;
    private int maxServicesPenalization;


    public NFVEvaluationMO(NetworkTopology topology, NFRequestsMap requests, NFServicesMap services, String filename, int maxServicesPenalization)
    {
        super(false);
        this.topology = topology;
        this.requestsMap = requests;
        this.servicesMap = services;
        this.filename = filename;
        this.maxServicesPenalization = maxServicesPenalization;
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
        return 1;
    }

    @Override
    public Double[] evaluateMO(ILinearRepresentation<Integer> solutionRepresentation) throws Exception
    {
        Double[] resultList = new Double[2];
        NFNodesMap nodes = decode(solutionRepresentation, topology.getDimension());
        MCFPhiNodeUtilizationSolver solver = new MCFPhiNodeUtilizationSolver(topology, servicesMap, requestsMap, nodes);
        OptimizationResultObject object = solver.optimize();

        double penalizationVal = getPenalization(object,this.maxServicesPenalization);

        resultList[0] = new Double(object.getPhiValue()) + penalizationVal;
        resultList[1] = new Double(object.getGammaValue()) + penalizationVal;

        return resultList;
    }

    private double getPenalization(OptimizationResultObject object, int maxServices)
    {
        double ret = 0;

        int servicesDeployed = object.getNumberOfServicesDeployed();
        if(!object.hasSolution())
        {
            ret = Double.MAX_VALUE;
        }
        else
        {
            if(maxServices < servicesDeployed)
            {
                ret = (servicesDeployed-maxServices)*1000;
            }
        }

        return ret;
    }

    public NFNodesMap decode(ILinearRepresentation<Integer> solution, int numberOfNodes)
    {
        NFNodesMap nodes = new NFNodesMap();
        int[] result = new int[numberOfNodes];
        SolutionParser parser = new SolutionParser(this.filename);

        for(int i = 0; i < numberOfNodes; i++)
        {
            result[i] = solution.getElementAt(i);
        }

        nodes = parser.solutionParser(result);

        return nodes;
    }

    public NetworkTopology getTopology() {
        return topology;
    }

    public void setTopology(NetworkTopology topology) {
        this.topology = topology;
    }

    public NFRequestsMap getRequestsMap() {
        return requestsMap;
    }

    public void setRequestsMap(NFRequestsMap requestsMap) {
        this.requestsMap = requestsMap;
    }

    public NFServicesMap getServicesMap() {
        return servicesMap;
    }

    public void setServicesMap(NFServicesMap servicesMap) {
        this.servicesMap = servicesMap;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getMaxServicesPenalization() {
        return maxServicesPenalization;
    }

    public void setMaxServicesPenalization(int maxServicesPenalization) {
        this.maxServicesPenalization = maxServicesPenalization;
    }
}
