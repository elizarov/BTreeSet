package sortedset

import java.util.*

class JTreeSet<E>(comparator: Comparator<in E>) : TreeSet<E>(comparator), SortedSet<E>