from CSVReader.Reader import loadData
from GraphGen.GenGraphs import genGraph, aux

reader1 = loadData("phi_nodes173.csv")
reader2 = loadData("mlu_nodes068.csv")
aux(reader1,reader2)
