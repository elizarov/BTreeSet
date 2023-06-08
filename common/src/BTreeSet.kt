private const val M = 5 // The order the B-tree -- the max number of children on a page.
private const val MAX_N = M - 1 // Max number of keys in a page.

private const val RN = (MAX_N + 1) / 2 // the resulting number of elements in a right page after split (smaller)
private const val LN = MAX_N + 1 - RN // the resulting number of element in a left page after split (larger)

// The structure of flags: leaf_bit + n, where n is the number of keys in a page.
private const val LEAF_FLAG = 1 shl 31
@Suppress("INTEGER_OVERFLOW")
private const val N_MASK = LEAF_FLAG - 1

private val EMPTY_LINKS = intArrayOf()

// Function that specified the minimal growth factor of internal arrays.
// A factor of x1.5 is used just like in ArrayList implementation, but always realloc first for at least 4 pages
fun minGrow(size: Int) = (size * 3 / 2).coerceAtLeast(4)

// SortedSet implementation as a B-Tree of order M.
// It is optimized for small root-only trees.
@Suppress("UNCHECKED_CAST")
class BTreeSet<E>(
    private val comparator: Comparator<in E>
) : SortedSetInterface<E> {
    // total number of elements
    private var size = 0
    // total number of allocated pages
    private var pages = 1
    // the index of the first free page among allocated, flags link to the next free page
    private var freePage = -1
    // 1 element for each page, initially allocated only for root page
    private var flags: IntArray = intArrayOf(LEAF_FLAG)
    // MAX_N elements for each page, initially allocated only for root
    private var keys: Array<Any?> = arrayOfNulls(MAX_N)
    // M elements for each non-leaf page, initially empty because root is leaf
    private var links: IntArray = EMPTY_LINKS

    override fun isEmpty(): Boolean = size == 0

    override fun first(): E {
        if (size == 0) throw NoSuchElementException()
        var page = 0
        while (!isLeafPage(page)) page = links[page * M]
        return keys[page * MAX_N] as E
    }

    override fun contains(element: E): Boolean {
        var page = 0
        while (true) {
            val i = findIndex(page, element)
            if (i >= 0) return true
            if (isLeafPage(page)) return false
            page = links[page * M - i - 1]
        }
    }

    override fun add(element: E): Boolean {
        val rp = addImpl(0, element)
        if (rp < 0) return false
        size++
        if (rp == 0) return true
        splitRootPage(rp)
        return true
    }

    // Tries to add element to a page.
    // * result == -1 -- if the element was found, nothing was done
    // * result == 0 -- if the element was added, all is fine
    // * result > 0 -- if the page was split in two, the value is the new page index
    //        -- this page became the left page (at it has the larger size);
    //        -- new page became the right page.
    private fun addImpl(page: Int, element: E): Int {
        val i = findIndex(page, element)
        if (i >= 0) return -1
        val j = -i - 1 // insertion index
        val flag = flags[page]
        val n = flag and N_MASK
        if ((flag and LEAF_FLAG) != 0) {
            // adding to the leaf page
            if (n == MAX_N) return splitPageKeys(page, j, element, LEAF_FLAG)
            insertLeafElement(page, j, n, element)
            flags[page] = (n + 1) or LEAF_FLAG
            return 0
        }
        // adding to the non leaf page, first recurse into the corresponding page
        val lp = links[page * M + j]
        val rp = addImpl(lp, element)
        if (rp <= 0) return rp
        // no-leaf page needs to accommodate a new key
        if (n == MAX_N) return splitInternalPage(page, j, lp, rp)
        insertLeafElement(page, j, n, keys[lp * MAX_N + LN - 1] as E)
        keys[lp * MAX_N + LN - 1] = null
        flags[page] = n + 1
        links.copyInto(links, page * M + j + 1, page * M + j, page * M + n + 1)
        links[page * M + j + 1] = rp
        return 0
    }

    private fun insertLeafElement(page: Int, j: Int, n: Int, element: E) {
        keys.copyInto(keys, page * MAX_N + j + 1, page * MAX_N + j, page * MAX_N + n)
        keys[page * MAX_N + j] = element
    }

    // Splits the page's keys when it is full (n == MAX_N), when j is the new element insertion index.
    // Returns an index of a newly added page. The resulting pages will have the following number of keys:
    //      page        -- LN elements.
    //      rp (result) -- RN elements
    // BEFORE:   page  keys: k_0, k_1, ... k_j, ... k_{MAX_N-1}
    // AFTER:    page* keys: k_0, k_1, ... element, k_j, ... k_{MAX_N-1}
    //                      \----------------/ \--------------------/
    //   split into         page with LN keys       page with RN keys
    // This function changes flags using newFlags (0 or LEAF_FLAG).
    // The caller will use element keys[page * MAX_N + LN - 1] as the new midpoint.
    private fun splitPageKeys(page: Int, j: Int, element: E, newFlag: Int): Int {
        val rp = allocateLeafPage()
        flags[page] = (LN - 1) or newFlag
        flags[rp] = RN or newFlag
        if (j < LN) {
            // new element added to the left page
            keys.copyInto(keys, rp * MAX_N, page * MAX_N + LN - 1, page * MAX_N + MAX_N)
            keys.copyInto(keys, page * MAX_N + j + 1, page * MAX_N + j, page * MAX_N + LN - 1)
            keys[page * MAX_N + j] = element
        } else {
            // new element added to the right page, j >= LN
            keys.copyInto(keys, rp * MAX_N, page * MAX_N + LN, page * MAX_N + j)
            keys.copyInto(keys, rp * MAX_N + j - LN + 1, page * MAX_N + j, page * MAX_N + MAX_N)
            keys[rp * MAX_N + j - LN] = element
        }
        keys.fill(null, page * MAX_N + LN, page * MAX_N + MAX_N)
        return rp
    }

    private fun splitInternalPage(page: Int, j: Int, lp: Int, rp0: Int): Int {
        val rp = splitPageKeys(page, j, keys[lp * MAX_N + LN - 1] as E, 0)
        allocatePageLinks(rp)
        if (j < LN) {
            // new element was added to the left page
            links.copyInto(links, rp * M, page * M + LN - 1, page * M + M)
            links.copyInto(links, page * M + j + 1, page * M + j, page * M + LN - 1)
            if (j == LN - 1) {
                links[rp * M] = rp0
            } else {
                links[page * M + j + 1] = rp0
            }
        } else {
            // new element added to the right page, j >= LN
            links.copyInto(links, rp * M, page * M + LN, page * M + j)
            links.copyInto(links, rp * M + j - LN + 2, page * M + j + 1, page * M + M)
            links[rp * M + j - LN] = lp
            links[rp * M + j - LN + 1] = rp0
        }
        links.fill(0, page * M + LN, page * M + M)
        return rp
    }

    private fun splitRootPage(rp: Int) {
        // first copy old root to new page
        val lp = allocateLeafPage()
        keys.copyInto(keys, lp * MAX_N, 0, LN - 1)
        keys[0] = keys[LN - 1]
        keys.fill(null, 1, LN)
        // now fix up root -- it is non-leaf and has 1 key and two children
        val rootWasLeafPage = isLeafPage(0)
        flags[0] = 1
        allocatePageLinks(0)
        if (rootWasLeafPage) {
            flags[lp] = (LN - 1) or LEAF_FLAG
        } else {
            allocatePageLinks(lp)
            links.copyInto(links, lp * M, 0, LN)
            flags[lp] = LN - 1
        }
        links[0] = lp
        links[1] = rp
        links.fill(0, 2, LN)
    }

    override fun remove(element: E): Boolean {
        val res= removeImpl(0, element)
        if (res < 0) return false
        size--
        return true
    }

    // Tries to remove an element from a page.
    // * result == -1 -- if the element was NOT found, nothing was done
    // * result >= 0 -- if the element was removed, the value is the resulting number of keys in the page
    private fun removeImpl(page: Int, element: E): Int {
        val i = findIndex(page, element)
        if (i < 0) {
            if (isLeafPage(page)) return -1 // leaf page -- element was not found
            // recursively remove from the child page
            val j = -i - 1
            val lp = links[page * M + j]
            val res = removeImpl(lp, element)
            if (res < 0) return -1
            if (res == 0) removeChild(page, j, lp)
            return flags[page] and N_MASK
        }
        // element was found at index i
        val flag = flags[page]
        val n = flag and N_MASK
        if ((flag and LEAF_FLAG) != 0) {
            // remove element from leaf page
            keys.copyInto(keys, page * MAX_N + i, page * MAX_N + i + 1, page * MAX_N + n)
            val newN = n - 1
            keys[page * MAX_N + newN] = null
            flags[page] = newN or LEAF_FLAG
            return newN
        }
        // remove element from non-leaf page
        TODO()
    }

    // Removes an empty page lp that sits at index j of a parent page.
    private fun removeChild(page: Int, j: Int, lp: Int) {
        if (!isLeafPage(lp)) {
            // child was not a leaf page, so copy its single link to the parent page
            links[page * M + j] = links[lp * M]
            links[lp * M] = 0
            freePage(lp)
            return
        }
        // child was a leaf page -- remove the separating key and add it back here
        val element = keys[page * MAX_N + j] as E
        val n = flags[page] and N_MASK
        keys.copyInto(keys, page * MAX_N + j, page * MAX_N + j + 1, page * MAX_N + n)
        keys[page * MAX_N + n - 1] = null
        flags[page] = n - 1

        TODO() // remove it...
    }

    private fun isLeafPage(page: Int) = (flags[page] and LEAF_FLAG) != 0

    // Returns index of an element in a page.
    // Returns negative number (-insertionIndex - 1) if not found.
    private fun findIndex(page: Int, element: E): Int {
        val n = flags[page] and N_MASK
        for (i in 0 until n) {
            val c = comparator.compare(element, keys[page * MAX_N + i] as E)
            if (c == 0) return i
            if (c < 0) return -i - 1
        }
        return -n - 1
    }

    // Allocates new leaf page, makes sure keys and flags have enough capacity, but does not fill them.
    private fun allocateLeafPage(): Int {
        var rp = freePage
        if (rp >= 0) {
            // we have an allocated page in a free list, return it
            freePage = flags[rp]
            return rp
        }
        // allocate new page
        rp = pages++
        if (pages > flags.size) {
            flags = flags.copyOf(pages.coerceAtLeast(minGrow(flags.size)))
        }
        if (pages * MAX_N > keys.size) {
            keys = keys.copyOf(pages.coerceAtLeast(minGrow(keys.size / MAX_N)) * MAX_N)
        }
        return rp
    }

    private fun allocatePageLinks(page: Int) {
        if (page * M >= links.size) {
            links = links.copyOf((page + 1).coerceAtLeast(minGrow(links.size / M)) * M)
        }
    }

    private fun freePage(page: Int) {
        flags[page] = freePage
        freePage = page
    }
}