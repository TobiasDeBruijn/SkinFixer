package dev.array21.skinfixer.reflect;

public class ReflectException extends Exception {
    private final Exception inner;

    public ReflectException(Exception inner) {
        this.inner = inner;
    }

    @Override
    public void printStackTrace() {
        this.inner.printStackTrace();
    }

    @Override
    public String toString() {
        return this.inner.toString();
    }
}
