package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import org.json.simple.parser.ParseException;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ResultAnalystParser;
import pt.uminho.algoritmi.netopt.nfv.optimization.Utils.ResultAnalystParserObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParetoFrontCSVFileGenerator
{
    public static void main(String[] args) throws IOException, ParseException {
        int i;
        String filename = "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Pareto_2\\Abilene\\Analysis\\PHI_";
        int max = 72;
        List<ResultAnalystParserObject> list = new ArrayList<>();
        for(i=1; i <= max;i++)
        {
            String filenameAux = filename +  i + ".json";
            list.add(ResultAnalystParser.loadResults(filenameAux));
        }
        ResultAnalystParser.saveToCSV(list, "C:\\Users\\gcama\\Desktop\\Dissertacao\\Resultados\\Pareto_2\\Abilene\\Analysis\\PHIResults.csv");
    }
}
