.. contents::
    :local:
    :backlinks: none
    :depth: 1

Overview
--------

In the case of memory intensive operations, Presto allows offloading
intermediate operator results to disk. The goal of this mechanism is to
enable execution of queries that require amounts of memory exceeding per query
or per node limits.

The mechanism is similar to OS level page swapping. However, it is
implemented on the application level to address specific needs of Presto.

Properties related to spilling are described in properties section
:ref:`tuning-spilling`.


Revocable memory management
---------------------------

By default, Presto manages memory needed by drivers manually to guarantee
fairness in allocating memory to queries and prevent saturation of memory by a
single query (could result in failure of other queries). In the basic case,
if the memory requested by a driver exceeds session properties
``query_max_memory`` or ``query_max_memory_per_node`` the query is killed. This
strategy is efficient when there is a lot of small queries in the cluster, but
leads to killing large queries that don't stay within the limits.

To overcome this inefficiency the concept of revocable memory was introduced.
The driver can request memory that does not count toward the limits, but it The
revoking is implemented as spill to disk. The intermediate data which was stored
in operator's revocable memory is saved to disk for further processing.

Supported operations
------------------------

Details of the revocation handling and returning to processing after being
revoked are operation dependent. Currently, the mechanism is implemented for the
following operations.

Joins
^^^^^

During join operation, memory is used to perform lookups for processed rows of
joined tables. One of the joined tables is stored in memory and is used as
lookup source for rows coming from the other table. The table stored in memory
is called build table.

If local concurrency is used for processing, the build table is partitioned into
a number of partitions. The number of partitions is equal to the value of
``task.concurrency`` configuration parameter.

Having build table partitioned allows using spill-to-disk mechanism to decrease
peak memory usage needed by join operator. When query approaches memory limit, a
subset of partitions of the build table gets spilled to disk. From now on, rows
from the other table that match to spilled build partitions are also spilled.
This influences the amount of disk space needed.

Afterward, the spilled partitions are read one-by-one to finish join operation.

With this mechanism, the peak memory used by the join operator can be decreased
to the size of the largest build table partition. Assuming no data skew, this will
be 1/task.concurrency the size of the whole build table.

Aggregations
^^^^^^^^^^^^

Aggregation functions are computed by cumulating the final results in memory.
Significant amounts of memory may be needed if the number of groups in aggregation
is large. In the case of insufficient memory cumulated aggregation states are
spilled to disk. They are loaded back and merged when memory is available.
