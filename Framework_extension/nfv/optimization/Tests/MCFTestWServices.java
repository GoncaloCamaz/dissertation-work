package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPMLUSolver;
import pt.uminho.algoritmi.netopt.cplex.NFV_MCFPhiSolver;
import pt.uminho.algoritmi.netopt.cplex.utils.Arc;
import pt.uminho.algoritmi.netopt.cplex.utils.Arcs;
import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ConfigurationSolutionSaver;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import pt.uminho.algoritmi.netopt.ospf.simulation.net.NetGraph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static pt.uminho.algoritmi.netopt.ospf.utils.io.GraphReader.readGML;


public class MCFTestWServices
{
    private static String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";// args[0];
    private static String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.edges";//args[1];
    private static String nodesFile1 ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.nodes";// args[0];
    private static String edgesFile1 = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/30_2/isno_30_2.edges";//args[1];
    private static String topoFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/BT Europe/BtEurope.gml";
    private static String servicesFileBT = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_BTEurope.json";
    private static String requestsFileBT_300 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosBTEurope_300.csv";
    private static String requestsFileBT_1200 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosBTEurope_1200.csv";
    private static String servicesFile30 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_WorstCase.json";
    private static String requestsFile30_300 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidos300.csv";
    private static String requestsFile30_1200 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidos1200.csv";
    private static String servicesFileAB = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_Abilene_WorstCase.json";
    private static String requestsFileAB_300 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosAbilene_300.csv";
    private static String requestsFileAB_1200 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosAbilene_1200.csv";
    private static String requestsFileBT_1200A = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\pedidosBT_Altered1200.csv";
    private static boolean enableMPTCP;


    public static void main(String[] args) throws Exception {
        enableMPTCP = false;
    //    runAbilene300();
      //  runAbilene1200();
      //  runBT300();
     //   runBT1200();
     //   run30300();
     //   run301200();

        runSpecialStuff();
    }

    private static void runSpecialStuff() throws IOException {
        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topologyBT = new NetworkTopology(netgraph);
        NFVState state = new NFVState(servicesFileBT, requestsFileBT_1200);
        double[][] capacity = topologyBT.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topologyBT.getDimension(); i++)
            for (int j = 0; j < topologyBT.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver(topologyBT,state,800,0.5,enableMPTCP);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"BT_PHI_1200");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topologyBT,state,800, enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"BT_MLU_1200");

        System.out.println("Congestion PHI BT 1200: " + congestionVal.getPhiValue());
        System.out.println("Congestion Gamma BT 1200: " + congestionVal.getGammaValue());
        System.out.println("Congestion MLU BT 1200: " + mluVal.getMlu());
        System.out.println("Congestion MNU BT 1200: " + mluVal.getMnu());

        enableMPTCP = true;
        NFV_MCFPhiSolver phiSolver2 = new NFV_MCFPhiSolver(topologyBT,state,800,0.5, enableMPTCP);
        OptimizationResultObject congestionVal2 = phiSolver2.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal2,arcsBT,state.getNodes(),"BT_PHI_1200A");

        NFV_MCFPMLUSolver solverMLU2 = new NFV_MCFPMLUSolver(topologyBT,state,800, enableMPTCP);
        OptimizationResultObject mluVal2 = solverMLU2.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal2,arcsBT,state.getNodes(),"BT_MLU_1200A");

        System.out.println("Congestion PHI BT 1200 MPTCP: " + congestionVal2.getPhiValue());
        System.out.println("Congestion Gamma BT 1200 MPTCP: " + congestionVal2.getGammaValue());
        System.out.println("Congestion MLU BT 1200 MPTCP: " + mluVal2.getMlu());
        System.out.println("Congestion MNU BT 1200 MPTCP: " + mluVal2.getMnu());


    }

    private static void run30300() throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile1,edgesFile1);
        NFVState state = new NFVState(servicesFile30, requestsFile30_300);
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topology.getDimension(); i++)
            for (int j = 0; j < topology.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topology,state ,300,0.5,true);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"30_PHI_300");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topology,state,300, true);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"30_MLU_300");

        System.out.println("Congestion PHI 30 300: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU 30 300: " + mluVal.getMlu());
    }

    private static void run301200() throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile1,edgesFile1);
        NFVState state = new NFVState(servicesFile30, requestsFile30_1200);
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topology.getDimension(); i++)
            for (int j = 0; j < topology.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topology,state ,800,0.5, true);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"30_PHI_1200");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topology,state,800,enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"30_MLU_1200");

        System.out.println("Congestion PHI 30 1200: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU 30 1200: " + mluVal.getMlu());
    }

    private static void runAbilene1200() throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFileAB, requestsFileAB_1200);
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topology.getDimension(); i++)
            for (int j = 0; j < topology.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topology,state ,300,0.5,enableMPTCP);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"AB_PHI_1200");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topology,state,300,enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"AB_MLU_1200");

        System.out.println("Congestion PHI AB 1200: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU AB 1200: " + mluVal.getMlu());
    }

    private static void runAbilene300() throws Exception {
        NetworkTopology topology = new NetworkTopology(nodesFile,edgesFile);
        NFVState state = new NFVState(servicesFileAB, requestsFileAB_300);
        double[][] capacity = topology.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topology.getDimension(); i++)
            for (int j = 0; j < topology.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topology,state ,100,0.5, enableMPTCP);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"AB_PHI_300");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topology,state,100, enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"AB_MLU_300");

        System.out.println("Congestion PHI AB 300: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU AB 300: " + mluVal.getMlu());
    }

    public static void runBT300() throws IOException {
        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topologyBT = new NetworkTopology(netgraph);
        NFVState state = new NFVState(servicesFileBT, requestsFileBT_300);
        double[][] capacity = topologyBT.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topologyBT.getDimension(); i++)
            for (int j = 0; j < topologyBT.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topologyBT,state,300,0.5, enableMPTCP);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"BT_PHI_300");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topologyBT,state,300, enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"BT_MLU_300");

        System.out.println("Congestion PHI BT 300: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU BT 300: " + mluVal.getMlu());
    }
    
    public static void runBT1200() throws IOException { 
        InputStream inputStream = new FileInputStream(topoFile);
        NetGraph netgraph = readGML(inputStream);

        NetworkTopology topologyBT = new NetworkTopology(netgraph);
        NFVState state = new NFVState(servicesFileBT, requestsFileBT_1200);
        double[][] capacity = topologyBT.getNetGraph().createGraph().getCapacitie();
        Arcs arcsBT = new Arcs();
        int arcID = 0;
        for (int i = 0; i < topologyBT.getDimension(); i++)
            for (int j = 0; j < topologyBT.getDimension(); j++) {
                if (capacity[i][j] > 0) {
                    Arc a = new Arc(arcID, i, j, capacity[i][j]);
                    arcID++;
                    arcsBT.add(a);
                }
            }
        NFV_MCFPhiSolver phiSolver = new NFV_MCFPhiSolver (topologyBT,state,800,0.5, enableMPTCP);
        OptimizationResultObject congestionVal = phiSolver.optimize();
        ConfigurationSolutionSaver.saveToCSV(congestionVal,arcsBT,state.getNodes(),"BT_PHI_1200");

        NFV_MCFPMLUSolver solverMLU = new NFV_MCFPMLUSolver (topologyBT,state,800, enableMPTCP);
        OptimizationResultObject mluVal = solverMLU.optimize();
        ConfigurationSolutionSaver.saveToCSV(mluVal,arcsBT,state.getNodes(),"BT_MLU_1200");

        System.out.println("Congestion PHI BT 1200: " + congestionVal.getPhiValue());
        System.out.println("Congestion MLU BT 1200: " + mluVal.getMlu());
    }

}
