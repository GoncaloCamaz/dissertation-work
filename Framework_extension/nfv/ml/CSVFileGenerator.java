package pt.uminho.algoritmi.netopt.nfv.ml;

import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVFileGenerator
{
    public static void saveToCSV(List<DataSetEntry> entries, int numberOfNodes, int numberOfEdges, int numberOfServices)
    {
        String filename = "trainingDataSet.csv";
        int entrySize = 3+numberOfEdges+numberOfNodes+numberOfServices+numberOfServices;
        try
        {
            FileWriter outputfile = new FileWriter(filename);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            String[] headers = genHeaders(numberOfNodes,numberOfEdges,numberOfServices);
            List<String[]> data = new ArrayList<>();
            data.add(headers);

            for(DataSetEntry entry : entries)
            {
                String[] row = new String[entrySize];
                row[0] = String.valueOf(entry.getOrigin());
                row[1] = String.valueOf(entry.getDestination());
                row[2] = String.valueOf(entry.getBandwidth());
                for(int i = 0; i < numberOfServices; i++)
                {
                    row[i+3] = String.valueOf(entry.getRequests()[i]);
                }
                for(int j = 0; j < numberOfEdges; j++)
                {
                    row[j+3+numberOfServices] = String.valueOf(entry.getLinksState()[j]);
                }
                for(int k = 0; k < numberOfNodes; k++)
                {
                    row[k+3+numberOfServices+numberOfEdges] = String.valueOf(entry.getNodesState()[k]);

                }
                for(int l = 0; l < numberOfServices; l++)
                {
                    if(entry.getProcessmentLocation()[l] == -1)
                    {
                        row[l+3+numberOfServices+numberOfEdges+numberOfNodes] = "NR";
                    }
                    else
                    {
                        row[l+3+numberOfServices+numberOfEdges+numberOfNodes] = String.valueOf(entry.getProcessmentLocation()[l]);

                    }
                }
                data.add(row);
            }

            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String[] genHeaders(int numberOfNodes, int numberOfEdges, int numberOfServices)
    {
        String[] headers = new String[3+numberOfNodes+numberOfEdges+numberOfServices+numberOfServices];
        headers[0] = "origin";
        headers[1] = "destination";
        headers[2] = "bandwidth";

        for(int i = 0; i < numberOfServices; i++)
        {
            headers[i+3] = "S" + i;
        }

        for(int j = 0; j < numberOfEdges; j++)
        {
            headers[j+3+numberOfServices] = "E"+ j;
        }

        for(int k = 0; k < numberOfNodes; k++)
        {
            headers[k+3+numberOfServices+numberOfEdges] = "N"+k;
        }

        for(int l = 0; l < numberOfServices; l++)
        {
            headers[l+3+numberOfServices+numberOfEdges+numberOfNodes] = "RPL" + l;
        }
        return headers;
    }
}
