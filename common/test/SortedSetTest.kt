import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SortedSetTest {
    private fun <E: Comparable<*>> sortedSetOf(vararg elements: E): SortedSet<E> =
        sortedSetOf(compareBy { it }, *elements)

    private fun <E> sortedSetOf(comparator: Comparator<in E>, vararg elements: E): SortedSet<E> {
        val set = SortedSet(comparator)
        for (element in elements) {
            set.add(element)
            assertTrue(set.contains(element))
        }
        return set
    }

    private fun <E> assertOrderEquals(expect: Iterable<E>, actual: SortedSet<E>) {
        for (e in expect) {
            assertEquals(e, actual.first())
            assertTrue(actual.contains(e))
            assertTrue(actual.remove(e))
            assertFalse(actual.contains(e))
        }
        assertTrue(actual.isEmpty())
    }

    @Test
    fun correctOrder() {
        assertOrderEquals(listOf(1, 2, 5, 6), sortedSetOf(1, 2, 5, 6))
        assertOrderEquals(listOf(1, 2, 5, 6), sortedSetOf(2, 6, 1, 5))
        val numbers = (1..1000).map { Random.nextInt(10_000_000) }.distinct()
        val set = sortedSetOf(*numbers.toTypedArray())
        assertOrderEquals(numbers.sorted(), set)
    }

    @Test
    fun customComparator() {
        val set = sortedSetOf(compareBy { it.length }, "B", "AAA", "DD")
        assertOrderEquals(listOf("B", "DD", "AAA"), set)
    }

    @Test
    fun testAddRemove() {
        val n = 100
        checkAddRemove((1..n).toList()) // insert in order
        checkAddRemove((n downTo 1).toList()) // insert backwards
        checkAddRemove((1..n).shuffled(Random(1))) // insert randomly
    }

    private fun <E : Comparable<E>> checkAddRemove(elements: List<E>) {
        val min = elements.min()
        val set = SortedSet<E>(compareBy { it })
        for (iteration in 0..5) {
            for (x in elements) set.add(x)
            for (x in elements) assertTrue(set.contains(x))
            assertEquals(min, set.first())
            assertFalse(set.isEmpty())
            when (iteration % 2) {
                0 -> for (x in elements) set.remove(x) // remove in original order
                1 -> for (x in elements.reversed()) set.remove(x) // remove in reverse
                2 -> for (x in elements) set.remove(set.first()) // remove first
            }
            for (x in elements) assertFalse(set.contains(x))
            assertTrue(set.isEmpty())
        }
    }
}