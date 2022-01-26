package org.example.poj.leetcode.queuestack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CloneGraph {
    private class Node {
        public int val;
        public List<Node> neighbors;
        public Node() {
            val = 0;
            neighbors = new ArrayList<Node>();
        }
        public Node(int _val) {
            val = _val;
            neighbors = new ArrayList<Node>();
        }
        public Node(int _val, ArrayList<Node> _neighbors) {
            val = _val;
            neighbors = _neighbors;
        }
    }

    public Node cloneGraph(Node node) {
        return cloneGraph(node, new HashMap<Integer, Node>());
    }

    public Node cloneGraph(Node node, HashMap<Integer, Node> visited) {
        if (node == null) {
            return null;
        }
        if (visited.containsKey(node.val)) {
            return visited.get(node.val);
        }
        Node node1 = new Node(node.val, new ArrayList<>());
        visited.put(node1.val, node1);
        for (Node node2 : node.neighbors) {
            node1.neighbors.add(cloneGraph(node2, visited));
        }
        return node1;
    }

    public static void main(String[] args) {

    }
}
