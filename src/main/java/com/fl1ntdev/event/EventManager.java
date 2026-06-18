package com.fl1ntdev.event;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class EventManager {

    private static final Map<Class<? extends Event>, MethodData[]> REGISTRY_MAP = new Object2ObjectArrayMap<>();

    @Setter
    private static EventExceptionHandler exceptionHandler =
        (context, throwable) -> {System.err.println("[EventManager] " + context);throwable.printStackTrace();};

    public void register(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (isMethodBad(method)) continue;
            register(method, object);
        }
    }

    public void register(Object object, Class<? extends Event> eventClass) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (isMethodBad(method, eventClass)) continue;
            register(method, object);
        }
    }

    public void unregister(Object object) {
        REGISTRY_MAP.replaceAll((eventClass, dataArray) -> {
            ObjectArrayList<MethodData> newList = new ObjectArrayList<>();
            for (MethodData data : dataArray) {
                if (!data.source().equals(object)) {
                    newList.add(data);
                }
            }
            return newList.isEmpty() ? null : newList.toArray(new MethodData[0]);
        });

        REGISTRY_MAP.values().removeIf(v -> v == null);
    }

    public void unregister(Object object, Class<? extends Event> eventClass) {
        MethodData[] dataArray = REGISTRY_MAP.get(eventClass);
        if (dataArray == null) return;

        ObjectArrayList<MethodData> newList = new ObjectArrayList<>();
        for (MethodData data : dataArray) {
            if (!data.source().equals(object)) {
                newList.add(data);
            }
        }

        if (newList.isEmpty()) {
            REGISTRY_MAP.remove(eventClass);
        } else {
            REGISTRY_MAP.put(eventClass, newList.toArray(new MethodData[0]));
        }
    }

    @SuppressWarnings("unchecked")
    private void register(Method method, Object object) {
        Class<? extends Event> indexClass = (Class<? extends Event>) method.getParameterTypes()[0];
        MethodData data = new MethodData(
                object,
                method,
                method.getAnnotation(EventHandler.class).value()
        );

        method.setAccessible(true);

        MethodData[] existing = REGISTRY_MAP.get(indexClass);

        if (existing != null) {
            for (MethodData e : existing) {
                if (e.equals(data)) return;
            }

            ObjectArrayList<MethodData> list = new ObjectArrayList<>(existing);
            list.add(data);
            list.sort((a, b) -> Byte.compare(a.priority(), b.priority()));

            REGISTRY_MAP.put(indexClass, list.toArray(new MethodData[0]));
        } else {
            REGISTRY_MAP.put(indexClass, new MethodData[]{data});
        }
    }

    public static Event callEvent(final Event event) {
        MethodData[] dataArray = REGISTRY_MAP.get(event.getClass());
        if (dataArray == null || dataArray.length == 0) return event;

        if (event instanceof EventStoppable stoppable) {
            for (MethodData data : dataArray) {
                invoke(data, event);
                if (stoppable.isStopped()) break;
            }
        } else {
            for (MethodData data : dataArray) {
                invoke(data, event);
            }
        }

        return event;
    }

    private static void invoke(MethodData data, Event argument) {
        try {
            data.target().invoke(data.source(), argument);

        } catch (IllegalAccessException e) {
            exceptionHandler.handle("Illegal access in " + data.target().getName(), e);

        } catch (IllegalArgumentException e) {
            exceptionHandler.handle("Illegal args in " + data.target().getName(), e);

        } catch (InvocationTargetException e) {
            exceptionHandler.handle("Exception in " + data.target().getName(), e.getCause());
        }
    }

    private static boolean isMethodBad(Method method) {
        return method.getParameterCount() != 1
                || !method.isAnnotationPresent(EventHandler.class);
    }

    private static boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return isMethodBad(method)
                || !method.getParameterTypes()[0].equals(eventClass);
    }

    private record MethodData(Object source, Method target, byte priority) {}

    @FunctionalInterface
    public interface EventExceptionHandler {
        void handle(String context, Throwable throwable);
    }
}