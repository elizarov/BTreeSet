package sortedset

// WAVL Tree. See "Rank-Balanced Trees" by BERNHARD HAEUPLER, SIDDHARTHA SEN, ROBERT E. TARJAN
class WTreeSet<E>(private val comparator: Comparator<in E>) : SortedSet<E> {
    private var root: Node<E>? = null

    private class Node<E>(var key: E) {
        var left: Node<E>? = null
        var right: Node<E>? = null
        var rank: Int = 0 // Invariant: all leaf nodes have rank 0
        operator fun get(rt: Boolean): Node<E>? = if (rt) right else left
        operator fun set(rt: Boolean, value: Node<E>?) { if (rt) right = value else left = value }
    }

    // Invariant: difference between rank of node at its parent is 1 or 2
    private val Node<E>?.rank: Int get() = this?.rank ?: -1

    private val Node<*>.isLeaf: Boolean
        get() = rank == 0

    override fun isEmpty(): Boolean = root == null

    override fun first(): E {
        var p = root ?: throw NoSuchElementException()
        while (true) p = p.left ?: break
        return p.key
    }

    override fun contains(element: E): Boolean {
        var p = root
        while (p != null) {
            val c = comparator.compare(element, p.key)
            p = when {
                c < 0 -> p.left
                c > 0 -> p.right
                else -> return true
            }
        }
        return false
    }

    override fun add(element: E): Boolean {
        root = root.addImpl(element) ?: return false
        return true
    }

    // Adds element to the subtree at this node
    //  * Returns new reference to the subtree.
    //  * Returns null if the element was found.
    private fun Node<E>?.addImpl(element: E): Node<E>? {
        if (this == null) return Node(element)
        val c = comparator.compare(element, key)
        val rt = c > 0
        val p = when {
            rt -> right
            c < 0 -> left
            else -> return null
        }.addImpl(element) ?: return null
        this[rt] = p
        // rebalance if needed
        if (rank <= p.rank) return rebalanceAfterAdd(rt, p)
        assert { rank <= p.rank + 2 }
        return this // ok, no need to rebalance
    }

    private fun Node<E>.rebalanceAfterAdd(rt: Boolean, p: Node<E>): Node<E> {
        assert { this[rt] === p && rank == p.rank }
        val q = this[!rt]
        val dq = rank - q.rank
        if (dq == 1) { // this is 0,1 node
            rank++ // promote this node to 1,2
            return this
        }
        // this is 0,2 node
        assert { dq == 2 }
        val r = p[rt]
        if (p.rank == r.rank + 1) {
            //  Note: the picture is for rt = false
            //       [this]
            //      / 0   \ 2
            //     [p]     [q]
            //  1 /  \ 2
            //  [r]   [s]
            assert { p.rank == p[!rt].rank + 2 }
            rotate(!rt)
            //       [p]
            //    1 /   \ 0
            //    [r]    [this]
            //        2 /     \ 2
            //         [s]    [q]
            rank-- // demote this node
            //       [p]
            //    1 /   \ 1
            //    [r]    [this]
            //        1 /     \ 1
            //         [s]    [q]
            return p
        } else {
            //  Note: the picture is for rt = false
            //       [this]
            //      / 0   \ 2
            //     [p]     [q]
            //  2 /  \ 1
            //  [r]   [s]
            //   1,2 /  \ 1,2
            //      [b] [c]
            val s = p[!rt]!!
            assert { p.rank == r.rank + 2 }
            assert { p.rank == s.rank + 1 }
            p.rotate(rt)
            this[rt] = s
            //           [this]
            //        1 /     \ 2
            //        [s]     [q]
            //    -1 /  \ 1,2
            //     [p]  [c]
            //  2 /  \ 2,3
            //  [r]  [b]
            rotate(!rt)
            //             [s]
            //     -1  /        \ -1
            //     [p]           [this]
            // 2  /   \ 2,3   2,3 /    \ 2
            //  [r]   [b]      [c]      [q]
            s.rank++
            p.rank--
            rank--
            //            [s]
            //     1  /        \  1
            //     [p]           [this]
            // 1  /   \ 1,2   1,2 /    \ 1
            //  [r]   [b]      [c]      [q]
            return s
        }
    }

    override fun remove(element: E): Boolean =
        when (root?.removeImpl(element)) {
            1 -> {
                root = null
                true
            }
            2 -> true
            else -> false
        }

    // Removes element from the subtree at this node.
    // * returns 0 if the element was not found.
    // * returns 1 if the element was found in a leaf that should be removed.
    // * returns 2 if the element was found and removed in non-leaf node.
    private fun Node<E>.removeImpl(element: E): Int {
        val c = comparator.compare(element, key)
        val rt = c > 0
        val p = when {
            rt -> right
            c < 0 -> left
            isLeaf -> return 1
            else -> {
                removeNonLeafNode()
                return 2
            }
        } ?: return 0
        when (p.removeImpl(element)) {
            0 -> return 0
            1 -> {
                this[rt] = null
                if (this[!rt] == null) {
                    rank = 0
                    return 2
                }
            }
        }
        rebalanceAfterRemoveIfNeeded(rt)
        return 2
    }

    // Removes key at this node.
    // It should not be called on a leaf node.
    private fun Node<E>.removeNonLeafNode() {
        assert { !isLeaf }
        val left = left ?: run {
            val right = right!!
            assert { right.isLeaf && rank == 1 }
            key = right.key
            rank = 0
            this.right = null
            return
        }
        if (left.isLeaf) {
            key = left.key
            this.left = null
            if (right == null) {
                rank = 0
                return
            }
        } else {
            key = left.removeLastFromNonLeafNode()
        }
        rebalanceAfterRemoveIfNeeded(rt = false)
    }

    // Removes last node from the subtree at this node, returns the removed element.
    // It should not be called on a leaf node.
    private fun Node<E>.removeLastFromNonLeafNode(): E {
        assert { !isLeaf }
        val right = right ?: run {
            val left = left!!
            assert { left.isLeaf && rank == 1 }
            val result = key
            key = left.key
            rank = 0
            this.left = null
            return result
        }
        val result = if (right.isLeaf) {
            this.right = null
            right.key
        } else {
            right.removeLastFromNonLeafNode()
        }
        rebalanceAfterRemoveIfNeeded(rt = true)
        TODO() // ^^^^ process rebalance result....
        return result
    }

    // Rebalances this node after removal of child if needed. Returns replacement of this node.
    //   rt = true : Removal in the right child
    //   rt = false: Removal in the left child
    // It should not be called on a leaf node.
    private fun Node<E>.rebalanceAfterRemoveIfNeeded(rt: Boolean): Node<E> {
        assert { !isLeaf }
        val p = this[rt]
        if (rank > p.rank + 2) return rebalanceAfterRemove(rt, p)
        return this
    }

    // Rebalances this node after removal of child. Returns replacement of this node.
    //   rt = true : Removal in the right child
    //   rt = false: Removal in the left child
    // It should not be called on a leaf node.
    private fun Node<E>.rebalanceAfterRemove(rt: Boolean, p: Node<E>?): Node<E> {
        assert { !isLeaf && this[rt] === p && rank == p.rank + 3 }
        val q = this[!rt]!!
        val dq = rank - q.rank
        if (dq == 2) {
            //  Note: the picture is for rt = false
            //       [this]
            //      / 3   \ 2
            //     [p]     [q]
            rank-- // demote this node
            //       [this]
            //      / 2   \ 1
            //     [p]     [q]
            return this
        }
        assert { dq == 1 }
        val r = q[rt]
        val s = q[!rt]
        //  Note: the picture is for rt = false
        //       [this]
        //      / 3   \ 1
        //     [p]     [q]
        //             /  \
        //          [r]   [s]
        val dr = q.rank - r.rank
        val ds = q.rank - s.rank
        assert { dr in 1..2 && ds in 1..2 }
        if (dr == 2 && ds == 2) {
            //       [this]
            //      / 3   \ 1
            //     [p]     [q]
            //           2 /  \ 2
            //          [r]   [s]
            rank-- // demote this node
            //       [this]
            //      / 2   \ 0
            //     [p]     [q]
            //           2 /  \ 2
            //          [r]   [s]
            q.rank-- // demote q node
            //       [this]
            //      / 2   \ 1
            //     [p]     [q]
            //           1 /  \ 1
            //          [r]   [s]
            return this
        }
        if (ds == 1) {
            //       [this]
            //      / 3   \ 1
            //     [p]     [q]
            //        1,2 /  \ 1
            //          [r]   [s]
            rotate(rt)
            //         [q]
            //    -1  /   \  1
            //   [this]    [s]
            //  3 /   \ 2,3
            //  [p]   [r]
            q.rank++
            rank--
            //         [q]
            //     1  /   \  2
            //   [this]    [s]
            //  2 /   \ 1,2
            //  [p]   [r]
            return q
        }
        assert { dr == 1 && ds == 2 }
        TODO()
    }

    // Rotates the node, rt shows direction of rotation
    // rt = true : left child is rotated to the right (as shown in the picture)
    // rt = false: right child is rotated to the left (mirror image)
    // --- BEFORE in rt = true ----
    //       [this]
    //      /    \
    //     [p]     [q]
    //   /  \
    //  [r]   [s]
    // --- AFTER in rt = true ----
    //       [p]
    //      /   \
    //    [r]    [this]
    //          /     \
    //         [s]    [q]
    private fun <E> Node<E>.rotate(rt: Boolean) {
        val p = this[!rt]!!
        val s = p[rt]
        p[rt] = this
        this[!rt] = s
    }
    
    private inline fun assert(cond: () -> Boolean) {
        check(cond())
    }
}

