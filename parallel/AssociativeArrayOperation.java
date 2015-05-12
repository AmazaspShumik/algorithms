package com.algorithms.parallel;

/**
 * Created by amazaspshaumyan
 *
 * Parallel associative array operation implemented with the
 * help of Java Fork/Join.
 *
 * Examples of associative operations include sum,min,max,product etc.
 *
 * Perfomance:
 *          Work: O(n)
 *          Span: O(log(n))
 *          Parallelism: O(n/log(n)) (exponential parallelism)
 */


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;

class AssociativeOperation<T> extends RecursiveTask<T>{

    int lo,hi;
    T[] data;
    BinaryOperator<T> op;

    public AssociativeOperation(int lo, int hi, T[] data, BinaryOperator<T> op){
        this.lo = lo; this.hi = hi; this.data = data;
        this.op = op;
    }

    public static <T> T seqAssociativeOp(T[] data, int lo, int hi, BinaryOperator<T> op){
        if(hi<lo) return null;
        T start = data[lo];
        for(int i = lo+1; i <= hi; i++){
            start = op.apply(start,data[i]);
        }
        return start;
    }

    @Override
    public T compute(){
        if(hi-lo < AssociativeArrayOperation.CUTSEQ) return seqAssociativeOp(data,lo,hi,op);
        int mid = lo + (hi-lo)/2;
        AssociativeOperation<T> leftOperation = new AssociativeOperation<>(lo,mid,data,op);
        AssociativeOperation<T> rightOperation = new AssociativeOperation<>(mid+1,hi,data,op);
        leftOperation.fork();
        T rightVal = rightOperation.compute();
        T leftVal = leftOperation.join();
        return op.apply(leftVal,rightVal);
    }
}

public class AssociativeArrayOperation {

    public static ForkJoinPool fjp = new ForkJoinPool();
    public static int CUTSEQ = 5;

    public static <T> T associativeOperation(T[] input, BinaryOperator<T> op, int lo, int hi){
        return fjp.invoke(new AssociativeOperation<>(lo,hi,input,op));
    }


    // testing client
    public static void main(String[] args){
        Integer[] testDataInteger = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        Double[] testDataDouble = {0.0,2.0,3.0};
        String[] testDataString = {"Test","for", "operation","with","strings"};
        BinaryOperator<Integer> op_sum_int = (z,v)->z+v;
        BinaryOperator<Integer> op_min_int = (z,v)->Math.min(z,v);
        BinaryOperator<Integer> op_prod_int = (z,v)->z*v;
        BinaryOperator<Double> op_sum_double = (z,v)->z+v;
        BinaryOperator<Double> op_min_double = (z,v)->Math.min(z,v);
        BinaryOperator<Double> op_prod_double = (z,v)->z*v;
        BinaryOperator<String> op_combine_strings = (z,v)->z+" "+v;
        // test operations on Integer array input
        int sumResult = associativeOperation(testDataInteger,op_sum_int,0,testDataInteger.length-1);
        assert 120==sumResult;
        int minResult = associativeOperation(testDataInteger,op_min_int,0,testDataInteger.length-1);
        assert 1==minResult;
        int prodResult = associativeOperation(testDataInteger,op_prod_int,0,5);
        assert 720==prodResult;
        // test operations on Double array input
        double sumDoubleResult = associativeOperation(testDataDouble, op_sum_double, 0, testDataDouble.length - 1);
        assert 5.0==sumDoubleResult;
        double minDoubleResult = associativeOperation(testDataDouble,op_min_double,0,testDataDouble.length-1);
        assert 0.0==minDoubleResult;
        double prodDoubleResult = associativeOperation(testDataDouble,op_prod_double,0,testDataDouble.length-1);
        assert 0.0==prodDoubleResult;
        // test operations on String array
        String combineStrings = associativeOperation(testDataString,op_combine_strings,0,testDataString.length-1);
        System.out.println(combineStrings);
    }
}

