import java.io.File;
import java.util.Scanner;

public class Launcher
{
    public static void main(String[] args)
    {
        ConfigGen configGen = new ConfigGen();
        JSONLoader loader = new JSONLoader();
        configGen = loader.getConfiguration();
        if(printGreetings())
        {
            loader = new JSONLoader();
            configGen.genRequests(loader.getConfiguration());
        }
        else
        {
            configGen.startConfiguration();
        }
    }

    private static boolean printGreetings()
    {
        boolean ret = false;
        File f = new File("./config.json") ;
        if(f.exists() && !f.isDirectory()) {
            System.out.println("Configuration file detected! Do you wish to load it? [Y/N]\n");
            Scanner scan = new Scanner(System.in);
            String in = scan.next("[a-zA-Z]");
            if(in.equals("y") || in.equals("Y"))
            {
                ret = true;
            }
        }
        return ret;
    }
}
