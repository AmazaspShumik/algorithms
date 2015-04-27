package com.algorithms.parallel;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.Comparator;


/**
 * Created by amazaspshaumyan on 8/20/14.
 *
 * Multithreaded algorithm for checking whether binary tree is
 * BST, uses standard Fork/Join paradigm.
 *
 * Perfomance:
 *             Work = O(n)
 *             Span = O(log(n))
 *             Parallelism = O(n/log(n)) (exponential parallelism)
 *
 * In solution we used two simple helper classes : Node and BinarySearchTreeHelper
 *
 */

class CheckBST<T> extends RecursiveTask<Boolean>{

    Node<T> node;
    T min,max;
    Comparator<T> comp;

    public CheckBST(Node<T> node, T min, T max, Comparator<T> comp){
        this.node = node; this.min = min; this.max = max;
        this.comp = comp;
    }

    public static <T> Boolean checkSeqBST(Node<T> x, T min, T max, Comparator<T> comp){
        if(x==null) return true;
        if(min!=null && comp.compare(x.data,min) <= 0) return false; //for BSTs that do not allow duplicates
        if(max!=null && comp.compare(x.data,max) >= 0) return false;
        return checkSeqBST(x.left,min,x.data,comp) && checkSeqBST(x.right,x.data,max,comp);
    }


    @Override
    public Boolean compute() {
        if (node.N < IsBST.CUT_SEQUENTIAL) return checkSeqBST(node, min, max, comp);
        if (min != null && comp.compare(node.data, min) <= 0) return false;
        if (max != null && comp.compare(node.data, max) >= 0) return false;
        CheckBST<T> left = new CheckBST<>(node.left, min, node.data, comp);
        CheckBST<T> right = new CheckBST<>(node.right, node.data, max, comp);
        left.fork(); // spaw new thread
        return right.compute() && left.join(); // note left.join()&&right.compute() would ruin parallelism
    }
}


public class IsBST{


    public static ForkJoinPool fjp = new ForkJoinPool();
    public static int CUT_SEQUENTIAL = 3; //


    public static <T> boolean isBST(BinarySearchTreeHelper<T> bst){
        return isBST(bst, new DefaultComparator<>());
    }


    public static <T> boolean isBST(BinarySearchTreeHelper<T> bst, Comparator<T> comp){
        return isBST(bst.root,comp);
    }

    private static <T> boolean isBST(Node<T> x, Comparator<T> comp){
        return fjp.invoke(new CheckBST<>(x, null, null, comp));
    }

    public static void main(String[] args){

    }



}