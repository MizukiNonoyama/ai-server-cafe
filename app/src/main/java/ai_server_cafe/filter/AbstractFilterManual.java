package ai_server_cafe.filter;

import ai_server_cafe.model.AbstractFilteredObject;
import ai_server_cafe.util.interfaces.IFuncParam1;

import java.util.Optional;

public abstract class AbstractFilterManual<T extends AbstractFilteredObject<V>, V> extends AbstractFilter<T, V> {
    protected IFuncParam1<Void, Optional<T>> writerFunc;

    public AbstractFilterManual() {
        this.writerFunc = new IFuncParam1<Void, Optional<T>>() {
            @Override
            public Void function(Optional<T> t) {
                return null;
            }
        };
    }

    public void setWriterFunc(IFuncParam1<Void, Optional<T>> writerFunc) {
        this.writerFunc = writerFunc;
    }

    public abstract void updateObserver(Object... args);

    protected void write(Optional<T> value) {
        this.writerFunc.function(value);
    }
}
