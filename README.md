# *TreeSet

This repo contains pure-Kotlin implementations of various ordered sets with limited set of supported operations
(see [`SortedSet.kt`](common/src/SortedSet.kt) interface) and compares them 
with Java `TreeSet` implementation on simple benchmarks.
  
* `JTreeSet` &mdash; baseline implementation of JVM `TreeSet` which uses red-black tree.
* `BTreeSet` &mdash; B-Tree algorithm. It is memory-efficient with very low allocation
  pressure and very few objects allocation, yet not the fastest one.
* `WTreeSet` &mdash; WAVL tree algorithm. See "Rank-Balanced Trees" by BERNHARD HAEUPLER, SIDDHARTHA SEN, ROBERT E. TARJAN.

## Results

Environment

```text
Windows 11, version 22H2
AMD Ryzen 9 5900X 12-Core Processor
JDK 17.0.6, Java HotSpot(TM) 64-Bit Server VM, 17.0.6+9-LTS-190
```

### Contains

```text
Benchmark                   (n)  Mode  Cnt      Score     Error  Units

BTreeSetBenchmark.contains    1  avgt    8      4.220 ±   0.007  ns/op
BTreeSetBenchmark.contains    2  avgt    8      7.249 ±   0.038  ns/op
BTreeSetBenchmark.contains    3  avgt    8     12.014 ±   0.138  ns/op
BTreeSetBenchmark.contains    4  avgt    8     16.127 ±   0.176  ns/op
BTreeSetBenchmark.contains    6  avgt    8     23.680 ±   0.211  ns/op
BTreeSetBenchmark.contains    8  avgt    8     51.515 ±   0.268  ns/op
BTreeSetBenchmark.contains   12  avgt    8     76.460 ±   0.414  ns/op
BTreeSetBenchmark.contains   16  avgt    8    103.095 ±   0.604  ns/op
BTreeSetBenchmark.contains   24  avgt    8    166.561 ±   0.425  ns/op
BTreeSetBenchmark.contains   32  avgt    8    240.446 ±   1.245  ns/op
BTreeSetBenchmark.contains   48  avgt    8    382.536 ±   2.607  ns/op
BTreeSetBenchmark.contains   64  avgt    8    688.471 ±   1.650  ns/op
BTreeSetBenchmark.contains   96  avgt    8   1095.722 ±   6.728  ns/op
BTreeSetBenchmark.contains  128  avgt    8   1503.633 ±   8.627  ns/op
BTreeSetBenchmark.contains  192  avgt    8   2328.804 ±  11.368  ns/op
BTreeSetBenchmark.contains  256  avgt    8   3365.289 ±  24.930  ns/op

JTreeSetBenchmark.contains    1  avgt    8      2.770 ±   0.013  ns/op
JTreeSetBenchmark.contains    2  avgt    8      5.939 ±   0.367  ns/op
JTreeSetBenchmark.contains    3  avgt    8      7.682 ±   0.056  ns/op
JTreeSetBenchmark.contains    4  avgt    8     11.014 ±   0.220  ns/op
JTreeSetBenchmark.contains    6  avgt    8     17.987 ±   0.263  ns/op
JTreeSetBenchmark.contains    8  avgt    8     24.243 ±   0.070  ns/op
JTreeSetBenchmark.contains   12  avgt    8     37.873 ±   0.138  ns/op
JTreeSetBenchmark.contains   16  avgt    8     53.102 ±   0.394  ns/op
JTreeSetBenchmark.contains   24  avgt    8     90.489 ±   0.221  ns/op
JTreeSetBenchmark.contains   32  avgt    8    129.494 ±   1.016  ns/op
JTreeSetBenchmark.contains   48  avgt    8    212.232 ±   0.311  ns/op
JTreeSetBenchmark.contains   64  avgt    8    290.520 ±   1.751  ns/op
JTreeSetBenchmark.contains   96  avgt    8    494.778 ±   4.724  ns/op
JTreeSetBenchmark.contains  128  avgt    8    727.076 ±   4.133  ns/op
JTreeSetBenchmark.contains  192  avgt    8   1228.497 ±   4.802  ns/op
JTreeSetBenchmark.contains  256  avgt    8   1790.707 ±  11.006  ns/op

WTreeSetBenchmark.contains    1  avgt    8      2.546 ±   0.005  ns/op
WTreeSetBenchmark.contains    2  avgt    8      4.857 ±   0.118  ns/op
WTreeSetBenchmark.contains    3  avgt    8      8.522 ±   0.203  ns/op
WTreeSetBenchmark.contains    4  avgt    8     10.197 ±   0.018  ns/op
WTreeSetBenchmark.contains    6  avgt    8     16.192 ±   0.131  ns/op
WTreeSetBenchmark.contains    8  avgt    8     24.969 ±   0.213  ns/op
WTreeSetBenchmark.contains   12  avgt    8     40.321 ±   0.103  ns/op
WTreeSetBenchmark.contains   16  avgt    8     53.256 ±   0.711  ns/op
WTreeSetBenchmark.contains   24  avgt    8     88.154 ±   0.511  ns/op
WTreeSetBenchmark.contains   32  avgt    8    127.467 ±   0.991  ns/op
WTreeSetBenchmark.contains   48  avgt    8    209.999 ±   2.194  ns/op
WTreeSetBenchmark.contains   64  avgt    8    295.745 ±   1.406  ns/op
WTreeSetBenchmark.contains   96  avgt    8    536.246 ±   1.984  ns/op
WTreeSetBenchmark.contains  128  avgt    8    766.276 ±   2.504  ns/op
WTreeSetBenchmark.contains  192  avgt    8   1241.485 ±   1.956  ns/op
WTreeSetBenchmark.contains  256  avgt    8   1785.673 ±  20.199  ns/op
```

### Add

```text
Benchmark                   (n)  Mode  Cnt      Score     Error  Units

BTreeSetBenchmark.add         1  avgt    8      9.360 ±   0.142  ns/op
BTreeSetBenchmark.add         2  avgt    8     28.199 ±   0.163  ns/op
BTreeSetBenchmark.add         3  avgt    8     47.219 ±   0.328  ns/op
BTreeSetBenchmark.add         4  avgt    8     61.724 ±   0.263  ns/op
BTreeSetBenchmark.add         6  avgt    8     94.449 ±   0.613  ns/op
BTreeSetBenchmark.add         8  avgt    8    225.320 ±   1.324  ns/op
BTreeSetBenchmark.add        12  avgt    8    333.533 ±   0.980  ns/op
BTreeSetBenchmark.add        16  avgt    8    558.451 ±   2.167  ns/op
BTreeSetBenchmark.add        24  avgt    8    837.878 ±   4.882  ns/op
BTreeSetBenchmark.add        32  avgt    8   1245.530 ±   2.774  ns/op
BTreeSetBenchmark.add        48  avgt    8   1790.055 ±  12.535  ns/op
BTreeSetBenchmark.add        64  avgt    8   3044.136 ±   9.722  ns/op
BTreeSetBenchmark.add        96  avgt    8   5031.803 ±  33.411  ns/op
BTreeSetBenchmark.add       128  avgt    8   6685.756 ±  17.619  ns/op
BTreeSetBenchmark.add       192  avgt    8  10804.432 ±  40.868  ns/op
BTreeSetBenchmark.add       256  avgt    8  14850.966 ± 149.874  ns/op

JTreeSetBenchmark.add         1  avgt    8      8.403 ±   0.045  ns/op
JTreeSetBenchmark.add         2  avgt    8     12.596 ±   0.069  ns/op
JTreeSetBenchmark.add         3  avgt    8     21.733 ±   0.109  ns/op
JTreeSetBenchmark.add         4  avgt    8     24.934 ±   0.033  ns/op
JTreeSetBenchmark.add         6  avgt    8     38.906 ±   0.346  ns/op
JTreeSetBenchmark.add         8  avgt    8     80.336 ±   0.132  ns/op
JTreeSetBenchmark.add        12  avgt    8    100.864 ±   0.899  ns/op
JTreeSetBenchmark.add        16  avgt    8    168.507 ±   0.352  ns/op
JTreeSetBenchmark.add        24  avgt    8    246.425 ±   2.139  ns/op
JTreeSetBenchmark.add        32  avgt    8    349.714 ±   0.886  ns/op
JTreeSetBenchmark.add        48  avgt    8    597.972 ±   1.594  ns/op
JTreeSetBenchmark.add        64  avgt    8    852.237 ±   4.263  ns/op
JTreeSetBenchmark.add        96  avgt    8   1319.508 ±  11.863  ns/op
JTreeSetBenchmark.add       128  avgt    8   1741.878 ±   2.702  ns/op
JTreeSetBenchmark.add       192  avgt    8   2815.356 ±  11.896  ns/op
JTreeSetBenchmark.add       256  avgt    8   3857.971 ±   7.622  ns/op

WTreeSetBenchmark.add         1  avgt    8      3.378 ±   0.042  ns/op
WTreeSetBenchmark.add         2  avgt    8      7.377 ±   0.033  ns/op
WTreeSetBenchmark.add         3  avgt    8     20.138 ±   0.119  ns/op
WTreeSetBenchmark.add         4  avgt    8     18.538 ±   0.059  ns/op
WTreeSetBenchmark.add         6  avgt    8     26.994 ±   0.152  ns/op
WTreeSetBenchmark.add         8  avgt    8     97.856 ±   0.736  ns/op
WTreeSetBenchmark.add        12  avgt    8    119.294 ±   0.450  ns/op
WTreeSetBenchmark.add        16  avgt    8    231.722 ±   0.670  ns/op
WTreeSetBenchmark.add        24  avgt    8    362.660 ±   2.246  ns/op
WTreeSetBenchmark.add        32  avgt    8    498.419 ±   3.311  ns/op
WTreeSetBenchmark.add        48  avgt    8    808.135 ±   6.930  ns/op
WTreeSetBenchmark.add        64  avgt    8   1199.399 ±  22.181  ns/op
WTreeSetBenchmark.add        96  avgt    8   2215.090 ±  20.373  ns/op
WTreeSetBenchmark.add       128  avgt    8   2921.914 ±  33.057  ns/op
WTreeSetBenchmark.add       192  avgt    8   4745.856 ±  23.071  ns/op
WTreeSetBenchmark.add       256  avgt    8   6387.992 ±  40.360  ns/op
```