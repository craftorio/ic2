package ic2.shades.org.ejml.interfaces.decomposition;

import ic2.shades.org.ejml.data.Matrix64F;

public interface DecompositionInterface<T extends Matrix64F> {
   boolean decompose(T var1);

   boolean inputModified();
}
