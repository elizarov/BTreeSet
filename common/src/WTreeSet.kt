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
        get() {
            assert { if (left == null && right == null) rank == 0 else true }
            return rank == 0
        }

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
        root?.addImpl(element)?.let { return it }
        root = Node(element)
        return true
    }

    // Adds element to the subtree at this node
    private fun Node<E>.addImpl(element: E): Boolean {
        val c = comparator.compare(element, key)
        val rt = c > 0
        var p = when {
            rt -> right
            c < 0 -> left
            else -> return false
        }
        if (p == null) {
            p = Node(element)
            this[rt] = p
        } else {
            if (!p.addImpl(element)) return false
        }
        if (rank <= p.rank) rebalanceAfterAdd(rt, p)
        return true
    }

    private fun Node<E>.rebalanceAfterAdd(rt: Boolean, p: Node<E>) {
        assert { this[rt] === p && rank == p.rank }
        val q = this[!rt]
        val dq = rank - q.rank
        if (dq == 1) { // this is 0,1 node
            rank++ // promote this node to 1,2
            return
        }
        // this is 0,2 node
        assert { dq == 2 }
        val r = p[rt]
        if (p.rank == r.rank + 1) {
            //  Note: the picture is for rt = false
            //         [t]<-this
            //      / 0    \ 2
            //     [p]<-p   [q]
            //  1 /  \ 2
            //  [r]   [s]
            assert { p.rank == p[!rt].rank + 2 }
            rotate(!rt)
            //        [p]<-this
            //    1 /    \ 0
            //    [r]      [t]<-p
            //         2 /     \ 2
            //          [s]    [q]
            p.rank-- // demote p
            //        [p]<-this
            //    1 /    \ 1
            //    [r]      [t]<-p
            //         1 /     \ 1
            //          [s]    [q]
        } else {
            //  Note: the picture is for rt = false
            //          [t]<-this
            //      / 0     \ 2
            //     [p]<-p   [q]
            //  2 /  \ 1
            //  [r]   [s]<-s
            //   1,2 /  \ 1,2
            //      [b] [c]
            val s = p[!rt]!!
            assert { p.rank == r.rank + 2 }
            assert { p.rank == s.rank + 1 }
            p.rotate(rt)
            //              [t]<-this
            //         1 /       \ 2
            //         [s]<-p    [q]
            //    -1 /    \ 1,2
            //     [p]<-s  [c]
            //  2 /  \ 2,3
            //  [r]  [b]
            rotate(!rt)
            //             [s]<-this
            //     -1  /          \ -1
            //     [p]<-s          [t]<-p
            // 2  /   \ 2,3   2,3 /   \ 2
            //  [r]   [b]      [c]      [q]
            rank++
            s.rank--
            p.rank--
            //             [s]<-this
            //      1  /          \  1
            //     [p]<-s          [t]<-p
            // 1  /   \ 1,2   1,2 /   \ 1
            //  [r]   [b]      [c]      [q]
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
            if (left == null) {
                rank = 0
                return right.key // node became leaf -- no further rebalance needed
            }
            right.key
        } else {
            right.removeLastFromNonLeafNode()
        }
        rebalanceAfterRemoveIfNeeded(rt = true)
        return result
    }

    // Rebalances this node after removal of child if needed. Returns replacement of this node.
    //   rt = true : Removal in the right child
    //   rt = false: Removal in the left child
    private fun Node<E>.rebalanceAfterRemoveIfNeeded(rt: Boolean) {
        val p = this[rt]
        if (rank > p.rank + 2) rebalanceAfterRemove(rt, p)
    }

    // Rebalances this node after removal of child.
    //   rt = true : Removal in the right child
    //   rt = false: Removal in the left child
    // It should not be called on a leaf node.
    private fun Node<E>.rebalanceAfterRemove(rt: Boolean, p: Node<E>?) {
        assert { !isLeaf && this[rt] === p && rank == p.rank + 3 }
        val q = this[!rt]!!
        val dq = rank - q.rank
        if (dq == 2) {
            //  Note: the picture is for rt = false
            //        [t]<-this
            //      / 3   \ 2
            //     [p]     [q]
            rank-- // demote this node
            //        [t]<-this
            //      / 2   \ 1
            //     [p]     [q]
            return
        }
        assert { dq == 1 }
        val r = q[rt]
        val s = q[!rt]
        //  Note: the picture is for rt = false
        //        [t]<-this
        //      / 3   \ 1
        //     [p]     [q]
        //             /  \
        //          [r]   [s]
        val dr = q.rank - r.rank
        val ds = q.rank - s.rank
        assert { dr in 1..2 && ds in 1..2 }
        if (dr == 2 && ds == 2) {
            //        [t]<-this
            //      / 3   \ 1
            //     [p]     [q]
            //           2 /  \ 2
            //          [r]   [s]
            rank-- // demote this node
            //        [t]<-this
            //      / 2   \ 0
            //     [p]     [q]
            //           2 /  \ 2
            //          [r]   [s]
            q.rank-- // demote q node
            //        [t]<-this
            //      / 2   \ 1
            //     [p]     [q]
            //           1 /  \ 1
            //          [r]   [s]
            return
        }
        if (ds == 1) {
            //        [t]<-this
            //      / 3   \ 1
            //     [p]     [q]<-q
            //        1,2 /  \ 1
            //          [r]   [s]
            rotate(rt)
            //         [q]<-this
            //    -1  /   \  1
            //    [t]<-q  [s]
            //  3 /   \ 2,3
            //  [p]   [r]
            rank++
            q.rank--
            //         [q]<-this
            //     1  /   \  2
            //    [t]<-q  [s]
            //  2 /   \ 1,2
            //  [p]   [r]
            if (p == null && r == null) q.rank = 0 // correct leaf invariant
            return
        }
        assert { dr == 1 && ds == 2 }
        //        [t]<-this
        //      / 3   \ 1
        //     [p]     [q]<-q
        //           1 /  \ 2
        //          [r]<-r [s]
        //     1,2 /  \ 1,2
        //       [a]  [b]
        q.rotate(!rt)
        //        [t]<-this
        //      / 3   \ 2
        //     [p]     [r]<-q
        //        1,2 /  \ -1
        //          [a]   [q]<-r
        //           2,3 /  \ 2
        //             [b]   [s]
        rotate(rt)
        //         [r]<-this
        //   -2 /         \ -1
        //     [t]<-q      [q]<-r
        //  3 /  \ 3,4 2,3 /  \ 2
        //  [p]  [a]     [b]  [s]
        q.rank -= 2
        r!!.rank--
        rank++
        //         [r]<-this
        //    1 /         \ 1
        //     [t]<-q      [q]<-r
        //  1 /  \ 1,2 1,2 /  \ 1
        //  [p]  [a]     [b]  [s]

    }

    // Rotates the node, rt shows direction of rotation
    // rt = true : left child is rotated to the right (as shown in the picture)
    // rt = false: right child is rotated to the left (mirror image)
    // Note, that p & this node keys and ranks get swapped
    // --- BEFORE in rt = true ----
    //         [t]<-this
    //      /      \
    //     [p]<-p   [q]
    //   /  \
    //  [r]   [s]
    // --- AFTER in rt = true ----
    //       [p]<-this
    //      /   \
    //    [r]    [t]<-p
    //         /     \
    //        [s]    [q]
    private fun <E> Node<E>.rotate(rt: Boolean) {
        val p = this[!rt]!!
        val q = this[rt]
        val r = p[!rt]
        val s = p[rt]
        this[!rt] = r
        this[rt] = p
        p[!rt] = s
        p[rt] = q
        key = p.key.also { p.key = key }
        rank = p.rank.also { p.rank = rank }
    }
    
    private inline fun assert(cond: () -> Boolean) {
        check(cond())
    }
}

