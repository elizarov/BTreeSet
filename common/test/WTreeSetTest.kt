package sortedset

object WTreeSetFactory : SortedSetFactory {
    override fun <E> createSortedSet(comparator: Comparator<in E>): SortedSet<E> =
        WTreeSet(comparator)
}

class WTreeSetTest : SortedSetTest() {
    override val factory: SortedSetFactory
        get() = WTreeSetFactory
}