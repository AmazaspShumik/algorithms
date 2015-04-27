package com.algorithms.parallel;


public class Node<T>{

    T data;
    Node<T> left,right;
    int N; // subtree size

    public Node(T data, Node<T> left, Node<T> right, int N){
        this.data = data; this.left = left; this.right = right;
        this.N = N;
    }

    public Node(T data, int N){
        this(data,null,null,N);
    }

    public Node(T data){
        this(data,null,null,0);
    }


}
