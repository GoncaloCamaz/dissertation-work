import csv

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

def loadParetoData(filename, row1, row2):
    with open(filename, newline='') as csvfile:
        reader = csv.reader(csvfile, delimiter=';')
        headers = next(reader, None)
        values = []
        for row in reader:
            values.append([float(row[row1]), float(row[row2])])

        return values
