package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.DUtils;
import com.suredroid.discord.DiscordBot;
import io.github.classgraph.*;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static com.suredroid.discord.CommandSystem.CommandBase.getObjectList;


public class CommandListener implements MessageCreateListener {

    private static final Logger logger = LogManager.getLogger(CommandListener.class);
    public ArrayList<MessageCreateListener> messageCreateListeners = new ArrayList<>();

    public CommandListener() {
        gather();
    }

    public static String[] getTokens(String str) {
        StringTokenizer tokenizer = new StringTokenizer(str);
        tokenizer.setQuoteMatcher(StringMatcherFactory.INSTANCE.quoteMatcher());
        return tokenizer.getTokenArray();
    }

    private static Object[] combine(MessageCreateEvent e, String[] args) {
        Object[] objects = new Object[args.length + 1];
        objects[0] = e;
        System.arraycopy(args, 0, objects, 1, args.length);
        return objects;
    }


    private void gather() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
            ClassInfoList routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Command");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                // Get the Route annotation on the class
                AnnotationInfo annotationInfo = routeClassInfo.getAnnotationInfo("com.suredroid.discord.Annotations.Command");
                AnnotationParameterValueList annotationParamVals = annotationInfo.getParameterValues();

                boolean visible = (boolean) annotationParamVals.getValue("visible");
                String name = (String) annotationParamVals.getValue("name"), desc = (String) annotationParamVals.getValue("desc"), usage = (String) annotationParamVals.getValue("usage"), example = (String) annotationParamVals.getValue("example");
                if (name.isEmpty())
                    name = routeClassInfo.loadClass().getSimpleName();
                name = name.toLowerCase();

                Object customObject = generateObject(routeClassInfo);

                if (customObject != null) {
                    if (CommandBase.list.keySet().contains(name)) {
                        CommandBase cb = CommandBase.list.get(name);
                        if (cb.description.isEmpty() && !desc.isEmpty())
                            cb.description = desc;
                        if (cb.usage.isEmpty() && !usage.isEmpty())
                            cb.usage = usage;
                        if (cb.example.isEmpty() && !example.isEmpty())
                            cb.example = example;
                        cb.gatherRuns(customObject);
                    } else
                        new CommandBase(name, desc, usage, example, visible).gatherRuns(customObject);
                }

            }

            routeClassInfoList = scanResult.getClassesWithMethodAnnotation("com.suredroid.discord.Annotations.Command");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                for (MethodInfo methodInfo : routeClassInfo.getDeclaredMethodAndConstructorInfo()) {
                    if (methodInfo.hasAnnotation("com.suredroid.discord.Annotations.Command")) {
                        AnnotationParameterValueList annotationParamVals = methodInfo.getAnnotationInfo("com.suredroid.discord.Annotations.Command").getParameterValues();
                        boolean visible = (boolean) annotationParamVals.getValue("visible");
                        String name = (String) annotationParamVals.getValue("name"), desc = (String) annotationParamVals.getValue("desc"), usage = (String) annotationParamVals.getValue("usage"), example = (String) annotationParamVals.getValue("example");
                        if (name.isEmpty())
                            name = methodInfo.getName();
                        name = name.toLowerCase();

                        CommandBase cb;
                        if (CommandBase.list.keySet().contains(name)) {
                            cb = CommandBase.list.get(name);
                        } else {
                            cb = new CommandBase(name, desc, usage, example, visible);
                        }
                        Object customObject = generateObject(routeClassInfo);
                        if (customObject != null)
                            cb.addRun(methodInfo.loadClassAndGetMethod(), customObject);
                    }
                }
            }


            for (CommandBase cb : CommandBase.list.values()) {
                String message = "Your %1$s in the " + cb.command + " command is empty. Please provide a %1$s value for the command annotation. Populating with default values for now...";
                if (cb.description.isEmpty()) {
                    System.out.println(String.format(message, "description"));
                    cb.description = "N/A";
                }
                if (cb.usage.isEmpty()) {
                    System.out.println(String.format(message, "usage"));
                    cb.usage = "N/A";
                }
            }

            routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Listener");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                addListener(routeClassInfo);
            }


            routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Create");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                generateObject(routeClassInfo);
            }


        } catch (Exception e) {
            System.out.println("Error scanning classes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ClassGraph requiredInfoGraph() {
        return new ClassGraph().enableAllInfo();
    }

    private InfoWrapper getClassInfo(@NonNull Class<?> clazz) {
        try {
            ScanResult result = requiredInfoGraph().whitelistClasses(clazz.getName()).scan();
            return new InfoWrapper(result.getAllClasses().get(0), result);
        } catch (Exception e) {
            System.out.println("Error scanning classes: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public <T extends GloballyAttachableListener> T addListener(@NotNull T object) {
        //noinspection unchecked
        addListener((Class<T>) object.getClass(), object);
        return object;
    }

    public <T extends GloballyAttachableListener> T addListener(@NonNull Class<T> clazz) {
        return addListener(clazz, null);
    }

    private <T extends GloballyAttachableListener> T addListener(Class<T> clazz, T object) {
        T obj;
        if (object == null)
            obj = generateObject(clazz);
        else
            obj = object;
        if (obj != null) {
            Class<?>[] interfaces = Arrays.stream(clazz.getInterfaces()).filter(info -> Arrays.asList(info.getInterfaces()).contains(GloballyAttachableListener.class)).toArray(Class[]::new);
            if (interfaces.length > 0) {
                for (Class info : interfaces) {
                    if (info == MessageCreateListener.class) {
                        messageCreateListeners.add((MessageCreateListener) obj);
                    } else {
                        DUtils.getApi().addListener(clazz, obj);
                    }
                }
            } else {
                System.out.println("A globally attachable listener has not been implemented to " + clazz.getName() + "\nSkipping adding listener class...");
            }
        }
        return obj;
    }

    private void addListener(ClassInfo classInfo) {
        Object obj = generateObject(classInfo);
        if (obj != null) {
            ClassInfo[] interfaces = classInfo.getInterfaces().stream().filter(info -> info.implementsInterface("org.javacord.api.listener.GloballyAttachableListener")).toArray(ClassInfo[]::new);
            if (interfaces.length > 0) {
                for(ClassInfo info : interfaces) {
                    if (info.getName().equals(MessageCreateListener.class.getName())) {
                        messageCreateListeners.add((MessageCreateListener) obj);
                    } else {
                        Class<GloballyAttachableListener> clazz = (Class<GloballyAttachableListener>) info.loadClass();
                        DUtils.getApi().addListener(clazz, clazz.cast(obj));
                    }
                }
            } else {
                System.out.println("A globally attachable listener has not been implemented to " + classInfo.getName() + "\nSkipping adding listener class...");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T generateObject(Class clazz) {
        InfoWrapper wrapper = getClassInfo(clazz);
        T obj = null;
        if (wrapper != null) {
            obj = (T) generateObject(wrapper.getRouteClassInfo());
            wrapper.result.close();
        }
        return obj;
    }

    private Object generateObject(ClassInfo routeClassInfo) {
        Optional<Object> optobj = getObjectList().stream().filter(o -> o.getClass().equals(routeClassInfo.loadClass())).findAny();
        if (optobj.isPresent())
            return optobj.get();
        Object customObject = null;
        try {
            if (routeClassInfo.isInnerClass() && !routeClassInfo.isStatic()) {
                Optional<Constructor<?>> optConst = Arrays.stream(routeClassInfo.loadClass().getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 1 && construct.getParameterTypes()[0] == routeClassInfo.getOuterClasses().get(0).loadClass()).findFirst();
                Optional<Constructor<?>> outerConst = Arrays.stream(routeClassInfo.getOuterClasses().get(0).loadClass().getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 0).findAny();
                if (optConst.isPresent() && outerConst.isPresent()) {
                    Object outer;
                    Optional<Object> optouter = getObjectList().stream().filter(obj -> obj.getClass().equals(routeClassInfo.getOuterClasses().get(0).loadClass())).findFirst();
                    if (optouter.isPresent()) {
                        outer = optouter.get();
                    } else {
                        outer = outerConst.get().newInstance();
                        getObjectList().add(outer);
                    }
                    optConst.get().setAccessible(true);
                    customObject = optConst.get().newInstance(outer);
                } else {
                    System.out.println("No no-args constructor found for inner class " + routeClassInfo.getName() + ". Skipping...");
                }
            } else {
                Optional<Constructor<?>> optConst = Arrays.stream(routeClassInfo.loadClass().getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 0).findFirst();
                if (optConst.isPresent()) {
                    optConst.get().setAccessible(true);
                    customObject = optConst.get().newInstance();
                } else if (routeClassInfo.isStatic()) {
                    System.out.println("Static classes are not supported yet. Don't worry about things not being initialized, it is all automatic.");
                } else {
                    System.out.println("No no-args constructor found for class " + routeClassInfo.getName() + ". Skipping...");
                }
            }

        } catch (InvocationTargetException e) {
            System.out.println("Cannot create a new class due to internal exception. Cause: " + e.getCause().getMessage() + "\nSkipping class " + routeClassInfo.getName() + "...");
            e.getCause().printStackTrace();
        } catch (Exception e) {
            System.out.println("Error loading class: " + e.getMessage());
            e.printStackTrace();
        }
        if (customObject != null)
            getObjectList().add(customObject);

        return customObject;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        if (e.getMessage().getAuthor().isYourself() || e.getMessage().getUserAuthor().map(User::isBot).orElse(true)) {
            return;
        }
        if (!DiscordBot.debug && e.getServerTextChannel()
                .flatMap(ServerTextChannel::getCategory)
                .map(ChannelCategory::getName)
                .filter("BOT TESTING"::equalsIgnoreCase)
                .isPresent()) {
            return;
        }

        if (e.getMessageContent().startsWith(DiscordBot.getPrefix())) {
            if (Base.list.containsKey(getCommand(e.getMessageContent()))) {
                String command = getCommand(e.getMessageContent()).toLowerCase().trim();
                DiscordBot.log(e, "Command Run - " + command, "Message: " + e.getMessageContent());
                if (Mini.list.containsKey(command)) {
                    DUtils.sendMessage(e, Mini.list.get(command).title, Mini.list.get(command).message);
                } else if (Small.list.containsKey(command)) {
                    Small.list.get(command).consumer.create(e);
                } else if (CommandBase.list.containsKey(command)) {
                    CommandBase c = CommandBase.list.get(command);
                    String[] args = getTokens(getRest(e.getMessageContent()));
                    try {
                        if (c.runs.containsKey(args.length)) {
                            MethodWrapper mw = c.runs.get(args.length);
                            mw.getMethod().invoke(mw.getObject(), combine(e, args));
                        } else if (c.runs.containsKey(-1)) {
                            MethodWrapper mw = c.runs.get(-1);
                            mw.getMethod().invoke(mw.getObject(), args, e);
                        } else if (c.runs.containsKey(-2)) {
                            MethodWrapper mw = c.runs.get(-2);
                            mw.getMethod().invoke(mw.getObject(), getRest(e.getMessageContent()), e);
                        } else {
                            DUtils.sendMessage(e, "Invalid arguments!", "The correct usage of this command is \"" + c.usage + "\"." + ((c.example.isEmpty()) ? "" : " Ex. ``" + c.example + "``"));
                        }
                    } catch (InvocationTargetException e1) {
                        DiscordBot.warnError(e);
                        logger.error(e1.getCause().getMessage(), e1.getCause());
                        return;
                    } catch (IllegalAccessException e1) {
                        DiscordBot.warnError(e);
                        logger.error(e1.getMessage(), e1);
                        return;
                    }
                }
            } else {
                DUtils.sendMessage(e, "I don't know this command!", "Please check command for any errors or use **!help**");
            }
        } else {
            messageCreateListeners.forEach(listener -> listener.onMessageCreate(e));
        }
    }

    private String getCommand(String text) {
        int index = text.indexOf(' ');
        return index > -1 ? text.substring(1, index) : text.substring(1);
    }

    private String getRest(String text) {
        int index = text.indexOf(' ');
        return index > -1 ? text.substring(index + 1) : "";
    }

    @Data
    private class InfoWrapper {
        private final ClassInfo routeClassInfo;
        private final ScanResult result;

    }

}
