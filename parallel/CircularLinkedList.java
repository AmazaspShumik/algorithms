package com.algorithms.parallel;

/**
 * Helper class for ConvertBSTSortedLinkedList class.
 */

public class CircularLinkedList<T>{

    Node<T> min;
    Node<T> max;
    int N;

    public CircularLinkedList(Node<T> x){
        this.min = x;
        this.max = x;
        max.right = min;
        min.left = max;
        this.N = 1;
    }

    public static <T> CircularLinkedList<T> CircularLinkedListMerge(CircularLinkedList<T> ListOne, CircularLinkedList<T> ListTwo){
        ListTwo.max.right = ListOne.min;
        ListOne.min.left = ListTwo.max;
        ListOne.max.right = ListTwo.min;
        ListTwo.min.left = ListOne.max;
        ListTwo.min = ListOne.min;
        ListTwo.N+=ListOne.N;
        return ListTwo;
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        Node<T> node = min;
        for(int i=0; i < N; i++){
            str.append(node.data.toString());
            node = node.right;
            str.append("->");
        }
        return str.toString();
    }

    public static void main(String[] args){

    }
}
