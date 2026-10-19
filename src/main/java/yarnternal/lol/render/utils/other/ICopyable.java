package yarnternal.lol.render.utils.other;

public interface ICopyable<T extends ICopyable<T>> {
    T set(T value);

    T copy();
}