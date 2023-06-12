expect class SortedSetImpl<E>(comparator: Comparator<in E>) : SortedSet<E> {
    override fun add(element: E): Boolean
    override fun remove(element: E): Boolean
    override fun first(): E
    override fun contains(element: E): Boolean
    override fun isEmpty(): Boolean
}