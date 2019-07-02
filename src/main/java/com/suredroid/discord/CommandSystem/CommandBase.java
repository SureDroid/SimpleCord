package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.EmbedMessage;
import org.javacord.api.event.message.MessageCreateEvent;

import java.lang.reflect.Method;
import java.util.*;

public class CommandBase {

    static HashMap<String, CommandBase> list = new HashMap<>(), fullBase = new HashMap<>();
    HashMap<Integer, MethodInfo> runs = new HashMap<>();
    public CommandProperties properties;

    CommandBase(CommandProperties properties) {
        this.properties = properties;
        String name = properties.getName();
        list.put(name, this);
        fullBase.put(name, this);
        for(String alias : properties.getAliases())
            fullBase.put(alias, this);
    }



//    public void addRun(Method method) {
//        method.setAccessible(true);
//        Class<?>[] classes = method.getParameterTypes();
//        if (method.getParameterCount() > 0 && classes[0].equals(MessageCreateEvent.class) && Arrays.stream(Arrays.copyOfRange(classes,1,classes.length)).allMatch(checkClass->checkClass.equals(String.class))) {
//            runs.put(method.getParameterCount() - 1, method);
//        }
//        else if(method.getParameterCount() == 2 && classes[1].equals(MessageCreateEvent.class)) {
//            if (classes[0].equals(String[].class) && !runs.containsKey(-1))
//                runs.put(-1, method);
//            else if (classes[0].equals(String.class) && !runs.containsKey(-2))
//                runs.put(-2, method);
//            else
//                System.out.println("This class does not have a valid all method. The first parameter must either be a string array or a string to be a valid all method. Ignoring...\n" + method.getName() + " with parameters " +  + " in " + object.getClass().getName());
//        } else {
//            System.out.println("This class does not have a valid run method. You should either have [MessageCreateEvent, String... args] or [String | String[], MessageCreateEvent] as your parameters. Ignoring...\n" + method.getName() + " in " + object.getClass().getName());
//        }



    void gatherRuns(Object object) {
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase("run"))
                addRun(method, object);
        }
    }

    void addRun(Method method, Object object) {
        CommandManager.addObject(object);
        method.setAccessible(true);

        Class<?> returnClass = method.getReturnType();
        ReturnType returnType;
        if(returnClass == EmbedMessage.class)
            returnType = ReturnType.EmbedMessage;
        else if(returnClass == String.class)
            returnType = ReturnType.String;
        else
            returnType = ReturnType.None;



        Class<?>[] classes = method.getParameterTypes();
        boolean allstring = true;
        for (int i = 1; i < method.getParameterCount(); i++)
            if (classes[i] != String.class) allstring = false;
        if (method.getParameterCount() > 0 && classes[0].equals(MessageCreateEvent.class) && allstring) {
            runs.put(method.getParameterCount() - 1, new MethodInfo(method, object, returnType));
        } else if (method.getParameterCount() == 2 && classes[1].equals(MessageCreateEvent.class)) {
            if (classes[0].equals(String[].class) && !runs.containsKey(-1))
                runs.put(-1, new MethodInfo(method, object, returnType));
            else if (classes[0].equals(String.class) && !runs.containsKey(-2))
                runs.put(-2, new MethodInfo(method, object, returnType));
            else
                System.out.println("This command does not have valid parameters. The first parameter must either be a string array or a string and the second should be a MessageCreateEvent." + getIgnore(method, object));
        } else
            System.out.println("This command does not have valid parameters. You should either have a [MessageCreateEvent, args...] (regular) or [String | String[], MessageCreateEvent] (all)." + getIgnore(method, object));


    }

    private String getIgnore(Method method, Object object) {
        return " Ignoring...\n" + method.getName() + " with parameters " + Arrays.toString(method.getParameterTypes()) + " in " + object.getClass().getName();
    }

    public static HashMap<String, CommandBase> getBaseList() {
        return list;
    }

}