import pandas as pd

dictip = pd.merge( "A.csv","D.csv","H.csv","M.csv","P.csv","S.csv","W.csv" on='key', how='inner')
print(dictip)