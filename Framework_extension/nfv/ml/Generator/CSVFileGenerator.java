package pt.uminho.algoritmi.netopt.nfv.ml.Generator;

import com.opencsv.CSVWriter;

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

                int aux1 = 4+numberOfServices+numberOfEdges;
                for(int k = 0; k < numberOfNodes; k++)
                {
                    row[k+aux1] = String.valueOf(entry.getNodesState()[k]);
                }

                if(binaryOutput)
                {
                    int index = 0;
                    int aux2 = 4 + numberOfServices + numberOfEdges + numberOfNodes;
                    for(int l = 0; l < numberOfServices; l++)
                    {
                        for(int n = 0; n < numberOfNodes; n++)
                        {
                            if (entry.getProcessmentLocation()[l] == n) {
                                row[index + aux2] = "1";
                            } else {
                                row[index + aux2] = "0";

                            }
                            index++;
                        }
                    }
                }
                else
                {
                    int aux3 = 4+numberOfServices+numberOfEdges+numberOfNodes;
                    for(int l = 0; l < numberOfServices; l++)
                    {
                        if(entry.getProcessmentLocation()[l] == -1)
                        {
                            row[l+aux3] = "NR";
                        }
                        else
                        {
                            row[l+aux3] = String.valueOf(entry.getProcessmentLocation()[l]);

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

        int aux4 = 4+numberOfServices+numberOfEdges+numberOfNodes;
        if(binaryOutput)
        {
            int index = 0;
            for(int l1 = 0; l1 < numberOfServices; l1++)
            {
                for(int n = 0; n < numberOfNodes; n++)
                {
                    headers[index+aux4] = "RN" + n + "S" + l1;
                    index++;
                }
            }
        }
        else
        {
            for(int l = 0; l < numberOfServices; l++)
            {
                headers[l+aux4] = "RPL" + l;
            }
        }

        return headers;
    }

    public static void genFileHeaders(int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput) throws IOException {
        String filename = "trainingDataSet.csv";

        FileWriter outputfile = new FileWriter(filename);
        CSVWriter writer = new CSVWriter(outputfile, ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        String[] headers = genHeaders(numberOfNodes,numberOfEdges,numberOfServices, binaryOutput);
        List<String[]> data = new ArrayList<>();
        data.add(headers);
        writer.writeAll(data);
        writer.close();
    }

    public static void addEntrytoFile(DataSetEntry entry,int numberOfNodes, int numberOfEdges, int numberOfServices, boolean binaryOutput) throws IOException {
        String filename = "trainingDataSet.csv";
        FileWriter outputfile = new FileWriter(filename, true);
        CSVWriter writer = new CSVWriter(outputfile, ';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
        int entrySize = 4+numberOfEdges+numberOfNodes+numberOfServices+(numberOfServices*numberOfNodes);
        List<String[]> data = new ArrayList<>();
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

        int aux5 = 4 + numberOfServices + numberOfEdges + numberOfNodes;
        if(binaryOutput)
        {
            int index = 0;
            for(int l = 0; l < numberOfServices; l++)
            {
                for(int n = 0; n < numberOfNodes; n++)
                {
                    if (entry.getProcessmentLocation()[l] == n) {
                        row[index + aux5] = "1";
                    } else {
                        row[index + aux5] = "0";

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

        writer.writeAll(data);
        writer.close();
    }
}
