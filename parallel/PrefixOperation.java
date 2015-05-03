package com.algorithms.parallel;

/**
 * Created by amazaspshaumyan on 5/10/14.
 *
 *
 * Parallel prefix operation on generic array.
 *
 *
 * First stage: (see PrefixBinaryTreeConstruct< class)
 *
 *      constructs binary tree where each Node holds value corresponding to prefix operation
 *      for given range. For instance if operation is sum then each node which corresponds
 *      to a specific range will have sum of all elements in range.
 *
 *
 *      Work: O(n)
 *      Span: O(log(n))
 *      Parallelism: O(n/log(n)) exponential parallelism
 *
 * Second stage: (see PrefixBinaryOperationTree class)
 *
 *      reads a binary tree and changes initial generic array according to
 *      prefix operation defined by user.
 *      For instance if operation is minimum, then result will be running minimum for array,
 *      if operation is sum, then cumulative sum etc.
 *
 *      Work: O(n)
 *      Span: O(log(n))
 *      Parallelism: O(n/log(n)) exponential parallelism
 *
 * Both parts of algorithm have span of O(log(n)) therefore overall span will be O(log(n))
 *
 *
 * Note binary operations can only be associative, for example:
 *   sum, min, max, multiply. (You can not use mean, or standard deviation).
 *
 */


import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.BinaryOperator;

/*
 creates binary serch tree for corresponfing prefix operation
 */
class PrefixBinaryTree<T>{

    T acc;
    int lo,hi;
    PrefixBinaryTree<T> left, right;

    public PrefixBinaryTree( int lo, int hi, T acc){
        this.lo = lo; this.hi = hi; this.acc = acc;
    }

    public PrefixBinaryTree( int lo, int hi){
        this(lo,hi,null);
    }
}


class PrefixBinaryTreeConstruct<T> extends RecursiveTask<PrefixBinaryTree<T>>{

    int lo, hi;
    T[] arr;
    BinaryOperator<T> op;


    public PrefixBinaryTreeConstruct(int lo, int hi, T[] arr, BinaryOperator<T> op){
        this.lo = lo; this.hi = hi; this.arr = arr; this.op = op;
    }

    public PrefixBinaryTree<T> seqPrefixBinaryTree(){
        T acc = arr[lo];
        for(int i = lo+1; i < hi; i++)
            acc = op.apply(acc,arr[i]);
        return new PrefixBinaryTree<>(lo,hi,acc);
    }

    @Override
    public PrefixBinaryTree<T> compute(){
        if(hi-lo < PrefixOperation.CUT_SEQ){
            return seqPrefixBinaryTree();
        }
        int mid = lo + (hi-lo)/2;
        PrefixBinaryTreeConstruct<T> left = new PrefixBinaryTreeConstruct<>(lo,mid,arr,op);
        PrefixBinaryTreeConstruct<T> right = new PrefixBinaryTreeConstruct<>(mid,hi,arr,op);
        left.fork();
        PrefixBinaryTree<T> current = new PrefixBinaryTree<>(lo,hi);
        current.right = right.compute();
        current.left = left.join();
        current.acc = op.apply(current.left.acc, current.right.acc);
        return current;
    }
}


class PrefixBinaryOperationTree<T> extends RecursiveAction{

    PrefixBinaryTree<T> bt;
    T offset;
    T[] arr;
    BinaryOperator<T> op;

    public PrefixBinaryOperationTree(PrefixBinaryTree<T> bt, T offset, T[] arr, BinaryOperator<T> op){
        this.bt = bt; this.offset = offset; this.op = op; this.arr = arr;
    }

    public void seqPrefixOpTree(){
        int lo = bt.lo; int hi = bt.hi;
        for(int i = lo; i < hi-1; i++)
            arr[i + 1] = op.apply(arr[i + 1], arr[i]);
        System.out.println(Arrays.toString(arr));
        T add;
        if(offset!=null) {
            add = offset;
            for(int i = lo; i < hi; i++)
                arr[i] = op.apply(arr[i],add);
        }
    }

    @Override
    public void compute(){
        if(bt==null) return;
        if(bt.hi-bt.lo<PrefixOperation.CUT_SEQ){
            seqPrefixOpTree();
        }
        T leftOffset;
        if(bt.left!=null && offset!=null) leftOffset = op.apply(bt.left.acc,offset);
        else if(offset!=null) leftOffset = offset;
        else if(bt.left!=null) leftOffset = bt.left.acc;
        else leftOffset = null;
        PrefixBinaryOperationTree<T> left = new PrefixBinaryOperationTree<>(bt.left,offset,arr,op);
        PrefixBinaryOperationTree<T> right = new PrefixBinaryOperationTree<>(bt.right, leftOffset, arr, op);
        left.fork();
        right.compute();
    }
}


public class PrefixOperation{

    public static ForkJoinPool fjp = new ForkJoinPool();
    public static int CUT_SEQ =3; // cutoff to sequential array, low threshold for testing purposes.

    public static <T> void prefix(T[] arr, BinaryOperator<T> op){
        PrefixBinaryTree<T> bt = fjp.invoke(new PrefixBinaryTreeConstruct<>(0,arr.length,arr,op));
        fjp.invoke(new PrefixBinaryOperationTree<>(bt,null,arr,op));
    }

    public static void main(String[] args) {

    }

}
