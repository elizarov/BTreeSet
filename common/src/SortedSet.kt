internal expect class SortedSet<E>(comparator: Comparator<in E>) {
    fun add(element: E): Boolean
    fun remove(element: E): Boolean
    fun first(): E
    fun contains(element: E): Boolean
    fun isEmpty(): Boolean
}