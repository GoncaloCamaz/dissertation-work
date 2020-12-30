import numpy as np
import tensorflow as tf
from matplotlib import pyplot
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import train_test_split
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
from keras.callbacks import ModelCheckpoint, EarlyStopping
from keras.optimizers import SGD
from sklearn.metrics import accuracy_score, classification_report
from sklearn.metrics import precision_score
from sklearn.metrics import recall_score
from sklearn.metrics import f1_score
from sklearn.metrics import cohen_kappa_score
from sklearn.metrics import roc_auc_score
from sklearn.metrics import classification_report, confusion_matrix


df = pd.read_csv('dataset_merged_treated_2.csv', sep=';', decimal='.')
df.drop(['origin','destination','duration_0','duration_1','duration_3','duration_2','duration','bandwidth'], axis='columns', inplace=True)
df = df[df.S0 == 1]
X = df[df.columns.drop(list(df.filter(regex='^R')))]
#y = df.apply(lambda row: row.RN4S0*4+row.RN5S0*5+row.RN10S0*10, axis=1)
y = df[['RN4S0', 'RN5S0', 'RN10S0']]

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.20, shuffle=False)


model = Sequential()
model.add(Dense(61,  activation='sigmoid'))
model.add(Dropout(0.1))
model.add(Dense(128, activation='sigmoid'))
model.add(Dropout(0.5))
model.add(Dense(128, activation='sigmoid'))
model.add(Dense(3, activation="sigmoid"))
# Compile model
model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])

history = model.fit(X_train, y_train,validation_data=(X_test, y_test),batch_size=20,epochs=100,verbose=1, shuffle=False)

y_pred = model.predict(X_test)

new_y_pred = []
new_y_test = []

y = df.apply(lambda row: row.RN4S0*4+row.RN5S0*5+row.RN10S0*10, axis=1)
X_train2, X_test2, y_train2, y_test2 = train_test_split(X, y, test_size = 0.20, shuffle=False)


tam_pred = len(y_pred)
i = 0
while(i < tam_pred):
    arrayinA = np.asarray(y_pred)[i]
    num = max(arrayinA[0], arrayinA[1], arrayinA[2])
    if(num == arrayinA[0]):
        new_y_pred.append(4)
    elif(num == arrayinA[1]):
        new_y_pred.append(5)
    else:
        new_y_pred.append(10)
    i += 1


print(pd.crosstab(y_test2, np.asarray(new_y_pred), rownames=['Actual Nodes'], colnames=['Predicted Nodes']))
print(confusion_matrix(y_test2, np.asarray(new_y_pred)))
print(classification_report(y_test2, np.asarray(new_y_pred)))