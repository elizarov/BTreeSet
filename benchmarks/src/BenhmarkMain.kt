package benchmarks

import org.openjdk.jmh.annotations.*
import sortedset.*
import java.util.concurrent.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
open class BTreeSetBenchmark : SortedSetBenchmark() {
    override val factory: () -> SortedSet<Key>
        get() = {  BTreeSet(compareBy { it.x }) }
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(1)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
open class JTreeSetBenchmark : SortedSetBenchmark() {
    override val factory: () -> SortedSet<Key>
        get() = {  SortedSetImpl(compareBy { it.x }) }
}