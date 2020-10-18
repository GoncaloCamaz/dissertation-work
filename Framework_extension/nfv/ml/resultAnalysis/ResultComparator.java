package pt.uminho.algoritmi.netopt.nfv.ml.resultAnalysis;

import ilog.concert.IloException;

import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;

import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.Request;

import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.OSPFWeights;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetNode;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Flow;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.LabelPath;
import pt.uminho.algoritmi.netopt.ospf.simulation.sr.Segment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultComparator
{
    private NetworkTopology topology;
    private NFVState state;
/*
    public ResultComparator(NetworkTopology topology, NFVState state) {
        this.topology = topology;
        this.state = state;
    }

    public List<DataSetEntry> cplexEvaluation(String filename)
    {
        List<DataSetEntry> entries = new ArrayList<>();

        try {
            entries = CSV_Loader.loadInput(filename,28,11,3);
            for(DataSetEntry entry : entries)
            {
                NFV_MCFlowMachineLearning solver = new NFV_MCFlowMachineLearning(this.topology, this.state, entry, 10, 0.5);
                int[] result = solver.solve();
                entry.setProcessmentLocation(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IloException e) {
            e.printStackTrace();
        }

        return entries;
    }

    public List<DataSetEntry> loadMLResults(String filenameinput, String filenameOutput)
    {
        List<DataSetEntry> entries = new ArrayList<>();
        List<DataSetEntry> input = new ArrayList<>();

        try {
            input = CSV_Loader.loadInput(filenameinput, 28,11,3);
            entries = CSV_Loader.loadDTOutput(filenameOutput,3,11,input);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return entries;
    }

    public double[] evaluate(List<DataSetEntry> entries, boolean isMILP) throws Exception {

        double[] result = new double[2];
        double accumulatedPHI = 0;
        double accumulatedMLU = 0;
        int numberOfEntries = 0;

        for(DataSetEntry entry : entries)
        {
            HashMap<Integer, Integer> alphas = new HashMap<>();
            if(!isMILP)
            {
                entry.getProcessmentLocation();
                for(int s = 0; s < 3; s++)
                {
                    alphas.put(s, entry.getProcessmentLocation()[s]);
                }
            }
            double[][]capacities = this.topology.getGraph().getCapacitie();
            Arcs arcs = decodeLinksCapacity(capacities, entry.getLinksState(),11);

            NFV_MCFlowMachineLearning solver = new NFV_MCFlowMachineLearning(topology, state, entry, 20, 0.5);
            double[] res = solver.optimizerResults(arcs, alphas);
            accumulatedPHI += res[0];
            accumulatedMLU += res[1];
            numberOfEntries++;
        }

        result[0] = accumulatedMLU;
        result[1] = accumulatedPHI;
        return result;
    }

    public HashMap<Integer, double[]> evaluation(String filenameINPUT, String filenameOutput) throws Exception {
        OSPFWeights weights = new OSPFWeights(topology.getDimension());
        weights.setRandomWeights(1,20, topology);
        List<DataSetEntry> entriesMILP = cplexEvaluation(filenameINPUT);
        List<DataSetEntry> entriesML = loadMLResults(filenameINPUT, filenameOutput);
        HashMap<Integer, double[]> map = new HashMap<>();

        double[] resultML = new double[2];
        double[] resultMILP = new double[2];

        resultML = evaluate(entriesML, false);
        resultMILP = evaluate(entriesMILP, true);

        map.put(0, resultMILP);
        map.put(1, resultML);
        System.out.println("DONE");

        return map;
    }

    private Request decodeRequests(DataSetEntry req, int id) {
        int origin = req.getOrigin();
        NetNode source = new NetNode(origin);
        int destination = req.getDestination();
        NetNode dest = new NetNode(destination);
        double bandwidth = req.getBandwidth();
        int[] srPath = req.getProcessmentLocation();

        LabelPath path = new LabelPath(source, dest);

        ArrayList<Segment> segments = new ArrayList<>();
        int old = -1;
        int it = 0;
        for (int i = 0; i < srPath.length; i++) {
            if(srPath[i] != -1)
            {
                if (srPath[i] != old) {
                    if (srPath[i] != origin && srPath[i] != destination || srPath[i] == origin && old != -1 || srPath[i] == destination && destination != origin && it < srPath.length) {
                        Segment s = new Segment(String.valueOf(srPath[i]), Segment.SegmentType.NODE);
                        s.setDstNodeId(srPath[i]);
                        if (old == -1) {
                            s.setSrcNodeId(origin);
                        } else {
                            s.setSrcNodeId(old);
                        }
                        segments.add(s);
                    }
                    old = srPath[i];
                }
                it++;
            }
        }

        path.setLabels(segments);
        Flow flow = new Flow(id, origin, destination, Flow.FlowType.NFV,false, bandwidth);
        Request request = new Request(id,flow,path);

        return request;
    }

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
                    double newcapacity = calculateCapacity(100,percentages[arcID]);
                    Arc arc = new Arc(arcID,i,j,newcapacity);
                    arcs.add(arc);
                    arcID++;
                }
            }
        }

        return arcs;
    }

    private double calculateCapacity(double fullcapacity, double percentage)
    {
        double percentageLeft = 1 - percentage;

        return fullcapacity * percentageLeft;
    }

    public static void main(String[] args) throws Exception {
        String input = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\MachineLearning\\Data_Set_3\\conf_1\\results\\input.csv";
        String output = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\MachineLearning\\Data_Set_3\\conf_1\\results\\prediction.csv";
        String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
        String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
        String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
        String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_Abilene.json";
        String requests = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosAbilene_1200.csv";

        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile);
        ResultComparator comparator = new ResultComparator(topology,state);
        HashMap<Integer, double[]> res = comparator.evaluation(input,output);
        System.out.println("Finished");
        System.out.println("MILP: \n");
        System.out.println("MILP - MLU: " + res.get(0)[0] + "\n");
        System.out.println("MILP - PHI: " + res.get(0)[1] + "\n");
        System.out.println("DecisionTrees: \n");
        System.out.println("DT - MLU: " + res.get(1)[0] + "\n");
        System.out.println("DT - PHI: " + res.get(1)[1] + "\n");

    }

 */
}
