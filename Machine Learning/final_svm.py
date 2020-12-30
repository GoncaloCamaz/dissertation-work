import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

df = pd.read_csv('dataset_merged_treated_2.csv',sep=';', decimal=".")
df.drop(['origin','destination','duration_0','duration_1','duration_3','duration'],axis='columns', inpl0ace=True)
df = df[df.S0 == 1]
X = df[df.columns.drop(list(df.filter(regex='^R')))]
y = df.apply(lambda row: row.RN4S0*4+row.RN5S0*5+row.RN10S0*10,axis=1)
from sklearn.model_selection import train_test_split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.20)

from sklearn.svm import SVC
svclassifier = SVC(kernel='linear')
svclassifier.fit(X_train, y_train)
y_pred = svclassifier.predict(X_test)

from sklearn.metrics import classification_report, confusion_matrix
print(confusion_matrix(y_test,y_pred))
print(classification_report(y_test,y_pred))

svclassifier = SVC(kernel='rbf')
svclassifier.fit(X_train, y_train)
y_pred = svclassifier.predict(X_test)
print(confusion_matrix(y_test, y_pred))
print(classification_report(y_test, y_pred))
svclassifier = SVC(kernel='poly', degree=8)
svclassifier.fit(X_train, y_train)
y_pred = svclassifier.predict(X_test)
print(confusion_matrix(y_test, y_pred))
print(classification_report(y_test, y_pred))
