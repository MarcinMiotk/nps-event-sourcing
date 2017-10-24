package nps.eventsourcing;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.UUID;

public class EventSourcing {

    public interface Command {
        <T extends IdAggregate> T getId();
    }

    public interface Event {
        <T extends IdAggregate> T getId();
    }

    public interface CommandStore {
        void append(EventSourcing.Command command);
    }

    public interface CommandBus {
        <T extends EventSourcing.Command> void subscribe(CommandHandler handler, Class<T> type);
    }

    public interface EventBus {
        <T extends EventSourcing.Event> Disposable subscribe(EventHandler handler, Class<T> type, Replay replay);
    }

    public enum Replay {
        REPLAY_ALLOWED,
        ONLY_FIRST_OCCURENCE_ALLOWED
    }

    @FunctionalInterface
    public interface CommandHandler {
        void handle(EventSourcing.Command command);
    }

    @FunctionalInterface
    public interface EventHandler {
        void handle(EventSourcing.Event event);
    }

    public interface EventStream {
        Observable<Event> load(IdAggregate id);
    }

    public interface EventStore {
        UUID append(EventSourcing.Event event);
    }

    public interface Aggregate {
        <T extends IdAggregate> T getId();
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OnEvent {

    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface OnCommand {

    }

    public static void subscribeListeners(Object target, EventSourcing.EventBus bus) {
        Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {
            OnEvent on = method.getAnnotation(OnEvent.class);
            if (on != null) {
                int parameters = method.getParameterCount();
                if (parameters == 1) {
                    Class parameterClass = method.getParameterTypes()[0];
                    method.setAccessible(true);
                    bus.subscribe(event -> {
                        try {
                            method.invoke(target, event);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                            System.err.println("ERROR !!!!");
                        }
                    }, parameterClass, EventSourcing.Replay.REPLAY_ALLOWED);
                }
            }
        }
    }

    public static void subscribeListeners(Object target, EventSourcing.CommandBus bus) {
        Method[] methods = target.getClass().getDeclaredMethods();
        for (Method method : methods) {
            OnCommand on = method.getAnnotation(OnCommand.class);
            if (on != null) {
                int parameters = method.getParameterCount();
                if (parameters == 1) {
                    Class parameterClass = method.getParameterTypes()[0];
                    method.setAccessible(true);
                    bus.subscribe(event -> {
                        try {
                            method.invoke(target, event);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                            System.err.println("ERROR !!!!");
                        }
                    }, parameterClass);
                }
            }
        }
    }
}
