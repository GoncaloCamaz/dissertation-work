package pt.uminho.algoritmi.netopt.nfv.optimization.Tests;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class ResultAnalysis
{
    public static void main(String[] args) throws IOException, ParseException {
        double mlu = 0;
        double phi = 0;

        String filename = "C:\\Users\\gcama\\Desktop\\Tese_final\\pesosIGP\\Abilene\\LC\\mlu\\weights (";
        int i;

        for(i = 1; i <= 15; i++)
        {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader(filename+i+").json"));
            JSONObject jsonObj = (JSONObject) obj;
            mlu += Double.parseDouble(String.valueOf(jsonObj.get("MLU")));
            phi += Double.parseDouble(String.valueOf(jsonObj.get("Congestion")));
        }
        System.out.println(mlu/15);
        System.out.println("PHI: " + phi/15);
    }
}
