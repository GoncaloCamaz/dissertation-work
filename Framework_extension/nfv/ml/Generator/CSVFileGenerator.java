package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import com.opencsv.CSVWriter;
import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVFileGenerator
{
    public static void saveToCSV(List<DataSetEntry> entries, int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput)
    {
        String filename = "trainingDataSet.csv";
        int entrySize = 4+numberOfEdges+numberOfNodes+numberOfServices+(numberOfServices*numberOfNodes);

        try
        {
            FileWriter outputfile = new FileWriter(filename);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            String[] headers = genHeaders(numberOfNodes,numberOfEdges,numberOfServices, binaryOutput);
            List<String[]> data = new ArrayList<>();
            data.add(headers);

            for(DataSetEntry entry : entries)
            {
                String[] row = new String[entrySize];
                row[0] = String.valueOf(entry.getOrigin());
                row[1] = String.valueOf(entry.getDestination());
                row[2] = String.valueOf(entry.getBandwidth());
                row[3] = String.valueOf(entry.getDuration());
                for(int i = 0; i < numberOfServices; i++)
                {
                    row[i+4] = String.valueOf(entry.getRequests()[i]);
                }
                for(int j = 0; j < numberOfEdges; j++)
                {
                    row[j+4+numberOfServices] = String.valueOf(entry.getLinksState()[j]);
                }
                for(int k = 0; k < numberOfNodes; k++)
                {
                    row[k+4+numberOfServices+numberOfEdges] = String.valueOf(entry.getNodesState()[k]);

                }

                if(binaryOutput)
                {
                    int index = 0;
                    for(int l = 0; l < numberOfServices; l++)
                    {
                        for(int n = 0; n < numberOfNodes; n++)
                        {
                            if (entry.getProcessmentLocation()[l] == n) {
                                row[index + 4 + numberOfServices + numberOfEdges + numberOfNodes] = "1";
                            } else {
                                row[index + 4 + numberOfServices + numberOfEdges + numberOfNodes] = "0";

                            }
                            index++;
                        }
                    }
                }
                else
                {
                    for(int l = 0; l < numberOfServices; l++)
                    {
                        if(entry.getProcessmentLocation()[l] == -1)
                        {
                            row[l+4+numberOfServices+numberOfEdges+numberOfNodes] = "NR";
                        }
                        else
                        {
                            row[l+4+numberOfServices+numberOfEdges+numberOfNodes] = String.valueOf(entry.getProcessmentLocation()[l]);

                        }
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

    private static String[] genHeaders(int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput)
    {
        String[] headers = new String[4+numberOfNodes+numberOfEdges+numberOfServices+(numberOfServices*numberOfNodes)];
        headers[0] = "origin";
        headers[1] = "destination";
        headers[2] = "bandwidth";
        headers[3] = "duration";

        for(int i = 0; i < numberOfServices; i++)
        {
            headers[i+4] = "S" + i;
        }

        for(int j = 0; j < numberOfEdges; j++)
        {
            headers[j+4+numberOfServices] = "E"+ j;
        }

        for(int k = 0; k < numberOfNodes; k++)
        {
            headers[k+4+numberOfServices+numberOfEdges] = "N"+k;
        }

        if(binaryOutput)
        {
            int index = 0;
            for(int l1 = 0; l1 < numberOfServices; l1++)
            {
                for(int n = 0; n < numberOfNodes; n++)
                {
                    headers[index+4+numberOfServices+numberOfEdges+numberOfNodes] = "RN" + n + "S" + l1;
                    index++;
                }
            }
        }
        else
        {
            for(int l = 0; l < numberOfServices; l++)
            {
                headers[l+4+numberOfServices+numberOfEdges+numberOfNodes] = "RPL" + l;
            }
        }



        return headers;
    }
}
