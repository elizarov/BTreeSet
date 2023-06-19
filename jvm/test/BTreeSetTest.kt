package sortedset

class BTreeSetTest : SortedSetTest() {
    override val factory: SortedSetFactory
        get() = BTreeSetFactory
}