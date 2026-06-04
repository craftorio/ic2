// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.uu;

import java.util.ListIterator;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeTransformation
{
    public final double transformCost;
    public List<List<LeanItemStack>> inputs;
    public List<LeanItemStack> outputs;
    
    public RecipeTransformation(final double transformCost, final List<List<LeanItemStack>> inputs, final LeanItemStack... outputs) {
        this(transformCost, inputs, Arrays.asList(outputs));
    }
    
    public RecipeTransformation(final double transformCost, final List<List<LeanItemStack>> inputs, final List<LeanItemStack> outputs) {
        this.transformCost = transformCost;
        this.inputs = inputs;
        this.outputs = outputs;
    }
    
    protected void merge() {
        final List<List<LeanItemStack>> cleanInputs = new ArrayList<List<LeanItemStack>>();
        for (final List<LeanItemStack> inputList : this.inputs) {
            boolean found = false;
            final ListIterator<List<LeanItemStack>> it = cleanInputs.listIterator();
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
        for (final List<LeanItemStack> inputList : this.inputs) {
            for (final List<LeanItemStack> cleanInputList2 : cleanInputs) {
                final List<LeanItemStack> unmatched = new LinkedList<LeanItemStack>(inputList);
                boolean found2 = false;
                for (final LeanItemStack stackOffer : cleanInputList2) {
                    found2 = false;
                    final Iterator<LeanItemStack> it2 = unmatched.iterator();
                    while (it2.hasNext()) {
                        final LeanItemStack stackReq = it2.next();
                        if (stackOffer.hasSameItem(stackReq)) {
                            found2 = true;
                            it2.remove();
                            break;
                        }
                    }
                    if (!found2) {
                        break;
                    }
                }
            }
        }
        this.inputs = cleanInputs;
        final List<LeanItemStack> cleanOutputs = new ArrayList<LeanItemStack>();
        for (final LeanItemStack output : this.outputs) {
            boolean found3 = false;
            final ListIterator<LeanItemStack> it3 = cleanOutputs.listIterator();
            while (it3.hasNext()) {
                final LeanItemStack stack = it3.next();
                if (output.hasSameItem(stack)) {
                    found3 = true;
                    it3.set(stack.copyWithSize(stack.getSize() + output.getSize()));
                    break;
                }
            }
            if (!found3) {
                cleanOutputs.add(output);
            }
        }
        this.outputs = cleanOutputs;
    }
    
    @Override
    public String toString() {
        return "{ " + this.transformCost + " + " + this.inputs + " -> " + this.outputs + " }";
    }
    
    private List<LeanItemStack> mergeEqualLists(final List<LeanItemStack> listA, final List<LeanItemStack> listB) {
        if (listA.size() != listB.size()) {
            return null;
        }
        final List<LeanItemStack> ret = new ArrayList<LeanItemStack>(listA.size());
        final List<LeanItemStack> listBCopy = new LinkedList<LeanItemStack>(listB);
        for (final LeanItemStack a : listA) {
            boolean found = false;
            final Iterator<LeanItemStack> it = listBCopy.iterator();
            while (it.hasNext()) {
                final LeanItemStack b = it.next();
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
