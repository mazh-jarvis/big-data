# Spark

## Concepts

### Difference between narrow and wide operation

Narrow operations occur in a single node, whereas wide operations occur in multiple ones, therefore more expensive.

### Spark shuffling

Shuffling event is distribution of RDD transformation across different nodes(partitions). Shuffling occurs when distinct keys exists in different partitions.

### Spark vs MapReduce performance

Spark is faster due to the fact that it stores data in memory, whereas MR always stores intermediate data on disk (memory was expensive back in the day). Also Spark RDD is optimized data structure for distributed data processing.

### Shared variables

A workaround for closures, which should be avoided in cluster mode if the closure is used after the scope(leads to unpredictable results).

* Broadcast variables
* Accumulator 