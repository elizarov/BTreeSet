// This interface is declared only for the purpose of testing on JVM,
// so that both java.util.TreeSet and BTreeSet implementations can be tested against the same testsuite.
interface SortedSetInterface<E> {
    fun add(element: E): Boolean
    fun remove(element: E): Boolean
    fun first(): E
    fun contains(element: E): Boolean
    fun isEmpty(): Boolean
}

interface SortedSetFactory {
    fun <E> createSortedSet(comparator: Comparator<in E>): SortedSetInterface<E>
}

object DefaultSortedSetFactory : SortedSetFactory {
    override fun <E> createSortedSet(comparator: Comparator<in E>): SortedSetInterface<E> =
        SortedSet(comparator)
}

object BTreeSetFactory : SortedSetFactory {
    override fun <E> createSortedSet(comparator: Comparator<in E>): SortedSetInterface<E> =
        BTreeSet(comparator)
}