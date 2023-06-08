expect class SortedSet<E>(comparator: Comparator<in E>) : SortedSetInterface<E> {
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun first(): E
    override fun contains(element: E): Boolean
    override fun isEmpty(): Boolean
}