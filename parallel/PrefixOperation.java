package com.algorithms.parallel;

/**
 * Created by amazaspshaumyan on 5/2/15.
 */


import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.IntBinaryOperator;

class PrefixIntBinaryTree{

    int acc;
    int lo,hi;
    PrefixIntBinaryTree left, right;

    public PrefixIntBinaryTree( int lo, int hi, int acc){
        this.lo = lo; this.hi = hi; this.acc = acc;
    }

    public PrefixIntBinaryTree( int lo, int hi){
        this(lo,hi,0);
    }
}


class PrefixBinaryTreeConstruct extends RecursiveTask<PrefixIntBinaryTree>{

    int lo, hi;
    int[] arr;
    IntBinaryOperator op;


    public PrefixBinaryTreeConstruct(int lo, int hi, int[] arr, IntBinaryOperator op){
        this.lo = lo; this.hi = hi; this.arr = arr; this.op = op;
    }

    public PrefixIntBinaryTree seqPrefixBinaryTree(){
        int acc = arr[lo];
        for(int i = lo+1; i < hi; i++)
            acc = op.applyAsInt(acc,arr[i]);
        return new PrefixIntBinaryTree(lo,hi,acc);
    }

    @Override
    public PrefixIntBinaryTree compute(){
        if(hi-lo < PrefixOperation.CUT_SEQ){
            return seqPrefixBinaryTree();
        }
        int mid = lo + (hi-lo)/2;
        PrefixBinaryTreeConstruct left = new PrefixBinaryTreeConstruct(lo,mid,arr,op);
        PrefixBinaryTreeConstruct right = new PrefixBinaryTreeConstruct(mid,hi,arr,op);
        left.fork();
        PrefixIntBinaryTree current = new PrefixIntBinaryTree(lo,hi);
        current.right = right.compute();
        current.left = left.join();
        current.acc = op.applyAsInt(current.left.acc, current.right.acc);
        return current;
    }
}


class PrefixBinaryOperationTree extends RecursiveAction{

    PrefixIntBinaryTree bt;
    Integer offset;
    int[] arr;
    IntBinaryOperator op;

    public PrefixBinaryOperationTree(PrefixIntBinaryTree bt, Integer offset, int[] arr, IntBinaryOperator op){
        this.bt = bt; this.offset = offset; this.op = op; this.arr = arr;
    }

    public void seqPrefixOpTree(){
        int lo = bt.lo; int hi = bt.hi;
        for(int i = lo; i < hi-1; i++)
            arr[i + 1] = op.applyAsInt(arr[i + 1], arr[i]);
        int add;
        if(offset!=null) {
            add = offset;
            arr[lo] = op.applyAsInt(arr[lo], add);
        } else add = arr[lo];
        for (int i = lo+1; i < hi; i++)
            arr[i] = op.applyAsInt(arr[i], add);
    }

    @Override
    public void compute(){
        if(bt==null) return;
        if(bt.hi-bt.lo<PrefixOperation.CUT_SEQ){
            seqPrefixOpTree();
        }
        Integer leftOffset=null;
        if(bt.left!=null && offset!=null) leftOffset = op.applyAsInt(bt.left.acc,offset);
        else if(offset!=null && bt.left==null) leftOffset = offset;
        else if(offset==null && bt.left!=null) leftOffset = bt.left.acc;
        PrefixBinaryOperationTree left = new PrefixBinaryOperationTree(bt.left,offset,arr,op);
        PrefixBinaryOperationTree right = new PrefixBinaryOperationTree(bt.right, leftOffset, arr, op);
        left.fork();
        right.compute();
    }
}


public class PrefixOperation{

    public static ForkJoinPool fjp = new ForkJoinPool();
    public static int CUT_SEQ =100;

    public static void prefix(int[] arr, IntBinaryOperator op){
        PrefixIntBinaryTree bt = fjp.invoke(new PrefixBinaryTreeConstruct(0,arr.length,arr,op));
        System.out.println(bt.acc);
        System.out.println(Arrays.toString(arr));
        fjp.invoke(new PrefixBinaryOperationTree(bt,null,arr,op));
    }

    public static void main(String[] args) {
        int[] x = {1,2,3,4,5,6,7,8,9,10};
        prefix(x, (z, y) -> Math.max(z,y));
        System.out.println(Arrays.toString(x));
    }

}
