package com.algorithms.parallel;

/**
 * Created by amazaspshaumyan on 4/26/14.
 *
 * This code shows construction of balanced binary search tree
 * from sorted array. We use fork/join parallelism model to take
 * advantage of several processors (for small inputs program will
 * still be using only single thread)
 *
 * In solution we used two simple helper classes : Node and BinarySearchTreeHelper
 *
 */


import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;


class ParallelBuild<T> extends RecursiveTask<Node<T>>{

    T[] x;
    int lo, hi;

    public ParallelBuild(T[] x, int lo, int hi){
        this.x = x; this.lo = lo; this.hi = hi;
    }

    public static <T> Node<T> computeSeq(T[] x, int lo, int hi){
        if(hi == lo) return new Node<>(x[lo],1);
        if(hi < lo) return null;
        int mid = lo+(hi-lo)/2;
        Node<T> current = new Node<>(x[mid],hi-lo+1);
        current.left = computeSeq(x, lo, mid);
        current.right = computeSeq(x, mid + 1, hi);
        return current;
    }

    @Override
    public Node<T> compute(){
        if(hi-lo+1<ParallelConstructBST.CUT_SEQUENTIAL){
            return computeSeq(x,lo,hi);
        }
        int mid = lo+(hi-lo)/2;
        Node<T> current = new Node<>(x[mid], hi-lo+1);
        ParallelBuild<T> left = new ParallelBuild<>(x,lo,mid);
        ParallelBuild<T> right = new ParallelBuild<>(x,lo,mid);
        left.fork(); // spawn new thread
        current.right = right.compute(); // process right subtree in current thread
        current.left = left.join(); // after completion spawned thread returns Node (see RecursiveTask class doc )
        return current;
    }

}



public class ParallelConstructBST{

    public static ForkJoinPool fjpl = new ForkJoinPool();
    public static int CUT_SEQUENTIAL= 5; // threshold for switching to sequential run ()


    public static <T> BinarySearchTreeHelper<T> construct(T[] x){
        Node<T> rootNode = fjpl.invoke( new ParallelBuild<>(x,0,x.length-1));
        return new BinarySearchTreeHelper<>(rootNode);
    }


    public static void main(String[] args){
        Integer[] x = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        BinarySearchTreeHelper<Integer> bst = construct(x);
        System.out.println(bst.root);
        System.out.println(bst.root.left.data);
        System.out.println(bst.root.right.data);
    }





}

