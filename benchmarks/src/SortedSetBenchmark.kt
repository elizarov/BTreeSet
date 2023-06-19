package benchmarks

import org.openjdk.jmh.annotations.*
import sortedset.*
import kotlin.random.*

data class Key(val x: Int)

@State(Scope.Benchmark)
abstract class SortedSetBenchmark {
    protected abstract val factory: () -> SortedSet<Key>

    @Param("1", "2", "4", "8", "16", "32", "64", "128", "256", "512", "1024")
    @JvmField var n: Int = 0

    private lateinit var keys: List<Key>

    @Setup
    fun setup() {
        keys = List(n) { Key(it) }.shuffled(Random(1))
    }

    @Benchmark
    fun benchmarkCreateAndAdd() {
        val set = factory()
        for (key in keys) {
            set.add(key)
        }
    }
}