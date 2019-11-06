# Hive Project
## Create WDI table using csv archives in gs bucket
The following query will create a persistent table, which is then populated using existing gunzip files on Google Storage, containing our comma-separated files.

In the query we tell Hive how our files are structured, so that it can read, parse, and populate the table. This operation is known as schema-on-read.

```SPARQL
%spark.sql
CREATE EXTERNAL TABLE wdi_gs
(year INTEGER, countryName STRING, countryCode STRING, indicatorName STRING, indicatorCode STRING, indicatorValue FLOAT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'
LOCATION 'gs://mazh-jarvis-bootcamp/datasets/wdi_2016_gz'
TBLPROPERTIES ("skip.header.line.count"="1")
```

## Retrieve the number of records from WDI data (1.5)

```SPARQL
%spark.sql
select count(countryName) from wdi_gs
```

##  Migrate data from google storage to a hive external table (1.6)
In this query, we store our csv data to a distributed file system(HDFS), using `INSERT OVERWRITE TABLE` syntax, which migrates our original table, to the new table stored at a HDFS location.

```SPARQL
%spark.sql
-- DROP TABLE IF EXISTS wdi_csv_text

CREATE EXTERNAL TABLE wdi_csv_text
(year INTEGER, countryName STRING, countryCode STRING, indicatorName STRING, indicatorCode STRING, indicatorValue FLOAT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'
LOCATION 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text'
```

In order to get consistent results, we can clear the cache in each worker node using the following bash command:

```sh
echo 3 | sudo tee /proc/sys/vm/drop_caches
```

## Compare computation time for getting the number of rows in our HDFS using Hive and Bash (1.7)

```sh
%sh
hdfs dfs -get hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text .

# calculate current directory size
cd wdi_csv_text && du -ch .

#create a 'big' file
hdfs dfs -cp  hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text  hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big

#inspect directory structure
hdfs dfs -ls hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big/

#create artifical 'big' data
for i in {26..30}
do
  echo "Copying round: $i"
  hdfs dfs -cp hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big/part-00000-* hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big/000000_0-$i
done

#insepct parent directory of resulted files
hdfs dfs -du -s -h hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big/
```



Now, let's create a Hive table for our "big" data, and compare:

```SPARQL
-- DROP TABLE IF EXISTS wdi_csv_text_big
CREATE EXTERNAL TABLE wdi_csv_text_big
(year INTEGER, countryName STRING, countryCode STRING, indicatorName STRING,
indicatorCode STRING, indicatorValue FLOAT)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' LINES TERMINATED BY '\n'
LOCATION 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big/'

-- query results
SELECT count(countryName) FROM wdi_csv_text_big
```

## Store "big" data locally from hadoop storage

```sh
hdfs dfs -get -p 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_text_big' .
cd wdi_csv_text_big && \
date +%s && cat 000000_0-* | wc -l && date +%s
```

## Parsing Issue

Let’s run the following query and discuss what could be wrong with the indicatorcode column:

```SPARQL
%spark.sql
SELECT distinct(indicatorcode)
FROM wdi_csv_text
order by indicatorcode
limit 20
```

Observe: the indicator code data seem to be incorrect. Since we are seeing misplaced quotation marks, it is probably a parsing issue.

## 1.9 Create a Debug Table

Let’s display the line, and find out why the `indicator_code` column is not shown correctly:

```SPARQL
-- DROP TABLE IF EXISTS wdi_gs_debug
CREATE EXTERNAL TABLE wdi_gs_debug
(line STRING)
LOCATION 'gs://mazh-jarvis-bootcamp/datasets/wdi_2016_gz'

-- query results
SELECT distinct(line)
FROM wdi_gs_debug 
WHERE line LIKE "%(\% of urban %)\"%"
LIMIT 5
```

Observe: `indicator_name` contains delimiter characters and is not being treated as a whole string. Which in turn, causes `indicator_code` to be rendered incorrectly.

## 1.10 Create a table with OpenCSV SerDe

Let’s:

1. create a new Hive table using OpenCSV serializer/deserializer on our `gs` data
2. export the data to a hdfs location
3. verify parsing results
4. compare runtime of our new openCSV table vs the previous table

```SPARQL
CREATE EXTERNAL TABLE wdi_opencsv_gs
(year INTEGER, countryName STRING, countryCode STRING, indicatorName STRING,
indicatorCode STRING, indicatorValue FLOAT)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
WITH SERDEPROPERTIES (
    "separatorChar" = ",",
    "quoteChar" = "\"",
    "escapeChar" = "\\"
    )
LOCATION 'gs://mazh-jarvis-bootcamp/datasets/wdi_2016_gz'

-- display intermediate table retrieved from GS
SELECT DISTINCT(indicatorCode)
FROM wdi_opencsv_gs
ORDER BY indicatorCode
LIMIT 20
```

??

```SPARQL
CREATE EXTERNAL TABLE wdi_opencsv_text
(year INTEGER, countryName STRING, countryCode STRING, indicatorName STRING,
indicatorCode STRING, indicatorValue FLOAT)
ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
LOCATION 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text'

-- copy table
INSERT OVERWRITE TABLE wdi_opencsv_text
SELECT * FROM wdi_opencsv_gs

-- display final table retrieved from HDFS
SELECT DISTINCT(indicatorCode)
FROM wdi_opencsv_text
ORDER BY indicatorCode
LIMIT 20

-- comapre performance
SELECT count(countryName) FROM wdi_csv_text
SELECT count(countryName) FROM wdi_opencsv_text
```

Observe: iteration over the table populated by the regular parser is quicker (as opposed to the table with OpevCSV parser)

## 1.10.1 OpenCSV SerDe Limitation

The downside to OpenCSV’s SerDe is that it handles all columns as strings. Therefore, if our data contains dates, integers, etc; we would need to create a view and cast every column to the appropriate type: ([source](https://cwiki.apache.org/confluence/display/hive/LanguageManual+DDL))

```SPARQL
CREATE VIEW IF NOT EXISTS wdi_csv_text_view
AS
SELECT cast(year as INTEGER),
    countryName,
    countryCode,
    indicatorName,
    indicatorCode,
    cast(indicatorValue as FLOAT)
FROM wdi_opencsv_text
```

## 1.11 2015 Canada GDP Growth HQL

Find 2015 `GDP growth (annual %)` for Canada. Output GDP growth value, country name

```SPARQL
SELECT
    DISTINCT(indicatorCode),
    indicatorName
FROM
    wdi_opencsv_text
WHERE
    indicatorCode LIKE '%GDP%'
```

In the above query, we obtain the indicator code for “GDP growth (annual %)” for use in the next query.

```SPARQL
SELECT
    indicatorValue,
    year,
    indicatorName
FROM
    wdi_opencsv_text
WHERE
    indicatorCode = 'NY.GDP.MKTP.KD.ZG'
    AND year = '2015'
    AND countryName = 'Canada'
```

Observe: the query is slow since the where clause goes through every row to match the indicator code, the year, as well as the country.

## Hive Partitions

In order to optimize the above query, we can use partitions

```SPARQL
-- DROP TABLE IF EXISTS wdi_opencsv_text_partitions

CREATE EXTERNAL TABLE wdi_opencsv_text_partitions (
    countryName STRING, countryCode STRING,
    indicatorName STRING, indicatorCode STRING,
    indicatorValue FLOAT
) PARTITIONED BY (year STRING)
ROW FORMAT DELIMITED
LOCATION 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions'

set hive.exec.dynamic.partition.mode=nonstrict

INSERT OVERWRITE TABLE wdi_opencsv_text_partitions PARTITION (year)
SELECT countryName,
    countryCode,
    indicatorName,
    indicatorCode,
    indicatorValue,
    year
FROM wdi_opencsv_text
```

Observe our newly created Hive partitions on the disk:

```sh
hdfs dfs -ls hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions
```

```sh
Found 59 items
drwxrwxrwt   - zeppelin hadoop          0 2019-09-11 14:20 hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions/year=1960
drwxrwxrwt   - zeppelin hadoop          0 2019-09-11 14:20 hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions/year=1961
drwxrwxrwt   - zeppelin hadoop          0 2019-09-11 14:20 hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions/year=1962
drwxrwxrwt   - zeppelin hadoop          0 2019-09-11 14:20 hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_opencsv_text_partitions/year=1963
...
```

Let’s query from the partitioned table:

```SPARQL
SELECT
    indicatorValue,
    year,
    countryName
FROM
    wdi_opencsv_text_partitions
WHERE
    indicatorCode='NY.GDP.MKTP.KD.ZG'
    AND year = 2015
    AND countryName = 'Canada'
```

## 1.13 Columnar File Optimization

We can optimize Hive queries (select clause) using columnar files

```SPARQL
-- DROP TABLE IF EXISTS wdi_csv_parquet

CREATE EXTERNAL TABLE wdi_csv_parquet
    (year STRING, countryName STRING, countryCode STRING, indicatorName STRING,
    indicatorCode STRING, indicatorValue FLOAT)
STORED AS PARQUET
LOCATION 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_parquet'

-- copy
INSERT OVERWRITE TABLE wdi_csv_parquet
SELECT * FROM wdi_opencsv_gs
```

Observe parquet file:

```sh
hdfs dfs -du -s -h 'hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_parquet'
93.9 M  hdfs:///user/milad-azh-jrvs-ca/hive/wdi/wdi_csv_parquet
```

Observe parquet file performance:

```SPARQL
SELECT count(countryName) FROM wdi_csv_parquet
Took 6 sec. Last updated by anonymous at September 11 2019, 10:33:26 AM. (outdated)
```



Let us do another comparison using a complex query:

```SPARQL
SELECT
    indicatorValue,
    year,
    countryName
FROM wdi_csv_parquet
WHERE indicatorCode = 'NY.GDP.MKTP.KD.ZG'
    AND year='2015'

-- Took 2 sec. 
```

## 1.14 HQL: Highest GDP Growth

Let’s find the highest GDP growth(‘NY.GDP.MKTP.KD.ZG’) year for each country:

```SPARQL
SELECT wcp.indicatorValue AS value,
    wcp.year AS year,
    wcp.countryName as country
FROM (SELECT MAX(indicatorValue) as ind, countryName
    FROM wdi_csv_parquet
    WHERE indicatorCode = 'NY.GDP.MKTP.KD.ZG'
        AND indicatorValue <> 0
    GROUP BY countryName) t1
    INNER JOIN wdi_csv_parquet AS wcp
        ON t1.ind = wcp.indicatorValue
            AND t1.countryName = wcp.countryName
```

## 1.15 Sort GDP by country and year

Let’s write a query that returns GDP Growth for all countries, sorted by country name and year:

```SPARQL
SELECT countryName,
    year,
    indicatorCode,
    indicatorValue
FROM wdi_csv_parquet
WHERE indicatorCode = 'NY.GDP.MKTP.KD.ZG'
DISTRIBUTE BY countryName
SORT BY countryName,
    year
LIMIT 200
```

Observe: we have optimized the query. By using `distribute by` and `sort by`, we are sorting the keys on mapper side, and sending the same key(`countryName`) to the same node.

