# TM4E Core Benchmarks

## GrammarBenchmark

The [GrammarBenchmark](../test/java/org/eclipse/tm4e/core/benchmark/GrammarBenchmark.java) measures how long it takes to tokenize a given source file using the
**[Grammar](../org.eclipse.tm4e.core/src/main/java/org/eclipse/tm4e/core/internal/grammar/Grammar.java)#tokenizeLine()**
method and how much memory is allocated on the JVM heap to do so. As test source file the
[GrammarBenchmark.JavaFile.txt](../test/resources/org/eclipse/tm4e/core/benchmark/GrammarBenchmark.JavaFile.txt) is used.

The benchmark executes multiple rounds. In the beginning warm-up rounds are executed to get the JIT compiler activated.
The results of these rounds should be ignored.

Each round is executed sequentially in a new thread. This gives the OS the opportunity, that in case for the first round an over-utilized core was chosen, to
select a better core to execute the new thread of the next benchmark round.

The output will look something like this:
```yaml
[0.047s][info][gc] Using G1
Source Code chars: 36.385
Source Code lines: 901
JVM Vendor: ojdkbuild
JVM Version: 11.0.14
JVM Inital Heap: 2048.00 MB
JVM Maximum Heap: 2048.00 MB
JVM Args: -Xms2048M -Xmx2048M -XX:+UseG1GC -Xlog:gc:stderr -Dfile.encoding=UTF-8
--------------------------------
Warmup Rounds: 3
Benchmark Rounds: 3
Operations per Benchmark Round: 50
--------------------------------
warm-up 1/3...
#[1.626s][info][gc] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 102M->3M(2048M) 4.692ms
#[6.155s][info][gc] GC(1) Pause Young (Normal) (G1 Evacuation Pause) 690M->3M(2048M) 3.132ms
#[10.557s][info][gc] GC(2) Pause Young (Normal) (G1 Evacuation Pause) 719M->3M(2048M) 2.788ms
#[15.228s][info][gc] GC(3) Pause Young (Normal) (G1 Evacuation Pause) 761M->3M(2048M) 2.957ms
#[20.160s][info][gc] GC(4) Pause Young (Normal) (G1 Evacuation Pause) 804M->3M(2048M) 2.743ms
#[25.590s][info][gc] GC(5) Pause Young (Normal) (G1 Evacuation Pause) 882M->3M(2048M) 2.632ms
 -> result: 28307 ms/round |  529.90 ops/s | 113.23 ms/op
warm-up 2/3...
#[31.646s][info][gc] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 987M->3M(2048M) 2.683ms
#[38.579s][info][gc] GC(7) Pause Young (Normal) (G1 Evacuation Pause) 1129M->3M(2048M) 2.927ms
#[46.106s][info][gc] GC(8) Pause Young (Normal) (G1 Evacuation Pause) 1227M->3M(2048M) 2.872ms
#[53.542s][info][gc] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 1227M->3M(2048M) 2.727ms
 -> result: 27172 ms/round |  552.04 ops/s | 108.69 ms/op
warm-up 3/3...
#[61.026s][info][gc] GC(10) Pause Young (Normal) (G1 Evacuation Pause) 1227M->3M(2048M) 3.225ms
#[68.556s][info][gc] GC(11) Pause Young (Normal) (G1 Evacuation Pause) 1227M->3M(2048M) 2.938ms
#[76.021s][info][gc] GC(12) Pause Young (Normal) (G1 Evacuation Pause) 1227M->3M(2048M) 2.766ms
 -> result: 27129 ms/round |  552.91 ops/s | 108.52 ms/op
--------------------------------
benchmark 1/3...
#[83.000s][info][gc] GC(13) Pause Full (System.gc()) 1139M->3M(2048M) 9.200ms
#[84.020s][info][gc] GC(14) Pause Full (System.gc()) 3M->3M(2048M) 6.465ms
 -> result:  5315 ms/round |  564.44 ops/s | 106.30 ms/op | 17.73 MB/op
benchmark 2/3...
#[90.352s][info][gc] GC(15) Pause Full (System.gc()) 889M->3M(2048M) 8.125ms
#[91.361s][info][gc] GC(16) Pause Full (System.gc()) 3M->3M(2048M) 5.792ms
 -> result:  5208 ms/round |  576.04 ops/s | 104.16 ms/op | 17.73 MB/op
benchmark 3/3...
#[97.590s][info][gc] GC(17) Pause Full (System.gc()) 889M->3M(2048M) 7.367ms
#[98.604s][info][gc] GC(18) Pause Full (System.gc()) 3M->3M(2048M) 6.515ms
 -> result:  5202 ms/round |  576.70 ops/s | 104.04 ms/op | 17.73 MB/op
DONE.
```

The results of the three benchmark rounds show that it was possible to parse the whole source file 564.44 to 576.70 times per second.\
One time parsing of the source file took 104.04 to 106.30 ms and allocated 17.73MB of temporary objects on the JVM heap.

### How to run the benchmark

To run the benchmark execute the `run-grammar-benchmark.sh` or `run-grammar-benchmark.cmd` from a command line window.

You can also run the [GrammarBenchmark](../test/java/org/eclipse/tm4e/core/benchmark/GrammarBenchmark.java) from within Eclipse via
`Run As -> Java Application` for development/debugging/testing purposes. However you need to add these VM arguments to the launch configuration:
`-Xms2048M -Xmx2048M -XX:+UseG1GC -Xlog:gc:stderr -Dfile.encoding=UTF-8`. Don't rely on the results when launched like this.
