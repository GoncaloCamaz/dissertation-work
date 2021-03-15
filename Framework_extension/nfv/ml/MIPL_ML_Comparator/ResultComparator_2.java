package pt.uminho.algoritmi.netopt.nfv.ml.MIPL_ML_Comparator;

import pt.uminho.algoritmi.netopt.nfv.NFVState;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;
import pt.uminho.algoritmi.netopt.nfv.ml.resultAnalysis.MILP_ResultAnalysis;
import pt.uminho.algoritmi.netopt.nfv.optimization.OptimizationResultObject;
import pt.uminho.algoritmi.netopt.ospf.simulation.NetworkTopology;
import java.util.ArrayList;
import java.util.List;

public class ResultComparator_2 {

    private static String inputfile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\trainingDataSet.csv";
    private static String milp_s0 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\actua0.csv";
    private static String milp_s1= "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\actua1.csv";
    private static String milp_s2= "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\actua2.csv";
    private static String ml_s0= "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\predictions0.csv";
    private static String ml_s1="C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\predictions1.csv";
    private static String ml_s2 = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Nova pasta\\predictions2.csv";

    public static void main(String[] args) throws Exception {
        List<DataSetEntry> milps = new ArrayList<>();
        List<DataSetEntry> ml = new ArrayList<>();

        List<DataSetEntry> input = CSV_ResultsLoader.loadInput(inputfile,28,11,3);
        List<DataSetEntry> inputML = CSV_ResultsLoader.loadInput(inputfile,28,11,3);
        milps = CSV_ResultsLoader.loadResults(milp_s0, milp_s1,milp_s2,input);
        ml = CSV_ResultsLoader.loadResults(ml_s0, ml_s1, ml_s2, inputML);

        String nodesFile ="/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abilene.nodes";
        String edgesFile = "/Users/gcama/Desktop/Dissertacao/Work/Framework/topos/abilene/abileneMachineLearning.edges";
        String servicesFile = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\Framework\\NetOpt-master\\frameworkConfiguration_Abilene_WorstCase.json";
        NetworkTopology topology = new NetworkTopology(nodesFile, edgesFile);
        NFVState state = new NFVState(servicesFile);

        double milpPHI = 0;
        double mlPHI = 0;
        double milpGamma = 0;
        double mlGamma = 0;

        double milpObj = 0;
        double mlObj = 0;

        double milpMlu = 0;
        double milpMnu = 0;
        double mlMlu = 0;
        double mlMnu = 0;

        for(DataSetEntry entry : milps)
        {
            MILP_ResultAnalysis resultAnalysis = new MILP_ResultAnalysis(topology,state,entry,10,0.5);
            OptimizationResultObject result = resultAnalysis.solve();


            milpPHI += result.getPhiValue();
            milpGamma += result.getGammaValue();
            milpObj += result.getLoadValue();
            milpMlu += result.getMlu();
            milpMnu += result.getMnu();
        }

        for(DataSetEntry entryML : ml)
        {

            MILP_ResultAnalysis resultAnalysisML = new MILP_ResultAnalysis(topology,state,entryML,10,0.5);
            OptimizationResultObject resultML = resultAnalysisML.solve();
            mlPHI += resultML.getPhiValue();
            mlGamma += resultML.getGammaValue();
            mlObj+= resultML.getLoadValue();
            mlMlu += resultML.getMlu();
            mlMnu+=resultML.getMnu();
        }

        System.out.println("PHI_MILP:" + milpPHI/milps.size());
        System.out.println("Gamma_MILP:" + milpGamma/milps.size());
        System.out.println("MLU_MILP:" + milpMlu/milps.size());
        System.out.println("MNU_MILP:" + milpMnu/milps.size());
        System.out.println("Load:" + milpObj/milps.size());

        System.out.println("PHI_ML:" + mlPHI/ml.size());
        System.out.println("Gamma_ML:" + mlGamma/ml.size());
        System.out.println("MLU_ML:" + mlMlu/ml.size());
        System.out.println("MNU_ML:" + mlMnu/ml.size());
        System.out.println("Load:" + mlObj/ml.size());
    }
}
