package com.suredroid.discord.Annotations;

import org.javacord.api.entity.permission.PermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Command {
    String name() default "";
    String desc() default "";
    String usage() default "";
    String example() default "";
    boolean visible() default true;

    //To Add
    String[] aliases() default {};
    PermissionType[] permissions() default {};
    String[] roles() default {};

    //Opt
    boolean serverOnly() default false;
    boolean async() default false;
}
