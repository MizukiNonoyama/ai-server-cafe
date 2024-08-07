package ai_server_cafe.model;

import ai_server_cafe.util.interfaces.IFunction;

import java.util.Optional;

public abstract class AbstractFilteredObject<T> extends FieldObject {
    public AbstractFilteredObject() {
        this.lost = true;
    }

    public abstract T getRaw();
}
