package ai_server_cafe.filter;

import ai_server_cafe.model.AbstractFilteredObject;

import java.util.Optional;

public abstract class AbstractFilter<T extends AbstractFilteredObject<V>, V> {
    public abstract Optional<T> updateRaw(Optional<V> rawValue, double updateTime);
}

