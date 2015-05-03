package com.algorithms.parallel;

import java.util.Queue;
import java.util.ArrayDeque;

public class BinarySearchTreeHelper<T>{

    Node<T> root;

    public BinarySearchTreeHelper(Node<T> root){
        this.root = root;
    }

    public void printTree(){
        Queue<Node<T>> q = new ArrayDeque<Node<T>>();
        q.add(root);
        while(!q.isEmpty()){
            Node<T> x = q.poll();
            System.out.print(x.data.toString()+" ");
            if(x.left!=null) q.add(x.left);
            if(x.right!=null) q.add(x.right);
        }
    }

}
