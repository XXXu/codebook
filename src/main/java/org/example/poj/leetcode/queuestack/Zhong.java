package org.example.poj.leetcode.queuestack;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Zhong {
//    List<Integer> zhongList = new ArrayList<>();
    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }
    public List<Integer> inorderTraversal(TreeNode root) {
        Stack<TreeNode> stack = new Stack<>();
        List<Integer> zhongList = new ArrayList<>();
        while (root != null || !stack.isEmpty()) {
            while (root!= null) {
                stack.push(root);
                root = root.left;
            }
            TreeNode pop = stack.pop();
            zhongList.add(pop.val);
            root = pop.right;
        }
        return zhongList;
    }

    /*public List<Integer> inorderTraversal(TreeNode root) {
        if (root == null) {
            return zhongList;
        }
        inorderTraversal(root.left);
        zhongList.add(root.val);
        inorderTraversal(root.right);
        return zhongList;
    }*/
}
