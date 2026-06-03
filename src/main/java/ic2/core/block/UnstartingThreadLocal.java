package ic2.core.block;

class UnstartingThreadLocal<T> extends ThreadLocal<T> {
  protected T initialValue() {
    throw new UnsupportedOperationException();
  }
}
