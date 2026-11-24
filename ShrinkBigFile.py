import pandas as pd
df=pd.read_csv("80_store_wait_time.csv",usecols=list(range(5)))
df.to_csv("80_store_wait_time.csv")