package edu.hkbu.comp4035.y2017.jc;

import java.io.Serializable;
import java.util.Vector;

// TODO: javadocs for BTreeNode<T>
public abstract class BTreeNode implements Serializable {
    private final BTree tree;
    private final Vector<Integer> keys;
    @Deprecated
    private BTreeNode father;

    // TODO: more ctors

    BTreeNode(BTree tree) {
        this(tree, null);
    }

    BTreeNode(BTree tree, @Deprecated BTreeNode father) {
        this.tree = tree;
        this.father = father;
        keys = new Vector<>();
    }

    public abstract boolean isLeaf();

    boolean addKey(int key) {
        boolean ok = !isKeysFull();
        if (ok) {
            ok = this.keys.add(key);
        }
        return ok;
    }

    private boolean isKeysFull() {
        return this.keys.size() >= t() - 1;
    }

    /* tricky methods goes below */

    int t() {
        return this.tree.getProperties().getDegree();
    }

    @Deprecated
    protected  BTreeNode god() {
        BTreeNode god = this;
        while (god.father() != null) {
            god = god.father();
        }
        return god;
    }

    @Deprecated
    protected BTreeNode father() {
        return father;
    }

    @Deprecated
    protected void father(BTreeNode father) {
        this.father = father;
    }

    protected BTreeNode predecessor() {
        throw new UnsupportedOperationException("not yet implemented.");
        // TODO: get its idx from father(), if val < 0 even father unless most father is root, else get its idx - 1
    }

    protected BTreeNode successor() {
        throw new UnsupportedOperationException("not yet implemented.");
        // TODO: get its idx from father(), if val > (2 * t) even father unless most father is root, else get its idx + 1
    }
}
