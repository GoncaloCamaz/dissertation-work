import keras
from keras.models import Sequential
from keras.layers import Dense, Dropout, Activation, Flatten
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
    data = pd.read_csv(filename, sep=';', na_values=['NR'], encoding='utf8')
    data = data.sample(frac=1).reset_index(drop=True)  # shuffle the data
    return data


def target_selection(data):
    """ Separates features from target
    :param: Dataframe
    :return: Features pandas.Dataframe
    :return: Target pandas.Series
    """
    data = data[
        ['origin', 'destination', 'bandwidth', 'S0', 'S1', 'S2', 'E0', 'E1', 'E2', 'E3', 'E4', 'E5', 'E6', 'E7', 'E8',
         'E9', 'E10', 'E11', 'E12', 'E13', 'E14', 'E15', 'E16', 'E17', 'E18', 'E19', 'E20', 'E21', 'E22', 'E23', 'E24',
         'E25', 'E26', 'E27', 'N0', 'N1','N3', 'N4','N7', 'N8', 'N9', 'N10', 'RPL0','RPL1','RPL2']]

    features = data[data.columns.difference(
        ['RPL0','RPL1','RPL2'])]

    target = data[['RPL0','RPL1','RPL2']]
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


def training_setup(data,path):
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
    save_dict(d, path + 'features_dict')
    save_dict(d_target, path + 'target_dict')

    (training_inputs, testing_inputs, training_classes, testing_classes) = train_test_split(features,
                                                                                            target,
                                                                                            train_size=0.75,
                                                                                            random_state=1)
    num_classes = 3
    print('Num classes: ', num_classes)
    print('training_input.shape: ', training_inputs.shape)
    print('training_output.shape: ', training_classes.shape)
    print('testing_input.shape: ', testing_inputs.shape)
    print('testing_output.shape: ', testing_classes.shape)
    return training_inputs, testing_inputs, training_classes, testing_classes, features_encoded, target_encoded, num_classes


def createModel():
    units = 36
    timesteps = 10
    input_dim = 45
    inputs = keras.Input(shape=(42))
    x = layers.Dense(64, activation='softmax')(inputs)
    x = layers.Dense(64, activation='relu')(x)
    x = layers.Dense(64, activation='relu')(x)
    outputs = layers.Dense(3, activation='softmax')(x)
    model = keras.Model(inputs, outputs)
    model.summary()

    model.compile(
        loss="mean_squared_error",
        optimizer=keras.optimizers.Adam(lr=0.001),
        metrics=["accuracy"],
    )

    return model


def run():
    data = load_data("C:\\Users\\gcama\\Desktop\\Dissertacao\\Work\\MachineLearning\\Data_Set_2\\DNN\\50000\\DNN_50000_training_EARemNR.csv")
    training_inputs, testing_inputs, training_classes, testing_classes, features_encoded, target_encoded, num_classes = training_setup(
        data, "./")
    model = createModel()
    model.fit(training_inputs, training_classes, epochs=200, batch_size=640)
    score = model.evaluate(testing_inputs, testing_classes, batch_size=640)
    print("Score:")
    print(score)


run()