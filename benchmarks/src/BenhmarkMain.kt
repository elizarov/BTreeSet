package benchmarks

import org.openjdk.jmh.annotations.*
import sortedset.*
import java.util.concurrent.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
open class BTreeSetBenchmark : SortedSetBenchmark() {
    override val factory: () -> SortedSet<Key>
        get() = {  BTreeSet(KeyComparator) }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 8, time = 1, timeUnit = TimeUnit.SECONDS)
open class JTreeSetBenchmark : SortedSetBenchmark() {
    override val factory: () -> SortedSet<Key>
        get() = {  JTreeSet(KeyComparator) }
}