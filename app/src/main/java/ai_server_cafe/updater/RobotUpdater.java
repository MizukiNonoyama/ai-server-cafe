package ai_server_cafe.updater;

import ai_server_cafe.filter.AbstractFilter;
import ai_server_cafe.filter.AbstractFilterManual;
import ai_server_cafe.filter.AbstractFilterSame;
import ai_server_cafe.model.FilteredRobot;
import ai_server_cafe.model.RawRobot;
import ai_server_cafe.network.proto.ssl.vision.VisionDetection;
import ai_server_cafe.util.TeamColor;
import ai_server_cafe.util.interfaces.IFunction;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class RobotUpdater {
    public final Map<Integer, AbstractFilterSame<FilteredRobot, RawRobot>> filterSameMap;
    public final Map<Integer, AbstractFilterManual<FilteredRobot, RawRobot>> filterManualMap;
    public final Map<Integer, FilteredRobot> filteredRobotsMap;
    public final TeamColor color;
    private Optional<IFunction<AbstractFilterSame<FilteredRobot, RawRobot>>> initializeFilterFunc;
    public RobotUpdater(TeamColor color) {
        this.color = color;
        this.filterSameMap = new HashMap<>();
        this.filterManualMap = new HashMap<>();
        this.filteredRobotsMap = new HashMap<>();
    }

    synchronized public void setDefaultFilter(Class<? extends AbstractFilterSame<FilteredRobot, RawRobot>> clazz, Object... arguments) {
        this.initializeFilterFunc = Optional.of(new IFunction<AbstractFilterSame<FilteredRobot, RawRobot>>() {
            @Override
            public AbstractFilterSame<FilteredRobot, RawRobot> function(Object... args) {
                List<Class<?>> classes = new ArrayList<>();
                for (Object o : arguments) {
                    classes.add(o.getClass());
                }
                try {
                    return clazz.getConstructor((Class<?>[]) classes.toArray()).newInstance(arguments);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    synchronized public void clearDefaultFilter() {
        this.initializeFilterFunc = Optional.empty();
    }

    synchronized public void update(VisionDetection detection) {

    }
}
