from CSVReader.Reader import loadData
from GraphGen.GenGraphs import genGraph, phiVSmlu
import numpy


def loadMLUData_Arcs(folder,number):
    i = 1
    data = []
    while(i <= number):
        file = "./"+folder+"/MLUA_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadPHIData_Arcs(folder, number):
    i = 1
    data = []
    while(i <= number):
        file = "./"+folder+"/PHIA_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadMLUData_Nodes(folder,number):
    i = 1
    data = []
    while(i <= number):
        file = "./"+folder+"/MLUN_"+str(i)+".csv"
        data.append(loadData(file))
        i += 1
    return data


def loadPHIData_Nodes(folder,number):
    i = 1
    data = []
    while(i <= number):
        file = "./"+folder+"/PHIN_"+str(i)+".csv"
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
    dataphi = loadPHIData_Nodes("ResultsBT1200",12)
    datamlu = loadMLUData_Nodes("ResultsBT1200",12)
    phiVSmlu(numpy.concatenate(dataphi), numpy.concatenate(datamlu), "Carga nos Nodos - Topologia BT Europe - 1200 Pedidos")


run()