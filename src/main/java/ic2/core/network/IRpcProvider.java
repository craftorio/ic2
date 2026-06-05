package ic2.core.network;

public interface IRpcProvider<V> {
   V executeRpc(Object... var1);
}
