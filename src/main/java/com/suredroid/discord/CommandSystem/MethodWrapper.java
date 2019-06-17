package com.suredroid.discord.CommandSystem;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@AllArgsConstructor
public @Data class MethodWrapper {
    private final Method method;
    private final Object object;
}
