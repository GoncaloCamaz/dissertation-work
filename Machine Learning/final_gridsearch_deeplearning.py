import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split, GridSearchCV
from tensorflow import keras
from tensorflow.keras import layers
import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten, Embedding, LSTM
from keras.optimizers import SGD
import numpy as np
import pandas as pd
import pickle
import joblib
from timeit import default_timer as timer
from sklearn import preprocessing
from collections import defaultdict
from sklearn.model_selection import train_test_split
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.python.keras.wrappers.scikit_learn import KerasClassifier


def save_model(model, filename):
    """ Save classification model in file
    :param: Model to save
    :param: Path
    """
    pickle.dump(model, open(filename, 'wb'))


def save_dict(dict_encoder, filename):
    """ Save dictionary of encoded data
    :param: Dictionary
    :param: Path
    """
    joblib.dump(dict_encoder, filename)


def load_data(filename):
    """ Get dataframe from csv file
    :param: Path to csv file
    :return: Pandas dataframe of the csv data
    """
    data = pd.read_csv(filename, sep=';', na_values=['nan'], encoding='utf8')
    data = data[data.S0 == 1]
    # data = data.sample(frac=1).reset_index(drop=True)  # shuffle the data
    return data


def target_selection(data):
    """ Separates features from target
    :param: Dataframe
    :return: Features pandas.Dataframe
    :return: Target pandas.Series
    """
    data = data[
        ['origin', 'destination', 'bandwidth', 'duration', 'S0','S1','S2', 'E0', 'E1', 'E2', 'E3', 'E4', 'E5', 'E6', 'E7', 'E8',
         'E9', 'E10', 'E11', 'E12', 'E13', 'E14', 'E15', 'E16', 'E17', 'E18', 'E19', 'E20', 'E21', 'E22', 'E23', 'E24',
         'E25', 'E26', 'E27','N1','N4', 'N5','N9','N10','RN4S0', 'RN5S0', 'RN10S0']]

    features = data[data.columns.difference(
        ['RN4S0', 'RN5S0', 'RN10S0'])]

    target = data[['RN4S0', 'RN5S0', 'RN10S0']]
    return features, target


def data_discretization(data_to_encode, multilplecolumns=True):
    """ Discretization of dataframe or series using preprocessing.LabelEncoder
    :param: Fataframe or series to encode
    :param: Bolean to determinate if data is dataframe or series
    :return: Encoded dataframe or series
    :return: Dictionary needed to reverse encoding, or encode new data
    """
    d = defaultdict(preprocessing.LabelEncoder)
    if multilplecolumns:
        encoded = data_to_encode.apply(lambda x: d[x.name].fit_transform(x))
    else:
        encoded = d['target'].fit_transform(data_to_encode)
    return encoded, d


def training_setup(data, path):
    """ Setup training and testing data, including discretization , also saves dictionarys of discretization
    :param: Fataframe to setup
    :return: Features data for training
    :return: Features data for testing
    :return: Target data for training
    :return: Target data for testing
    :return: Features encoded
    :return: Target encoded
    :return: Number of different classes in target
    """
    features, target = target_selection(data)
    features_encoded, d = data_discretization(features, True)
    target_encoded, d_target = data_discretization(target, True)

    (training_inputs, testing_inputs, training_classes, testing_classes) = train_test_split(features,
                                                                                            target,
                                                                                            train_size=0.85,
                                                                                            random_state=1,
                                                                                            shuffle=False)
    num_classes = 40
    print('Num classes: ', num_classes)
    print('training_input.shape: ', training_inputs.shape)
    print('training_output.shape: ', training_classes.shape)
    print('testing_input.shape: ', testing_inputs.shape)
    print('testing_output.shape: ', testing_classes.shape)
    return training_inputs, testing_inputs, training_classes, testing_classes, features_encoded, target_encoded, num_classes


def grid_search(X, y, nfolds, param_grid, model):
    """ Auxiliary method to implement grid search algoritm for hyperparameter tuning
    :param: Features
    :param: Target
    :param: Nfolds for cross validation
    :param: Grid of hyperparameters to test
    :param: Model to find best hyperparameters
    :return: Best hyperparameters
    """
    grid_search = GridSearchCV(model, param_grid, cv=nfolds, verbose=2, n_jobs=2)
    grid_search.fit(X, y)
    grid_search.best_params_
    return grid_search.best_params_


def create_model(n_hidden, size_nodo, ativ, opt, dropout):
    """ Neural network model setup method
    :param: Number of hidden layers
    :param: Number of nodos on each layer
    :param: Activation function
    :param: Optimizer
    :param: Dropout chance (0 - 1)
    :return: Neural network model
    """
    num_classes = 3
    model = Sequential()
    model.add(Dense(61, activation='sigmoid'))
    for n in range(n_hidden) :
            model.add(Dropout(dropout))
            model.add(Dense(size_nodo, activation=ativ))

    model.add(Dense(3, activation="softmax"))
    # Compile model
    model.compile(loss='categorical_crossentropy', optimizer=opt, metrics=['accuracy'])
    return model

def neural_network_hyper(features_encoded, target_encoded):
    """ Hyperparameter tuning of Neural network
    :param: Features data
    :param: Target data
    :return: Neural network model with best parameters
    """
    grid_param = {
        'n_hidden': [2,4],
        'size_nodo': [128,256],
        'ativ': ['sigmoid','softmax','relu'],
        'opt': ['adam'],
        'dropout': [0.2,0.5],
        'epochs': [100],
        'batch_size': [48]
    }
    model = KerasClassifier(build_fn=create_model, verbose=1, validation_split=0.2)
    ann_hyper_parameters = grid_search(features_encoded, target_encoded, 2, grid_param, model)
    print('\n\n\nBest Neural Network Hyper-parameters using GridSearch:\n', ann_hyper_parameters)

    estimator = KerasClassifier(build_fn=create_model,
                                n_hidden=ann_hyper_parameters['n_hidden'],
                                size_nodo=ann_hyper_parameters['size_nodo'],
                                ativ=ann_hyper_parameters['ativ'],
                                opt=ann_hyper_parameters['opt'],
                                dropout=ann_hyper_parameters['dropout'],
                                epochs= ann_hyper_parameters['epochs'],
                                batch_size=ann_hyper_parameters['batch_size'],
                                validation_split=0.1,
                                verbose=1)
    return estimator


def select_best_model(models):
    """ Selection best model based on score
    :param: Models dictionary
    :return: Best model
    :return: Accuracy score of model
    """
    best_model = "none"
    max_score = 0
    for model in models:
        if models[model] > max_score:
            max_score = models[model]
            best_model = model
    return best_model, max_score


def models_training_and_evaluating(training_inputs, testing_inputs, training_classes, testing_classes, models):
    """ Train and evaluate all models
    :param: Features data for training
    :param: Features data for testing
    :param: Target data for training
    :param: Target data for testing
    :param: Models dictionary
    :return: Models dictionary with scores updated for each model
    """
    print("Number of models: " + str(len(models)))
    for model in models:
        model.fit(training_inputs, training_classes)
        score = model.score(testing_inputs, testing_classes)
        models[model] = score
        print("=>>>>>>")
        print(model)
        print("Score : " + str(score))
    return models


def saveResultsCSV(best_model, training):
    row = training
    pd.DataFrame(training,
                 columns=['origin', 'destination', 'bandwidth', 'duration', 'S0', 'E0', 'E1', 'E2', 'E3', 'E4', 'E5',
                          'E6', 'E7', 'E8',
                          'E9', 'E10', 'E11', 'E12', 'E13', 'E14', 'E15', 'E16', 'E17', 'E18', 'E19', 'E20', 'E21',
                          'E22', 'E23', 'E24',
                          'E25', 'E26', 'E27', 'N4', 'N5', 'N10']).to_csv('inputs.csv')

    output = best_model.predict(row, check_input=True)
    pd.DataFrame(output, columns=['RN4S0','RN5S0','RN10S0']).to_csv('prediction.csv')


def run():

    df = pd.read_csv('dataset_merged_treated_2.csv', sep=';', decimal='.')
    df.drop(['origin', 'destination', 'duration_0', 'duration_1', 'duration_3', 'duration_2', 'duration', 'bandwidth'],
            axis='columns', inplace=True)
    df = df[df.S0 == 1]
    X = df[df.columns.drop(list(df.filter(regex='^R')))]
    # y = df.apply(lambda row: row.RN4S0*4+row.RN5S0*5+row.RN10S0*10, axis=1)
    y = df[['RN4S0', 'RN5S0', 'RN10S0']]

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, shuffle=False)

    models = {
        neural_network_hyper(X_train, y_train): 0
    }

    models = models_training_and_evaluating(X_train, X_test, y_train, y_test, models)
    best_model, max_score = select_best_model(models)

    if (best_model.get_params()):
        print("\n\nParams : " + str(best_model.get_params()))
    print("Score : " + str(max_score))


run()