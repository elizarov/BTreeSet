package sortedset

object JTreeSetFactory : SortedSetFactory {
    override fun <E> createSortedSet(comparator: Comparator<in E>): SortedSet<E> =
        JTreeSet(comparator)
}

class JTreeSetTest : SortedSetTest() {
    override val factory: SortedSetFactory
        get() = JTreeSetFactory
}