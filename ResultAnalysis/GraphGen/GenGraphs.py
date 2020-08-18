import matplotlib.pyplot as plt


# Boxplot graph without outliers
def genGraph(data, experienceID):
    fig, ax = plt.subplots()
    ax.set_title(experienceID)
    ax.boxplot(data)
    plt.show()


# Boxplot graph with multiple samples
def genGraphMultiple(data, filename):
    fig, ax = plt.subplots()
    ax.set_title('Multiple Samples with Different sizes')
    ax.boxplot(data)
    plt.show()

def phiVSmlu(dataphi, datamlu, name):
    fig, ax1 = plt.subplots()
    all_data = [dataphi, datamlu]
    labels = ['phi', 'mlu']
    # rectangular box plot
    bplot1 = ax1.boxplot(all_data,
                         vert=True,  # vertical box alignment
                         labels=labels)  # will be used to label x-ticks
    ax1.set_ylabel('Taxa de Utilização (%)')
    ax1.set_xlabel('Modelo')
    ax1.set_title(name)
    plt.show()

