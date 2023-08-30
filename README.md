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

WTreeSetBenchmark.contains    1  avgt    8      2.575 ±   0.007  ns/op
WTreeSetBenchmark.contains    2  avgt    8      4.908 ±   0.115  ns/op
WTreeSetBenchmark.contains    3  avgt    8      8.034 ±   0.185  ns/op
WTreeSetBenchmark.contains    4  avgt    8     10.055 ±   0.222  ns/op
WTreeSetBenchmark.contains    6  avgt    8     16.408 ±   0.240  ns/op
WTreeSetBenchmark.contains    8  avgt    8     25.197 ±   0.111  ns/op
WTreeSetBenchmark.contains   12  avgt    8     40.851 ±   0.076  ns/op
WTreeSetBenchmark.contains   16  avgt    8     53.462 ±   0.316  ns/op
WTreeSetBenchmark.contains   24  avgt    8     89.060 ±   0.608  ns/op
WTreeSetBenchmark.contains   32  avgt    8    128.520 ±   1.298  ns/op
WTreeSetBenchmark.contains   48  avgt    8    217.862 ±   0.817  ns/op
WTreeSetBenchmark.contains   64  avgt    8    309.186 ±   8.912  ns/op
WTreeSetBenchmark.contains   96  avgt    8    526.562 ±  18.510  ns/op
WTreeSetBenchmark.contains  128  avgt    8    782.840 ±  13.292  ns/op
WTreeSetBenchmark.contains  192  avgt    8   1260.129 ±  11.633  ns/op
WTreeSetBenchmark.contains  256  avgt    8   1807.930 ±   6.233  ns/op
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

WTreeSetBenchmark.add         1  avgt    8      3.430 ±   0.084  ns/op
WTreeSetBenchmark.add         2  avgt    8      9.041 ±   0.057  ns/op
WTreeSetBenchmark.add         3  avgt    8     15.569 ±   0.082  ns/op
WTreeSetBenchmark.add         4  avgt    8     13.434 ±   0.036  ns/op
WTreeSetBenchmark.add         6  avgt    8     21.464 ±   0.423  ns/op
WTreeSetBenchmark.add         8  avgt    8     78.034 ±   0.462  ns/op
WTreeSetBenchmark.add        12  avgt    8     91.809 ±   0.619  ns/op
WTreeSetBenchmark.add        16  avgt    8    193.997 ±   1.770  ns/op
WTreeSetBenchmark.add        24  avgt    8    315.485 ±   1.296  ns/op
WTreeSetBenchmark.add        32  avgt    8    403.325 ±   1.383  ns/op
WTreeSetBenchmark.add        48  avgt    8    707.021 ±   3.448  ns/op
WTreeSetBenchmark.add        64  avgt    8   1072.929 ±   7.222  ns/op
WTreeSetBenchmark.add        96  avgt    8   1945.479 ±   7.760  ns/op
WTreeSetBenchmark.add       128  avgt    8   2354.559 ±   6.616  ns/op
WTreeSetBenchmark.add       192  avgt    8   3739.823 ±  28.130  ns/op
WTreeSetBenchmark.add       256  avgt    8   5014.931 ±  36.373  ns/op
```