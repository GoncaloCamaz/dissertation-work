import csv
from GraphGen.GenGraphs import aux


def loadData(filename):
    with open(filename, newline='') as csvfile:
        reader = csv.reader(csvfile, delimiter=';')
        headers = next(reader, None)
        values = []
        if len(headers) == 3:
            for row in reader:
                values.append(float(row[2]))
        else:
            for row in reader:
                if(row[1] == 'NaN'):
                    values.append(0)
                else:
                    values.append(float(row[1]))
        return values
