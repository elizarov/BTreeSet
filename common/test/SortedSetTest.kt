package sortedset

import kotlin.random.*
import kotlin.test.*

abstract class SortedSetTest {
    protected abstract val factory: SortedSetFactory

    private fun <E: Comparable<*>> sortedSetOf(vararg elements: E): SortedSet<E> =
        sortedSetOf(compareBy { it }, *elements)

    private fun <E> sortedSetOf(comparator: Comparator<in E>, vararg elements: E): SortedSet<E> {
        val set = factory.createSortedSet(comparator)
        for (element in elements) {
            set.add(element)
            assertContains(set, element, "$element was added")
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
        val rnd = Random(1)
        val numbers = (1..1000).map { rnd.nextInt(1_000_000..9_999_9999) }.distinct()
        val set = sortedSetOf(*numbers.toTypedArray())
        assertOrderEquals(numbers.sorted(), set)
    }

    @Test
    fun testAddConsistency() {
        val rnd = Random(13)
        val elements = (1..3).shuffled(rnd)
        val set = factory.createSortedSet<Int>(compareBy { it })
        for (i in elements.indices) {
            val element = elements[i]
            set.add(element)
            for (j in 0..i) {
                val check = elements[j]
                assertContains(set, check, "set contains $check after adding $i-th element $element")
            }
        }
    }

    private fun <E> assertContains(set: SortedSet<E>, element: E, message: String) {
        val contains = try {
            set.contains(element)
        } catch (e: Exception) {
            fail(message, e)
        }
        assertTrue(contains, message)
    }

    @Test
    fun customComparator() {
        val set = sortedSetOf(compareBy { it.length }, "B", "AAA", "DD")
        assertOrderEquals(listOf("B", "DD", "AAA"), set)
    }

    @Test
    fun testAddRemove() {
        val rnd = Random(1)
        for (n in listOf(1, 2, 3, 4, 5, 6, 7, 10, 20, 50, 100)) {
            checkAddRemove(rnd, (1..n).toList()) // insert in order
            checkAddRemove(rnd, (n downTo 1).toList()) // insert backwards
            checkAddRemove(rnd, (1..n).shuffled(rnd)) // insert randomly
        }
    }

    private fun <E : Comparable<E>> checkAddRemove(rnd: Random, elements: List<E>) {
        val min = elements.min()
        val set = factory.createSortedSet<E>(compareBy { it })
        for (iteration in 0..7) {
            for (x in elements) {
                assertTrue(set.add(x)) // insert all elements in order
            }
            for (x in elements) {
                assertContains(set, x, "set contains $x") // check that they are all there
            }
            assertEquals(min, set.first())
            assertFalse(set.isEmpty())
            when (iteration % 4) {
                0 -> for (x in elements) {
                    assertTrue(set.remove(x)) // remove in original order
                }
                1 -> for (x in elements.reversed()) {
                    assertTrue(set.remove(x)) // remove in reversed order
                }
                2 -> for (x in elements) {
                    assertTrue(set.remove(set.first())) // remove first element
                }
                3 -> for (x in elements.shuffled(rnd)) {
                    assertTrue(set.remove(x)) // remove in random order
                }
            }
            for (x in elements) assertFalse(set.contains(x))
            assertTrue(set.isEmpty())
        }
    }
}