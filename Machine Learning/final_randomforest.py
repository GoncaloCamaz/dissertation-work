import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score


df = pd.read_csv('dataset_merged_treated_2.csv',sep=';', decimal=".")
df.drop(['origin','destination','duration_0','duration_1','duration_3','duration_2','duration','bandwidth'], axis='columns', inplace=True)
df = df[df.S0 == 1]
X = df[df.columns.drop(list(df.filter(regex='^R')))]
y = df.apply(lambda row: row.RN4S0*4+row.RN5S0*5+row.RN10S0*10, axis=1)

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size = 0.20)

classifier = RandomForestClassifier(n_estimators = 1024, criterion = 'entropy',verbose=2)
classifier.fit(X_train, y_train)

y_pred = classifier.predict(X_test)
print(pd.crosstab(y_test, y_pred, rownames=['Actual Nodes'], colnames=['Predicted Nodes']))
print(confusion_matrix(y_test, y_pred))
print(classification_report(y_test, y_pred))