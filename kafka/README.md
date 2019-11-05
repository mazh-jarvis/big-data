# Kafka

## Introduction

Kafka is a message queue streaming service that enable us to consume data fed into databases in real-time. Of course, Kafka is more than just that, however, that is the focus on this project.

## Kafka Connect

In this section I'm going to demo how to connect to a Postgres database, and consume real-time data. There is a docker repository for a connect demo, which makes setting up connect much more convenient.

Using the trading-app project, do the following steps:

1. Create a network bridge

   ```sh
   docker network create --driver bridge trading-net
   ```

2. Build and run the Postgres container:

```sh
cd assets/sql
docker build -t jrvs-psql . && \
docker run --rm --name jrvs-psql \
-e POSTGRES_USER=postgres \
-e POSTGRES_PASSWORD=petrop \
-e POSTGRES_DB=jrvstrading \
--network trading-net \
-d -p 5432:5432 jrvs-psql
```

3. Build and run the Trading App container:

```sh
cd ../..
docker build -t jrvs-psql . && \
docker run -d \
--restart   unless-stopped \
-e "IEX_HOST=https://cloud-sse.iexapis.com/stable" \
-e "IEX_PUB_TOKEN=pk_63c7df0a9ea542149b5def0c85d2579b" \
-e "PSQL_URL=jdbc:postgresql://jrvs-psql:5432/jrvstrading" \
-e "PSQL_USER=postgres" \
-e 'PSQL_PASSWORD=petrop' \
--network trading-net \
-p 8080:8080 -t trading-app \
```

Ensure at least one quote and a trader is present in the database. If not create one and give it some funds.

Now let's get started with confluent:

1. Start Confluent

   ```bash
   confluent start
   ```

2. Set up confluent configuration:

   ```properties
   # A simple example that copies all tables from a SQLite database. The first few settings are
   # required for all connectors: a name, the connector class to run, and the maximum number of
   # tasks to create:
   name=trading-postgres-jdbc-serial
   connector.class=io.confluent.connect.jdbc.JdbcSourceConnector
   tasks.max=1
   # The remaining configs are specific to the JDBC source connector. In this example, we connect to a
   # SQLite database stored in the file test.db, use and auto-incrementing column called 'id' to
   # detect new rows as they are added, and output to topics prefixed with 'test-sqlite-jdbc-', e.g.
   # a table called 'users' will be written to the topic 'test-sqlite-jdbc-users'.
   connection.url=jdbc:postgresql://jrvs-trading:5432/jrvstrading
   mode=incrementing
   incrementing.column.name=id
   topic.prefix=trading-postgres-jdbc-
   table.whitelist="security_order"
   ```

3. Connect to the database:

   ```bash
   connect-standalone -daemon /etc/schema-registry/connect-avro-standalone.properties /etc/kafka-connect-jdbc/source-quickstart-sqlite.properties
   ```

4. Check logs for any errors or success messages:

   ```
   cat /logs/connectStandalone.out | egrep -i "error|finished"
   ```

5. Consume

   ```bash
   kafka-avro-console-consumer --new-consumer --bootstrap-server localhost:9092 --topic trading-postgres-jdbc-security_order --from-beginning
   ```

   

## References

Nico. *Kafka Connect - Sqlite in Standalone Mode*:

https://gerardnico.com/dit/kafka/connector/sqlite_standalone#start_confluent

