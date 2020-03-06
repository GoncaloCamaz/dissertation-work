import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.opencsv.*;

public class ConfigGen
{
    private static String filepath = "./pedidos.csv";
    private List<String> headers;
    private ArrayList<Configuration> configs;
    private RandomNumGen random;
    private Helper helpers;
    private boolean allNodesWServices;
    private int maxNodesWServices;
    private int maxNodes;
    private int requestnumber;

    public ConfigGen()
    {
        this.headers = new ArrayList<>();
        this.random = new RandomNumGen();
        this.helpers = new Helper();
        this.allNodesWServices = false;
        this.maxNodesWServices = 0;
        this.maxNodes = 0;
        this.requestnumber = 0;
        this.configs = new ArrayList<>();
    }

    public void startConfiguration()
    {
        Scanner scan = new Scanner(System.in);
        System.out.println("Do you pretend to have all the nodes with NFV services? [Y/N]\n");
        String input = scan.next("[a-zA-Z]");
        if(input.equals("y") || input.equals("Y"))
        {
            this.allNodesWServices = true;
        }
        else
        {
            System.out.println("Insert the maximum number of nodes with services:\n");
            this.maxNodesWServices = scan.nextInt();
        }
        System.out.println("Please insert the number of nodes of the topology:\n");
        this.maxNodes = scan.nextInt();
        if(this.allNodesWServices == true)
        {
            this.maxNodesWServices = this.maxNodes;
        }
        System.out.println("Please insert the number of requests to generate:\n");
        this.requestnumber = scan.nextInt();
        generateSolutions();
    }

    public void generateSolutions() {
        File file = new File(filepath);
        try
        {
            FileWriter outputfile = new FileWriter(file);
            CSVWriter writer = new CSVWriter(outputfile, ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.DEFAULT_LINE_END);

            List<String[]> data = new ArrayList<String[]>();
            data = genData();
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String[]> genData()
    {
        List<String[]> data = new ArrayList<>();
        List<Integer> nodesWServices = new ArrayList<>();
        if(this.allNodesWServices)
        {
            for(int i = 0; i < maxNodes; i++)
            {
                nodesWServices.add(i+1);
            }
        }
        else
        {
            nodesWServices = random.genNodesWServices(this.maxNodes,this.maxNodesWServices);
        }
        data.add(genHeaders(nodesWServices, this.maxNodesWServices));

        int rows = 0;
        while(rows < requestnumber)
        {
            String[] row = genRow(rows);
            data.add(row);
            rows++;
        }
        return data;
    }

    private String[] genRow(int row)
    {
        Configuration c = new Configuration();
        c.setId(row + 1);
        c.setOriginNodeID(random.randomInt(maxNodes));
        c.setBandwidthConsumption(random.randomInt(1000));
        if(allNodesWServices)
        {
            c.insertAllOne(maxNodes);
        }
        else
        {
            c.insertRandomServices(maxNodes,1+random.randomInt(maxNodesWServices));
        }
        return helpers.convertListToString(c, this.headers.size());
    }

    private String[] genHeaders(List<Integer> nodesWithServices, int maxNodesWServices)
    {
        this.headers.add("id");
        this.headers.add("nodeID");
        this.headers.add("bandwidth");
        int i = 0;
        while(i < maxNodesWServices)
        {
            this.headers.add(String.valueOf(nodesWithServices.get(i)));
            i++;
        }
        return helpers.genHeaders(this.headers);
    }
}
