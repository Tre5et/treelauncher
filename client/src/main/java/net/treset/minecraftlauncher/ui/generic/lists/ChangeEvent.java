package net.treset.minecraftlauncher.ui.generic.lists;

public interface ChangeEvent<V, E> {

    void update();

    void add(V value);

    void remove(E element);

    void change(V oldValue, V newValue);
}
