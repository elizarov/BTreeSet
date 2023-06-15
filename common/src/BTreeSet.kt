private const val M = 5 // The order the B-tree -- the max number of children on a page.
private const val MAX_N = M - 1 // Max number of keys in a page.
private const val MIN_N = M / 2 // Min number of keys in a page.

// The resulting sizes of two pages when overflowing page with MAX_N + 1 keys is split
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
) : SortedSet<E> {
    // total number of elements
    private var size = 0
    // total number of allocated pages
    private var pages = 1
    // the index of the first free page among allocated, flags link to the next free page
    private var freePage = -1
    // the index of the root page
    private var rootPage = 0
    // 1 element for each page, initially allocated only for root page
    private var flags: IntArray = intArrayOf(LEAF_FLAG)
    // MAX_N elements for each page, initially allocated only for root
    private var keys: Array<Any?> = arrayOfNulls(MAX_N)
    // M elements for each non-leaf page, initially empty because root is leaf
    private var links: IntArray = EMPTY_LINKS

    override fun isEmpty(): Boolean = size == 0

    override fun first(): E {
        if (size == 0) throw NoSuchElementException()
        var page = rootPage
        while (!isLeafPage(page)) page = getLink(page, 0)
        return getKey(page, 0)
    }

    override fun contains(element: E): Boolean {
        var page = rootPage
        while (true) {
            val i = findIndex(page, element)
            if (i >= 0) return true
            if (isLeafPage(page)) return false
            page = getLink(page, -i - 1)
        }
    }

    override fun add(element: E): Boolean {
        val rp = addImpl(rootPage, element)
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
        val lp = getLink(page, j)
        val rp = addImpl(lp, element)
        if (rp <= 0) return rp
        // no-leaf page needs to accommodate a new key
        if (n == MAX_N) return splitInternalPage(page, j, lp, rp)
        insertLeafElement(page, j, n, getKey(lp, LN - 1))
        clearKey(lp, LN - 1)
        flags[page] = n + 1
        copyLinks(page, j + 1, page, j, n + 1)
        setLink(page, j + 1, rp)
        return 0
    }

    private fun insertLeafElement(page: Int, j: Int, n: Int, element: E) {
        copyKeys(page, j + 1, page, j, n)
        setKey(page, j, element)
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
            copyKeys(rp, 0, page, LN - 1, MAX_N)
            copyKeys(page, j + 1, page, j, LN - 1)
            setKey(page, j, element)
        } else {
            // new element added to the right page, j >= LN
            copyKeys(rp, 0, page, LN, j)
            copyKeys(rp, j - LN + 1, page, j, MAX_N)
            setKey(rp, j - LN, element)
        }
        clearKeys(page, LN, MAX_N)
        return rp
    }

    private fun splitInternalPage(page: Int, j: Int, lp: Int, rp0: Int): Int {
        val rp = splitPageKeys(page, j, getKey(lp, LN - 1), 0)
        allocatePageLinks(rp)
        if (j < LN) {
            // new element was added to the left page
            copyLinks(rp, 0, page, LN - 1, M)
            copyLinks(page, j + 1, page, j, LN - 1)
            if (j == LN - 1) {
                setLink(rp, 0, rp0)
            } else {
                setLink(page, j + 1, rp0)
            }
        } else {
            // new element added to the right page, j >= LN
            copyLinks(rp, 0, page, LN, j)
            copyLinks(rp, j - LN + 2, page, j + 1, M)
            setLink(rp, j - LN, lp)
            setLink(rp, j - LN + 1, rp0)
        }
        clearLinks(page, LN, M)
        clearLinks(rp, RN + 1, M)
        return rp
    }

    // splits root page after add, increasing tree level by one
    // old rootPage goes to the left, rp becomes the right page
    private fun splitRootPage(rp: Int) {
        // allocate new root page and set it up
        val np = allocateLeafPage()
        allocatePageLinks(np)
        copyKey(np, 0, rootPage, LN - 1)
        clearKey(rootPage, LN - 1)
        setLink(np, 0, rootPage) // old root
        setLink(np, 1, rp)
        flags[np] = 1
        rootPage = np
    }

    override fun remove(element: E): Boolean {
        val res= removeImpl(rootPage, element)
        if (res < 0) return false
        if (res == 0 && !isLeafPage(rootPage)) removeDegenerateRootPage()
        size--
        return true
    }

    // Drops root page after remove, decreasing tree level by one
    private fun removeDegenerateRootPage() {
        val np = getLink(rootPage, 0)
        setLink(rootPage, 0, -1)
        freePage(rootPage)
        rootPage = np
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
            val lp = getLink(page, j)
            val res = removeImpl(lp, element)
            if (res < 0) return -1
            if (res >= MIN_N) return nKeys(page)
            // the resulting page is too small - rebalance
            return rebalancePageChildren(page, findRebalanceIndex(page, j))
        }
        // element was found at index i
        val flag = flags[page]
        val n = flag and N_MASK
        if ((flag and LEAF_FLAG) != 0) {
            // remove element from leaf page
            copyKeys(page, i, page, i + 1, n)
            val newN = n - 1
            clearKey(page, newN)
            flags[page] = newN or LEAF_FLAG
            return newN
        }
        // remove element from non-leaf page by moving the last element from the left child into its place
        val lp = getLink(page, i)
        setKey(page, i, removeLastImpl(lp))
        if (nKeys(lp) >= MIN_N) return n // everything is Ok w.r.t to size
        // the new page on the left is too small -- rebalance it
        return rebalancePageChildren(page, findRebalanceIndex(page, i))
    }

    // Removes the last element from the specified page, does not rebalance this page.
    // The caller must take care of rebalancing the page if needed.
    private fun removeLastImpl(page: Int): E {
        val n = nKeys(page)
        if (isLeafPage(page)) {
            val result = getKey(page, n - 1)
            clearKey(page, n - 1)
            flags[page]--
            return result
        }
        // this page is not leaf, remove from the last one
        val rp = getLink(page, n)
        val result = removeLastImpl(rp)
        if (nKeys(rp) < MIN_N) rebalancePageChildren(page, n - 1)
        return result
    }

    // Finds the index at which to call rebalancePageChildren when the child at in j has become underful
    private fun findRebalanceIndex(page: Int, j: Int): Int =
        if (j == 0 || j < nKeys(page) - 1 && nKeys(getLink(page, j + 1)) > nKeys(getLink(page, j - 1)))
            j else j - 1

    // Rebalances children of page at positions k and k + 1 (one of them must have less than MIN_N keys)
    // Returns the resulting number of keys on page.
    private fun rebalancePageChildren(page: Int, k: Int): Int {
        val n = nKeys(page)
        val lp = getLink(page, k)
        val rp = getLink(page, k + 1)
        val ln = nKeys(lp)
        val rn = nKeys(rp)
        val leafChildren = isLeafPage(lp)
        check(ln < MIN_N || rn < MIN_N)
        check(leafChildren == isLeafPage(rp))
        val ns = ln + rn
        if (ns + 1 <= MAX_N) {
            // merge both children
            copyKey(lp, ln, page, k)
            copyKeys(lp, ln + 1, rp, 0, rn)
            clearKeys(rp, 0, rn)
            if (!leafChildren) {
                copyLinks(lp, ln + 1, rp, 0, rn + 1)
                clearLinks(rp, 0, rn + 1)
            }
            flags[lp] = flag(ns + 1, leafChildren)
            freePage(rp)
            copyKeys(page, k, page, k + 1, n)
            clearKey(page, n - 1)
            copyLinks(page, k + 1, page, k + 2, n + 1)
            setLink(page, n, -1)
            flags[page] = n - 1
            return n - 1
        }
        // redistribute evenly
        val ln1 = ns / 2
        val rn1 = ns - ln1
        check(ln1 >= MIN_N && rn1 >= MIN_N)
        if (ln1 > ln) {
            // move left
            val m = ln1 - ln
            copyKey(lp, ln, page, k)
            copyKeys(lp, ln + 1, rp, 0, m - 1)
            copyKey(page, k, rp, m - 1)
            copyKeys(rp, 0, rp, m, rn)
            clearKeys(rp, rn1, rn)
            if (!leafChildren) {
                copyLinks(lp, ln + 1, rp, 0, m)
                copyLinks(rp, 0, rp, m, rn + 1)
                clearLinks(rp, rn1 + 1, rn + 1)
            }
        } else {
            // move right
            check(rn1 > rn)
            val m = rn1 - rn
            copyKeys(rp, m, rp, 0, rn)
            copyKey(rp, m - 1, page, k)
            copyKeys(rp, 0, lp, ln1 + 1, ln)
            copyKey(page, k, lp, ln1)
            clearKeys(lp, ln1, ln)
            if (!leafChildren) {
                copyLinks(rp, m, rp, 0, rn + 1)
                copyLinks(rp, 0, lp, ln1 + 1, ln + 1)
                clearLinks(lp, ln1 + 1, ln + 1)
            }
        }
        flags[lp] = flag(ln1, leafChildren)
        flags[rp] = flag(rn1, leafChildren)
        return n
    }

    private fun flag(n: Int, leaf: Boolean): Int = if (leaf) n or LEAF_FLAG else n
    private fun isLeafPage(page: Int) = (flags[page] and LEAF_FLAG) != 0
    private fun nKeys(page: Int) = flags[page] and N_MASK

    private fun getKey(page: Int, index: Int): E = keys[page * MAX_N + index] as E

    private fun setKey(page: Int, index: Int, element: E) {
        keys[page * MAX_N + index] = element
    }

    private fun copyKey(dstPage: Int, dstIndex: Int, srcPage: Int, srcIndex: Int) {
        keys[dstPage * MAX_N + dstIndex] = keys[srcPage * MAX_N + srcIndex]
    }

    private fun copyKeys(dstPage: Int, dstIndex: Int, srcPage: Int, startIndex: Int, endIndex: Int) {
        keys.copyInto(keys, dstPage * MAX_N + dstIndex, srcPage * MAX_N + startIndex, srcPage * MAX_N + endIndex)
    }

    private fun clearKey(dstPage: Int, dstIndex: Int) {
        keys[dstPage * MAX_N + dstIndex] = null
    }

    private fun clearKeys(dstPage: Int, startIndex: Int, endIndex: Int) {
        keys.fill(null, dstPage * MAX_N + startIndex, dstPage * MAX_N + endIndex)
    }

    private fun getLink(page: Int, index: Int) = links[page * M + index]

    private fun setLink(page: Int, index: Int, value: Int) {
        links[page * M + index] = value
    }

    private fun copyLinks(dstPage: Int, dstIndex: Int, srcPage: Int, startIndex: Int, endIndex: Int) {
        links.copyInto(links, dstPage * M + dstIndex, srcPage * M + startIndex, srcPage * M + endIndex)
    }

    private fun clearLinks(dstPage: Int, startIndex: Int, endIndex: Int) {
        links.fill(-1, dstPage * M + startIndex, dstPage * M + endIndex)
    }

    // Returns index of an element in a page.
    // Returns negative number (-insertionIndex - 1) if not found.
    private fun findIndex(page: Int, element: E): Int {
        val n = flags[page] and N_MASK
        val pageOffset = page * MAX_N
        for (i in 0 until n) {
            val c = comparator.compare(element, keys[pageOffset + i] as E)
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
        val curSize = links.size
        if (page * M >= curSize) {
            links = links.copyOf((page + 1).coerceAtLeast(minGrow(curSize / M)) * M)
            links.fill(-1, curSize) // fill with -1 to fail fast in case of bugs
        }
    }

    private fun freePage(page: Int) {
        flags[page] = freePage
        freePage = page
    }
}