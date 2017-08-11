# How to sync online database from local sql


#### 08/11/2017
1. I think no need for time in key, just set _ID from Sqlite as key in online database.

#### 08/10/2017

1. I want to set the key form in online database as "yyyy-MM-dd&index", which index is the _ID in Sqlite. In this way, we can update only the not synchronized data.

  

2. We can delete the last day record in online database, and update all records after that day(including that day).
