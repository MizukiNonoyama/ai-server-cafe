package ai_server_cafe.filter;

import ai_server_cafe.model.AbstractFilteredObject;

import java.util.Optional;

public abstract class AbstractFilterManual<T extends AbstractFilteredObject<V>, V> extends AbstractFilter<T, V> {
    protected abstract void write(Optional<V> value);
}
