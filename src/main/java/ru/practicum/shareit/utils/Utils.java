package ru.practicum.shareit.utils;

import java.util.function.Consumer;

public class Utils {
    public static <T> void applyIfNotNull(T value, Consumer<T> action) {
        if (value != null) {
            action.accept(value);
        }
    }
}