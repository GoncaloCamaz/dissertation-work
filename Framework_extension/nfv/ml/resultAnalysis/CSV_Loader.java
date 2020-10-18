package pt.uminho.algoritmi.netopt.nfv.ml.resultAnalysis;

import com.opencsv.CSVReader;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSV_Loader
{
    public static List<DataSetEntry> loadInput(String filename, int numberOfEdges, int numberOfNodes, int numberOfServices) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> list = csvReader.readAll();
        List<DataSetEntry> entries = new ArrayList<>();
        list.remove(0);

        for(String[] row : list)
        {
            DataSetEntry entry = new DataSetEntry(numberOfNodes, numberOfEdges,numberOfServices,false);
            entry.setOrigin(Integer.parseInt(row[1]));
            entry.setDestination(Integer.parseInt(row[2]));
            entry.setBandwidth(Double.parseDouble(row[3]));
            int[] requests = new int[numberOfServices];
            double[] edges = new double[numberOfEdges];
            double[] nodes = new double[numberOfNodes];

            for(int s = 0; s < numberOfServices; s++)
            {
                requests[s] = Integer.parseInt(row[s+4]);
            }
            entry.setRequests(requests);
            for(int e = 0; e < numberOfEdges; e++)
            {
                edges[e] = Double.parseDouble(row[4+numberOfServices+e]);
            }
            entry.setLinksState(edges);
            for(int n = 0; n < numberOfNodes; n++)
            {
                nodes[n] = Double.parseDouble(row[4+numberOfServices+numberOfEdges+n]);
            }
            entry.setNodesState(nodes);

            entries.add(entry);
        }

        return entries;
    }

    public static List<DataSetEntry> loadDTOutput(String filename, int numberOfServices, int numberOfNodes, List<DataSetEntry> input) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> list = csvReader.readAll();
        List<DataSetEntry> entries = new ArrayList<>();
        int entryID = 0;
        list.remove(0);

        for(String[] row : list)
        {
            DataSetEntry entry = input.get(entryID);
            int[] requested = entry.getRequests();
            int[] location = new int[numberOfServices];

            for(int i = 0; i < numberOfServices; i++)
            {
                if(requested[i] == 0)
                {
                    location[i] = -1;
                }
            }
            int servicesCounter = 0;
            int nodesCounter = 0;
            for(int i = 1; i <= numberOfServices*numberOfNodes; i++)
            {
                if(nodesCounter == numberOfNodes)
                {
                    nodesCounter = 0;
                    servicesCounter++;
                }

                if(Integer.parseInt(row[i]) == 1)
                {
                    location[servicesCounter] = nodesCounter;
                }

                nodesCounter++;
            }
            input.get(entryID).setProcessmentLocation(location);
            entryID++;

        }

        return input;
    }

    public static void main(String[] args) throws IOException {
        List<DataSetEntry> input = loadInput("input_DT_100000.csv", 28,11,3);
        loadDTOutput("prediction_DT_100000.csv", 3,11,input);
    }
}
