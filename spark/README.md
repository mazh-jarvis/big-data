# Spark

## Concepts

### Difference between narrow and wide operation

Narrow operations occur in a single node, whereas wide operations occur in multiple ones, therefore more expensive.

### Spark shuffling

Shuffling is distribution of RDD transformation across different nodes. Shuffling occurs when distinct keys exists in different partitions.

### Spark vs MapReduce performance

Spark is faster due to the fact that it stores data in memory, whereas MR always stores intermediate data on disk (memory was expensive back in the day).