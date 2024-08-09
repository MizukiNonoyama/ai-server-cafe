package ai_server_cafe.updater;

import ai_server_cafe.model.FieldObject;

public abstract class AbstractUpdater<T extends FieldObject> {
    /**
     * 反転させるか
     */
    protected boolean invert;
    /**
     * 最終的な値
     */
    protected T value;

    public AbstractUpdater(T init, boolean invert) {
        this.invert = invert;
        this.value = init;
    }

    /**
     * 最終的な値
     */
    @SuppressWarnings("unchecked")
    synchronized public T getValue() {
        return (T)this.value.copy();
    }

    /**
     * 反転させるか
     */
    synchronized public void setInvert(boolean value) {
        this.invert = value;
    }
}
