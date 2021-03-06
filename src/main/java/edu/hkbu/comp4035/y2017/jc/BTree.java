package edu.hkbu.comp4035.y2017.jc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;

// TODO: Javadoc for class BTree
public class BTree<VType> implements Serializable {
    private final Class<VType> valueType;
    // Note: you cannot confirm what key type will be before completion of invoking ctor.
    private BTreeNode rootNode;
    private BTreeProperties properties;

    /**
     * Builds a B+Tree with an empty root <b>leaf</b> node, and value of {@code CharSequence}s.
     * This will create a new empty leaf node object and be put
     * its object reference to BTree as the root node for further insertion.<p>
     * This operation coses O(1) time.
     */
    public static <CSType extends CharSequence> BTree<CSType> createBTreeCharSequence(
            Class<CSType> valueType, BTreeProperties properties) {
        return new BTree<>(valueType, properties);
    }

    /**
     * Builds a B+Tree with an empty root <b>leaf</b> node, and value of {@code Number}s.
     * This will create a new empty leaf node object and be put
     * its object reference to BTree as the root node for further insertion.<p>
     * This operation coses O(1) time.
     */
    public static <NumType extends Number> BTree<NumType> createBTreeNumber(
            Class<NumType> valueType, BTreeProperties properties) {
        return new BTree<>(valueType, properties);
    }

    private BTree(Class<VType> valueType, BTreeProperties properties) {
        this.rootNode = new BTreeLeafNode<VType>(this);
        this.properties = properties;
        this.valueType = valueType;
    }

    BTree(Class<VType> valueType, BTreeProperties properties, BTreeNode rootNode) {
        this(valueType, properties);
        this.rootNode = rootNode;
    }

    /**
     * The destructor of BTree just "closes" the index. This includes de-allocating
     * the memory space for the index. Note that it does not delete the file.
     */
    // TO DO: This method is not guaranteed to be invoked by the GC.
    // TO DO: Use try-finally if possible
    // See: http://stackoverflow.com/questions/171952/is-there-a-destructor-for-java
//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
//    }

    /**
     * Takes in a filename, and checks if a file with that name already exists.
     * If the file exists, we "open" the file and build an initial B+-tree based on the key values in the file.
     * Otherwise, we return an error message and the program terminates.
     */
    public static BTree importJsonFromFilePath(String pathname) throws IOException, ClassNotFoundException {
        File file = new File(pathname);
        if (!file.isFile()) {
            throw new FileNotFoundException("404: " + pathname);
        }
        String strJson = StringUtils.join(Files.readAllLines(file.toPath()), "");
        return BTreeDumpJsonFormat.Parse.toBTree(strJson);
    }

    public String exportJson() {
        return BTreeDumpJsonFormat.Stringfy.fromBTree(this);
    }

    public int getHeight() {
        return BTreeStatValidator.doMeasureHeight(this);
    }

    BTreeNode getRootNode() {
        return rootNode;
    }

    void setRootNode(BTreeNode rootNode) {
        this.rootNode = rootNode;
    }

    /**
     * This method inserts a pair ({@code key}, {@code rid}) into the B+-Tree Index ({@code rid} can always be assumed
     * to be {@code 0} in your implementation). The actual pair ({@code key}, {@code rid}) is inserted into a leaf node.
     * But this insertion may cause one or more ({@code key}, {@code rid}) pair to be inserted into index nodes. You
     * should always check to see if the current node has enough space before you insert. If you don't have enough
     * space, you have to split the current node by creating a new node, and copy some of the data over from the current
     * node to the new node. Splitting will cause a new entry to be added in the parent node.<br/>
     * <br/>
     * Splitting of the root node should be considered separately, since if we have a new root, we need to update the
     * root pointer to reflect the changes. Splitting of a leaf node should also be considered separately since the leaf
     * nodes are linked as a link list.<br/>
     * <br/>
     * Due to the complexity of this function, we recommend that you write separate functions for different cases. For
     * example, it is a good idea to write a function to insert into a leaf node, and a function to insert into an index
     * node.
     * @param key Key for identifying the data to be stored on the tree indexing system.
     * @param rid The data.
     * @return The current operating {@code BTree} object for further chain operation.
     */
    public BTree insert(int key, VType rid) {
        // P.S.: This statement might update the root due to the push/copy-up step during insertion.
        BTreeInsertOperations2.doInsert(this, key, rid);
        return this; // chain operation like jQuery
    }

    /**
     * This method deletes an entry ({@code key}, {@code rid}) from a leaf node. Deletion from a leaf node may cause one
     * or more entries in the index node to be deleted. You should always check if a node underflows (less than 50% full)
     * after deletion. If a node becomes underflows, merging or redistribution will occur (read and implement the
     * algorithm in the notes).<br/>
     * <br/>
     * You should consider different scenarios separately (maybe write separate functions for them). You should consider
     * deletion from a leaf node and index node separately. Deletion from the root should also be considered separately.
     * @param key Key for identifying the data, which its reference to be removed from the tree indexing system.
     * @return The current operating {@code BTree} object for further chain operation.
     */
    public BTree delete(int key) {
    	   BTreeDeleteOperations.doDelete(this, key);
        
        return this; // chain operation like jQuery
    }
    public BTree delete(int key,int key2) {
 	   BTreeDeleteOperations.doDelete(this, key,key2);
     
     return this; // chain operation like jQuery
 }


    /**
     * This method implements range queries. Given a search range ({@code key1}, {@code key2}), the method returns all
     * the qualifying key values in the range of between {@code key1} and {@code key2} in the B+-tree. If such keys are
     * not found, it returns nothing. <i><b>Be careful with the duplicate keys that span over multiple pages.</b></i>
     * @param key1 The first key which may identify a data reference.
     * @param key2 The second key which may identify another data reference.
     * @return The collection of {@code Integers}s of keys which are within range between {@code key1} and {@code key2}
     * <u>inclusively</u>.
     */
    public Collection<Integer> search(int key1, int key2) {
        if (key2 < key1) {
            System.out.println("key1 > key2! Flipped but be careful.");
            int i = key2;
            key2 = key1;
            key1 = i;
        }

        return BTreeSearchOperations.doRangeSearchInclusive(this, key1, key2);
    }

    /**
     * In this method, you are required to collect statistics to reflect the performance of your B+- Tree implementation.
     * This method should print out the following statistics of your B+-Tree.<br/>
     * <ol>
     *     <li>Total number of nodes in the tree</li>
     *     <li>Total number of data entries in the tree</li>
     *     <li>Total number of index entries in the tree</li>
     *     <li>Average fill factor (used space/total space) of the nodes.</li>
     *     <li>Height of tree</li>
     * </ol>
     * These statistics should serve you in making sure that your code executes correctly. For example, the fill factor
     * of each node should be greater than {@code 0.5}. You should make sure that DumpStatistics performs this operation.
     * @return An <i>immutable</i> B+ Tree statistics object which records the status of the B+ Tree on the moment.
     * @see <a href="http://stackoverflow.com/questions/279507/what-is-meant-by-immutable">java - What is meant by immutable? - Stack Overflow</a>
     */
    public BTreeStatValidator.Statistics dumpStatistics() {
        return BTreeStatValidator.generate(this);
    }

    /**
     * These are helper functions that should help you debug, by showing the tree contents.
     * {@code PrintTree} must be implemented and {@code PrintNode} is optional.
     * @param ps The {@code PrintStream} to be used as the destination of text output, for example, "{@code System.out}".
     */
    public void printTree(PrintStream ps) {
        ps.println(BTreePrinter.doPrintAsString(this));
    }

    @SuppressWarnings("ConstantConditions")
    public void printNode(PrintStream ps, int nodeKey) {
        BTreeSearchOperations.SearchNodeResult<VType> result = BTreeSearchOperations.doSearchNodeByKey(this, nodeKey, true);
        BTreeNode parent = result == null ? null : result.getParentNode();
        System.out.printf("Search Key: %d\n\nResult: %s\n\nParent Node: %s\n",
                nodeKey,
                result == null ? "null" : "\n" + BTreePrinter.doPrintNodeAsString(result.getDestinationNode()),
                parent == null ? "null" : BTreePrinter.doPrintNodeFriendlyNameAsString(parent) + "@" +
                        Integer.toHexString(parent.hashCode()) + ": " + BTreePrinter.doPrintNodeKeysAsString(parent));
    }

    public BTreeProperties getProperties() {
        return properties;
    }

    boolean isValueMatchActualType(Object value) {
        return value == null || getValueType().isInstance(value); // cannot check if value is already null
    }

    Class<VType> getValueType() {
//        try {
//            Type gs = getClass().getGenericSuperclass();
//            String className = ((ParameterizedType) gs).getActualTypeArguments()[0].getTypeName();
//            Class<?> clazz = Class.forName(className);
//            //noinspection unchecked
//            return (Class<VType>) clazz;
//        } catch (Exception e) {
//            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
//        }
        return valueType;
    }

    String getHashString() {
        return Integer.toHexString(hashCode());
    }
}
