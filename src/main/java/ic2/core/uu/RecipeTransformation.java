package ic2.core.uu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class RecipeTransformation {
   public final double transformCost;
   public List<List<LeanItemStack>> inputs;
   public List<LeanItemStack> outputs;

   public RecipeTransformation(double transformCost, List<List<LeanItemStack>> inputs, LeanItemStack... outputs) {
      this(transformCost, inputs, Arrays.asList(outputs));
   }

   public RecipeTransformation(double transformCost, List<List<LeanItemStack>> inputs, List<LeanItemStack> outputs) {
      this.transformCost = transformCost;
      this.inputs = inputs;
      this.outputs = outputs;
   }

   protected void merge() {
      List<List<LeanItemStack>> cleanInputs = new ArrayList<>();

      for (List<LeanItemStack> inputList : this.inputs) {
         boolean found = false;
         ListIterator<List<LeanItemStack>> it = cleanInputs.listIterator();

         while (it.hasNext()) {
            List<LeanItemStack> cleanInputList = it.next();
            cleanInputList = this.mergeEqualLists(inputList, cleanInputList);
            if (cleanInputList != null) {
               found = true;
               it.set(cleanInputList);
               break;
            }
         }

         if (!found) {
            cleanInputs.add(inputList);
         }
      }

      for (List<LeanItemStack> inputList : this.inputs) {
         for (List<LeanItemStack> cleanInputList : cleanInputs) {
            List<LeanItemStack> unmatched = new LinkedList<>(inputList);
            boolean found = false;

            for (LeanItemStack stackOffer : cleanInputList) {
               found = false;
               Iterator<LeanItemStack> it = unmatched.iterator();

               while (it.hasNext()) {
                  LeanItemStack stackReq = it.next();
                  if (stackOffer.hasSameItem(stackReq)) {
                     found = true;
                     it.remove();
                     break;
                  }
               }

               if (!found) {
                  break;
               }
            }
         }
      }

      this.inputs = cleanInputs;
      List<LeanItemStack> cleanOutputs = new ArrayList<>();

      for (LeanItemStack output : this.outputs) {
         boolean found = false;
         ListIterator<LeanItemStack> it = cleanOutputs.listIterator();

         while (it.hasNext()) {
            LeanItemStack stack = it.next();
            if (output.hasSameItem(stack)) {
               found = true;
               it.set(stack.copyWithSize(stack.getSize() + output.getSize()));
               break;
            }
         }

         if (!found) {
            cleanOutputs.add(output);
         }
      }

      this.outputs = cleanOutputs;
   }

   @Override
   public String toString() {
      return "{ " + this.transformCost + " + " + this.inputs + " -> " + this.outputs + " }";
   }

   private List<LeanItemStack> mergeEqualLists(List<LeanItemStack> listA, List<LeanItemStack> listB) {
      if (listA.size() != listB.size()) {
         return null;
      }

      List<LeanItemStack> ret = new ArrayList<>(listA.size());
      List<LeanItemStack> listBCopy = new LinkedList<>(listB);

      for (LeanItemStack a : listA) {
         boolean found = false;
         Iterator<LeanItemStack> it = listBCopy.iterator();

         while (it.hasNext()) {
            LeanItemStack b = it.next();
            if (a.hasSameItem(b)) {
               found = true;
               ret.add(a.copyWithSize(a.getSize() + b.getSize()));
               it.remove();
               break;
            }
         }

         if (!found) {
            return null;
         }
      }

      return ret;
   }
}
