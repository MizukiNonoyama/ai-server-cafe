package ai_server_cafe.updater;

import ai_server_cafe.model.FieldObject;

public abstract class AbstractUpdater<T extends FieldObject> {
    protected boolean invert;
    // 最終的な値
    protected T value;

    public AbstractUpdater(T init) {
        this.invert = false;
        this.value = init;
    }

    public T getValue() {
        return (T)this.value.copy();
    }

    synchronized public void setInvert(boolean value) {
        this.invert = value;
    }

    public boolean isInvert() {
        return this.invert;
    }
}
