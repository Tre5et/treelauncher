package net.treset.minecraftlauncher.ui.base;

import net.treset.minecraftlauncher.ui.generic.PopupElement;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class UiElement implements UiController {
    UiController parent;
    private Function<Boolean, Boolean> lockSetter;
    private Supplier<Boolean> lockGetter;
    private boolean visible = false;

    public void init(UiController parent, Function<Boolean, Boolean> lockSetter, Supplier<Boolean> lockGetter) {
        this.parent = parent;
        this.lockSetter = lockSetter;
        this.lockGetter = lockGetter;
    }

    @Override
    public void triggerHomeAction() {
        parent.triggerHomeAction();
    }

    public void setVisible(boolean visible) {
        if(this.visible == visible) return;
        if(visible)
            beforeShow(null);
        setRootVisible(visible);
        this.visible = visible;
        if(visible)
            afterShow(null);
    }

    public abstract void setRootVisible(boolean visible);

    protected boolean setLock(boolean lock) {
        return lockSetter.apply(lock);
    }

    protected boolean getLock() {
        return lockGetter.get();
    }

    public void setLockSetter(Function<Boolean, Boolean> lockSetter) {
        this.lockSetter = lockSetter;
    }

    public Function<Boolean, Boolean> getLockSetter() {
        return lockSetter;
    }

    public Supplier<Boolean> getLockGetter() {
        return lockGetter;
    }

    public void setLockGetter(Supplier<Boolean> lockGetter) {
        this.lockGetter = lockGetter;
    }
}