package pt.uminho.algoritmi.netopt.nfv.ml.MIPL_ML_Comparator;

import com.opencsv.CSVReader;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSV_ResultsLoader {

    public static List<DataSetEntry> loadInput(String filename, int numberOfEdges, int numberOfNodes, int numberOfServices) throws IOException {
        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVReader csvReader = new CSVReader(reader,';');
        List<String[]> list = csvReader.readAll();
        String[] header = list.get(0);

        List<DataSetEntry> entries = new ArrayList<>();
        list.remove(0);
        for(String[] row : list)
        {
            DataSetEntry entry = new DataSetEntry(numberOfNodes, numberOfEdges,numberOfServices,false);
            entry.setOrigin(Integer.parseInt(row[findIndex(header,"origin")]));
            entry.setDestination(Integer.parseInt(row[findIndex(header, "destination")]));
            entry.setBandwidth(Double.parseDouble(row[findIndex(header, "bandwidth")]));
            entry.setDuration(Integer.parseInt(row[findIndex(header, "duration")]));
            int[] requests = new int[numberOfServices];
            double[] edges = new double[numberOfEdges];
            double[] nodes = new double[numberOfNodes];

            int startingRequests = findIndex(header, "S0");
            for(int s = 0; s < numberOfServices; s++)
            {
                requests[s] = Integer.parseInt(row[s+startingRequests]);
            }
            entry.setRequests(requests);

            int startingEdges = findIndex(header, "E0");
            for(int e = 0; e < numberOfEdges; e++)
            {
                edges[e] = Double.parseDouble(row[startingEdges+e]);
            }
            entry.setLinksState(edges);

            int startingnodes = findIndex(header, "N0");
            for(int n = 0; n < numberOfNodes; n++)
            {
                nodes[n] = Double.parseDouble(row[startingnodes+n]);
            }
            entry.setNodesState(nodes);

            entries.add(entry);
        }

        return entries;
    }

    private static int findIndex(String[] header, String origin) {
        int i = 0;
        int result = -1;
        for(String s : header)
        {
            if(s.equals(origin))
            {
                result = i;
                break;
            }
            i++;
        }

        return result;
    }

    public static List<DataSetEntry> loadResults(String filenameService0, String filenameService1, String filenameService2, List<DataSetEntry> entries) throws IOException {
        Reader readerservice0 = Files.newBufferedReader(Paths.get(filenameService0));
        Reader readerservice1 = Files.newBufferedReader(Paths.get(filenameService1));
        Reader readerservice2 = Files.newBufferedReader(Paths.get(filenameService2));
        CSVReader csvReader0 = new CSVReader(readerservice0,',');
        CSVReader csvReader1 = new CSVReader(readerservice1,',');
        CSVReader csvReader2 = new CSVReader(readerservice2, ',');

        List<String[]> list0 = csvReader0.readAll();
        list0.remove(0);
        List<String[]> list1 = csvReader1.readAll();
        list1.remove(0);
        List<String[]> list2 = csvReader2.readAll();
        list2.remove(0);

        int service0Iterator= 0;
        int service1Iterator= 0;
        int service2Iterator= 0;
        for(DataSetEntry entry : entries)
        {
            int[] nodesprocessment = new int[3];
            for(int i = 0; i < 3; i++)
            {
                nodesprocessment[i] = -1;
            }

            if(entry.getRequests()[0] == 1)
            {
                nodesprocessment[0]= Integer.parseInt(list0.get(service0Iterator)[1]);
                service0Iterator++;
            }
            if(entry.getRequests()[1] == 1)
            {
                nodesprocessment[1]= Integer.parseInt(list1.get(service1Iterator)[1]);
                service1Iterator++;
            }
            if(entry.getRequests()[2] == 1)
            {
                nodesprocessment[2]= Integer.parseInt(list2.get(service2Iterator)[1]);
                service2Iterator++;
            }
            entry.setProcessmentLocation(nodesprocessment);
        }
        return entries;
    }
}
