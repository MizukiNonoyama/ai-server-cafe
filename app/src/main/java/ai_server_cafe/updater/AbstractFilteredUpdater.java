package ai_server_cafe.updater;

import ai_server_cafe.filter.AbstractFilterManual;
import ai_server_cafe.filter.AbstractFilterSame;
import ai_server_cafe.model.AbstractFilteredObject;
import java.util.Optional;

public abstract class AbstractFilteredUpdater<T extends AbstractFilteredObject<V>, V> extends AbstractUpdater<T> {
    protected Optional<AbstractFilterSame<T, V>> filterSame;
    protected Optional<AbstractFilterManual<T, V>> filterManual;

    public AbstractFilteredUpdater(T init) {
        super(init);
    }

    synchronized public void setFilterSame(AbstractFilterSame<T, V> filterSame) {
        this.filterManual = Optional.empty();
        this.filterSame = Optional.of(filterSame);
    }

    synchronized public void setFilterManual(AbstractFilterManual<T, V> filterManual) {
        this.filterSame = Optional.empty();
        this.filterManual = Optional.of(filterManual);
    }

    synchronized public void clearFilters() {
        this.filterSame = Optional.empty();
        this.filterManual = Optional.empty();
    }
}
