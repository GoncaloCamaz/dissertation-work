from CSVReader.Reader import loadData
from GraphGen.GenGraphs import genGraph, phiVSmlu
import numpy


def loadMLUData_Arcs():
    i = 1
    data = []
    while(i <= 7):
        file = "./BTE_1200/MLUA_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadPHIData_Arcs():
    i = 1
    data = []
    while(i <= 6):
        file = "./BTE_1200/PHIA_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadMLUData_Nodes():
    i = 1
    data = []
    while(i <= 7):
        file = "./BTE_1200/MLUN_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadPHIData_Nodes():
    i = 1
    data = []
    while(i <= 6):
        file = "./BTE_1200/PHIN_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def cplexEvaluation_PHI():
    data = []
    file = "./Results/300phiarcs.csv"
    data.append(loadData(file))

    return data


def cplexEvaluation_MLU():
    data = []
    file = "./Results/300mluarcs.csv"
    data.append(loadData(file))

    return data


def run():
    dataphi = loadPHIData_Arcs()
    datamlu = loadMLUData_Arcs()
    phiVSmlu(numpy.concatenate(dataphi), numpy.concatenate(datamlu), "Carga nas Ligações")


run()