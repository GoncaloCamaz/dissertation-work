import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
from CSVReader.Reader import loadParetoData


def identify_pareto(scores):
    # Count number of items
    population_size = scores.shape[0]
    # Create a NumPy index for scores on the pareto front (zero indexed)
    population_ids = np.arange(population_size)
    # Create a starting list of items on the Pareto front
    # All items start off as being labelled as on the Parteo front
    pareto_front = np.ones(population_size, dtype=bool)
    # Loop through each item. This will then be compared with all other items
    for i in range(population_size):
        # Loop through all other items
        for j in range(population_size):
            # Check if our 'i' pint is dominated by out 'j' point
            if all(scores[j] >= scores[i]) and any(scores[j] > scores[i]):
                # j dominates i. Label 'i' point as not on Pareto front
                pareto_front[i] = 0
                # Stop further comparisons with 'i' (no more comparisons needed)
                break
    # Return ids of scenarios on pareto front
    return population_ids[pareto_front]

def genPareto(scores):
    pareto = identify_pareto(scores)
    pareto_front = scores[pareto]
    pareto_front_df = pd.DataFrame(pareto_front)
    pareto_front_df.sort_values(0, inplace=True)
    pareto_front = pareto_front_df.values

    x_all = scores[:, 0]
    y_all = scores[:, 1]
    x_pareto = pareto_front[:, 0]
    y_pareto = pareto_front[:, 1]

    plt.scatter(x_all, y_all)
    plt.plot(x_pareto, y_pareto, color='b')
    plt.xlabel('Nível de Congestão')
    plt.ylabel('Número de Serviços')
    plt.show()

def loadScores():
    scores = loadParetoData('../analisePareto3.csv',4,3)
    print(scores)

    #np.array([[277.574328, 56],[277.5826335, 50],[277.5804755, 53],[344.1634928, 30],[287.194332, 25],[277.5896322, 50],
    #                   [277.7066201, 38],[277.622437, 43],[277.5646611, 52],[277.6707621, 38],[277.5677136, 49],
     #                  [444.0526931, 25],[297.7627356, 29],[277.8591921, 27],[396.6996284, 19],[368.9843883, 20]])
    genPareto(np.array(scores))

loadScores()