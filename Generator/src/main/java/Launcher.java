import java.util.Scanner;

public class Launcher
{
    public static void main(String[] args)
    {
        ConfigGen configGen = new ConfigGen();
        if(printGreetings())
        {
            //TODO LOAD FILE
        }
        else
        {
            configGen.startConfiguration();
        }
    }

    private static boolean printGreetings()
    {
        System.out.println("Hello! I'm the traffic demands generator!\n");
        System.out.println("Configuration file detected! Do you wish to load it? [Y/N]\n");
        Scanner scan = new Scanner(System.in);
        String in = scan.next("[a-zA-Z]");
        if(in.equals("y") || in.equals("Y"))
        {
            return true;
        }
        else return false;
    }
}
