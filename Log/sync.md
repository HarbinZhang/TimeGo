# How to sync online database from local sql
08/10/2017

2. I want to set the data form in online database as "yyyy-MM-dd&index", which index is the _ID in Sqlite. In this way, we can update only the not synchronized data.

  

1. We can delete the last day record in online database, and update all records after that day(including that day).
