package benchmarks

import org.openjdk.jmh.annotations.*
import sortedset.*
import kotlin.random.*

data class Key(val x: Int)

object KeyComparator : Comparator<Key> {
    override fun compare(o1: Key, o2: Key): Int = o1.x - o2.x
}

@State(Scope.Benchmark)
abstract class SortedSetBenchmark(private val factory: () -> SortedSet<Key>) {
    @Param("1", "2", "3", "4", "6", "8", "12", "16", "24", "32", "48", "64", "96", "128", "192", "256")
    @JvmField var n: Int = 0

    private lateinit var keys: Array<Key>
    private lateinit var sharedSet: SortedSet<Key>

    @Setup
    fun setup() {
        keys = List(n) { Key(it) }.shuffled(Random(1)).toTypedArray()
        sharedSet = add()
    }

    @Benchmark
    fun contains(): Int {
        var count = 0
        for (key in keys) {
            if (sharedSet.contains(key)) count++
        }
        return count
    }

    @Benchmark
    fun add(): SortedSet<Key> {
        val set = factory()
        for (key in keys) {
            set.add(key)
        }
        return set
    }

    @Benchmark
    fun addRemove(): SortedSet<Key> {
        val set = factory()
        for (key in keys) {
            set.add(key)
        }
        for (key in keys) {
            set.remove(key)
        }
        return set
    }
}