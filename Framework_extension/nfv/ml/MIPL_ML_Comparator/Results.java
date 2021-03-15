package pt.uminho.algoritmi.netopt.nfv.ml.MIPL_ML_Comparator;

import pt.uminho.algoritmi.netopt.nfv.ml.Generator.DataSetEntry;

import java.util.List;

public class Results
{
    private List<DataSetEntry> milpentries;
    private List<DataSetEntry> mlentries;

    public Results(List<DataSetEntry> milpentries, List<DataSetEntry> mlentries) {
        this.milpentries = milpentries;
        this.mlentries = mlentries;
    }

    public List<DataSetEntry> getMilpentries() {
        return milpentries;
    }

    public void setMilpentries(List<DataSetEntry> milpentries) {
        this.milpentries = milpentries;
    }

    public List<DataSetEntry> getMlentries() {
        return mlentries;
    }

    public void setMlentries(List<DataSetEntry> mlentries) {
        this.mlentries = mlentries;
    }
}
