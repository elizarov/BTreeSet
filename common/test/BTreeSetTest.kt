package sortedset

object BTreeSetFactory : SortedSetFactory {
    override fun <E> createSortedSet(comparator: Comparator<in E>): SortedSet<E> =
        BTreeSet(comparator)
}

class BTreeSetTest : SortedSetTest() {
    override val factory: SortedSetFactory
        get() = BTreeSetFactory
}