package com.algorithms.parallel;

/**
 * Created by amazaspshaumyan on 9/28/15.
 *
 * Parallel construction of sorted doubly linked list from
 * binary search tree using Java Fork/Join paradigm.
 * Space complexity O(1)
 *
 * Perfomance:
 *           Work = O(n)
 *           Span = O(log(n)) (assuming balanced tree)
 *           Parallelism = O(n/log(n)) (exponential parallelism)
 *
 */

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Comparator;



class Convert<T> extends RecursiveTask<CircularLinkedList<T>>{

   Node<T> node;
   Comparator<T> comp;

   public Convert(Node<T> node, Comparator<T> comp){
      this.node = node;
      this.comp = comp;
   }

   public static <T> CircularLinkedList<T> constructSortedListFromBST(Node<T> node, Comparator<T> comp){
      if(node==null) return null;
      CircularLinkedList<T> left = constructSortedListFromBST(node.left,comp);
      CircularLinkedList<T> right = constructSortedListFromBST(node.right,comp);
      CircularLinkedList<T> current = new CircularLinkedList<T>(node);
      if(left!=null) current = CircularLinkedList.CircularLinkedListMerge(left,current);
      if(right!=null) current = CircularLinkedList.CircularLinkedListMerge(current,right);
      return current;
   }

   @Override
   public  CircularLinkedList<T>  compute(){
      if(node.N < ConvertBSTSortedLinkedList.CUT_SEQ){
         return constructSortedListFromBST(node, comp);
      }
      Convert<T> leftList = new Convert<>(node.left,comp);
      Convert<T> rightList = new Convert<>(node.right,comp);
      leftList.fork();
      CircularLinkedList<T> right = rightList.compute();
      CircularLinkedList<T> left  = leftList.join();
      CircularLinkedList<T> current = new CircularLinkedList<T>(node);
      if(left!=null) current = CircularLinkedList.CircularLinkedListMerge(left,current);
      if(right!=null) current = CircularLinkedList.CircularLinkedListMerge(current,right);
      return current;
   }
}


public class ConvertBSTSortedLinkedList<T> {
    public static int CUT_SEQ = 10;  // current threshold value is for testing purposes, generally it should be higher
    public static ForkJoinPool fjp = new ForkJoinPool();


    public static <T> CircularLinkedList<T> convert(BinarySearchTreeHelper<T> bst){
       return convert(bst,new DefaultComparator<T>());
    }

    public static <T> CircularLinkedList<T> convert(BinarySearchTreeHelper<T> bst, Comparator<T> comp){
       return fjp.invoke(new Convert<>(bst.root,comp));
    }


    public static void main(String[] args){
     Integer[] x = {1,2,3,4,5,6,7,8,9,10};
     BinarySearchTreeHelper<Integer> bst = ParallelConstructBST.construct(x);
     System.out.println("");
     System.out.println(IsBST.isBST(bst));
     System.out.println(convert(bst).toString());

    }

}
