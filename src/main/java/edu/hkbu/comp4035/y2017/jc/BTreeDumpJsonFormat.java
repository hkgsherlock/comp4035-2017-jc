package edu.hkbu.comp4035.y2017.jc;

import com.google.gson.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

// Note: Classes and methods (if not static) are **final** to make sure that they won't be extended or being overriden.

/**
 * Contains the instructions to import/export JSON file from/to a B+ tree.
 */
final class BTreeDumpJsonFormat {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JsonParser parser = new JsonParser();

    // import

    /**
     * The parser instructions to import a JSON file to become a B+ tree.
     */
    static final class Parse {
        /**
         * Parse a JSON context as a B+ Tree.
         * @param content The JSON context as {@code String}.
         * @return The B+ tree.
         * @throws com.google.gson.JsonSyntaxException If the syntax of {@code content} is illegal.
         * @throws IOException If a node cannot be generated. See stack traces.
         * @throws ClassNotFoundException If the value type of B+ tree specified in JSON context cannot be found
         * to create.
         */
        static BTree toBTree(String content) throws com.google.gson.JsonSyntaxException, IOException, ClassNotFoundException {
            JsonObject jo = parser.parse(content).getAsJsonObject();

            Class<?> valueType = Class.forName(jo.get("valueType").getAsString());
            BTreeProperties properties = gson.fromJson(jo.get("properties"), BTreeProperties.class);
            //noinspection unchecked
            BTree bTree = new BTree(valueType, properties, null);

            BTreeNode rootNode = toNode(bTree, jo.get("rootNode").getAsJsonObject(), valueType);
            bTree.setRootNode(rootNode);

            reboundLeafNodes(bTree);

            return bTree;
        }

        // from btree do VType detection
        private static BTreeNode toNode(BTree tree, JsonObject jo, Class<?> leafNodeValueType) throws IOException {
            boolean leaf = jo.get("leaf").getAsBoolean();
            if (leaf) {
                return toLeafNode(tree, jo, leafNodeValueType);
            } else {
                return toIndexNode(tree, jo, leafNodeValueType);
            }
        }

        private static BTreeIndexNode toIndexNode(BTree tree, JsonObject jo, Class<?> leafNodeValueType) throws IOException {
            LinkedList<Integer> keys = new LinkedList<>();
            jo.get("keys").getAsJsonArray().forEach(e -> keys.add(e.getAsInt()));
            LinkedList<BTreeNode> subItems = new LinkedList<>();
            for (JsonElement e : jo.get("subItems").getAsJsonArray()) {
                subItems.add(toNode(tree, e.getAsJsonObject(), leafNodeValueType));
            }
            //noinspection unchecked
            return new BTreeIndexNode(tree, keys, subItems);
        }

        private static <LeafNodeValType> BTreeLeafNode<LeafNodeValType> toLeafNode(BTree tree, JsonObject jo, Class<LeafNodeValType> leafNodeValueType) throws IOException {
            LinkedList<Integer> keys = new LinkedList<>();
            jo.get("keys").getAsJsonArray().forEach(e -> keys.add(e.getAsInt()));
            LinkedList<LeafNodeValType> subItems = new LinkedList<>();
            for (JsonElement e : jo.get("subItems").getAsJsonArray()) {
                subItems.add(gson.fromJson(e, leafNodeValueType));
            }
            //noinspection unchecked
            return new BTreeLeafNode<LeafNodeValType>(tree, keys, subItems, null);
        }

        private static void reboundLeafNodes(BTree bTree) {
            Queue<BTreeNode> iterating = new LinkedList<>();
            iterating.add(bTree.getRootNode());
            Stack<BTreeLeafNode> leafNodes = new Stack<>();
            while (iterating.size() > 0) {
                BTreeNode node = iterating.remove();
                if (node.isLeaf()) {
                    leafNodes.add((BTreeLeafNode) node);
                } else {
                    iterating.addAll(((BTreeIndexNode) node).getSubItems());
                }
            }

            BTreeLeafNode reverseLastNode = null; // i.e. next node
            BTreeLeafNode l;
            for (l = leafNodes.pop(); !leafNodes.empty(); l = leafNodes.pop()) {
                //noinspection unchecked
                l.setNextLeaf(reverseLastNode);
                reverseLastNode = l;
            }
            //noinspection unchecked
            l.setNextLeaf(reverseLastNode);
        }
    }

    // export
    /**
     * The string-fy instructions to export a JSON file from a B+ tree.
     */
    static final class Stringfy {
        /**
         * String-fies a B+ tree as JSON.
         * @param bTree The B+ tree.
         * @return The JSON representing a B+ tree of {@code bTree}.
         */
        static String fromBTree(BTree bTree) {
            String strValueType = gson.toJson(bTree.getValueType().getName());
            String strProperties = gson.toJson(bTree.getProperties());
            //noinspection unchecked
            String strRootNode = fromNode(bTree.getRootNode(), bTree.getValueType());
            String tmpl = "{\"valueType\":%s,\"properties\":%s,\"rootNode\":%s}";
            String json = String.format(tmpl, strValueType, strProperties, strRootNode);
            return Prettify.fromJsonString(json);
        }

        private static <LeafNodeValType> String fromNode(BTreeNode node, Class<LeafNodeValType> leafNodeValueType) {
            if (node.isLeaf()) {
                //noinspection unchecked
                return fromLeafNode((BTreeLeafNode<LeafNodeValType>) node, leafNodeValueType);
            } else {
                return fromIndexNode((BTreeIndexNode) node, leafNodeValueType);
            }
        }

        private static String fromIndexNode(BTreeIndexNode in, Class<?> leafNodeValueType) {
            String strKeys = gson.toJson(in.getKeys());
            String strSubItems = in.getSubItems().stream().map(sn -> fromNode(sn, leafNodeValueType)).collect(Collectors.joining(", "));
            String tmpl = "{\"leaf\":false,\"keys\":%s,\"subItems\":[%s]}";
            return String.format(tmpl, strKeys, strSubItems);
        }

        private static <LeafNodeValType> String fromLeafNode(BTreeLeafNode<LeafNodeValType> ln, Class<LeafNodeValType> leafNodeValueType) {
            String strKeys = gson.toJson(ln.getKeys());
            String strSubItems = gson.toJson(ln.getSubItems().stream().map(sn -> sn == null ? null : gson.toJson(sn)).toArray(String[]::new));
            String tmpl = "{\"leaf\":true,\"keys\":%s,\"subItems\":%s}";
            return String.format(tmpl, strKeys, strSubItems);
        }
    }

    /**
     * Prettifies a JSON context.
     */
    private static class Prettify {
        /**
         * Prettifies a JSON context.
         * @param json The JSON context to beautify.
         * @return The prettified JSON context.
         */
        static String fromJsonString(String json) {
            return gson.toJson(parser.parse(json).getAsJsonObject());
        }
    }
}
