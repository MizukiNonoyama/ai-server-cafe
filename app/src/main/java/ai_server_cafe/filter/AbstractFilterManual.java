package ai_server_cafe.filter;

import ai_server_cafe.model.AbstractFilteredObject;
import ai_server_cafe.util.interfaces.IFunction;

import java.util.Optional;

public abstract class AbstractFilterManual<T extends AbstractFilteredObject<V>, V> extends AbstractFilter<T, V> {
    protected IFunction<Void> writerFunc;

    public AbstractFilterManual(IFunction<Void> writerFunc) {
        this.writerFunc = writerFunc;
    }

    protected void write(Optional<T> value) {
        this.writerFunc.function(value);
    }
}
