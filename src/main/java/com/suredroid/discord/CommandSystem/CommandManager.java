package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.Annotations.Command;
import com.suredroid.discord.DUtils;
import com.suredroid.discord.DiscordBot;
import com.suredroid.discord.EmbedMessage;
import com.suredroid.discord.Error;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
import org.apache.commons.text.matcher.StringMatcherFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.channel.ChannelCategory;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.javacord.api.listener.message.MessageCreateListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static com.suredroid.discord.CommandSystem.CommandBase.fullBase;


public class CommandManager {

    private static final Logger logger = LogManager.getLogger(CommandManager.class);
    private ArrayList<MessageCreateListener> messageCreateListeners = new ArrayList<>();

    public CommandManager() {
        gather();
        DUtils.getApi().addListener(new CommandListener());
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

    private static boolean isInnerClass(Class<?> clazz) {
        return clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers());
    }

    private void gather() {
        try (ScanResult scanResult = requiredInfoGraph().scan()) {
            ClassInfoList routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Command");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                addCommand(routeClassInfo.loadClass());
            }

            routeClassInfoList = scanResult.getClassesWithMethodAnnotation("com.suredroid.discord.Annotations.Command");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                for (io.github.classgraph.MethodInfo methodInfo : routeClassInfo.getDeclaredMethodAndConstructorInfo()) {
                    if (methodInfo.hasAnnotation("com.suredroid.discord.Annotations.Command")) {
                        addCommand(methodInfo.loadClassAndGetMethod());
                    }
                }
            }


            for (CommandBase cb : CommandBase.list.values()) {
                cb.properties.check();
            }

            routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Listener");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                addListener(routeClassInfo);
            }


            routeClassInfoList = scanResult.getClassesWithAnnotation("com.suredroid.discord.Annotations.Create");
            for (ClassInfo routeClassInfo : routeClassInfoList) {
                generateObject(routeClassInfo.loadClass());
            }


        } catch (Exception e) {
            System.out.println("Error scanning classes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addCommand(@NotNull CommandProperties properties, @NotNull Object object) {
        properties.check();
        if (CommandBase.list.keySet().contains(properties.getName())) {
            CommandBase cb = CommandBase.list.get(properties.getName());
            cb.properties.combine(properties);
            cb.gatherRuns(object);
        } else
            new CommandBase(properties).gatherRuns(object);
    }

    public void addCommand(@NotNull Class clazz) {
        addCommand(clazz, null);
    }

    public void addCommand(@NotNull Class clazz, Object object) {
        // Get the Route annotation on the class
        Command annotation = (Command) clazz.getAnnotation(Command.class);
        if (annotation != null) {
            Object customObject = object == null ? generateObject(clazz) : object;
            if (customObject != null) {
                String name = annotation.name().isEmpty() ? clazz.getSimpleName() : annotation.name();
                CommandProperties properties = new CommandProperties(name, annotation.desc(), annotation.usage(), annotation.example(), annotation.hidden(), annotation.aliases(), annotation.permissions(), annotation.roles(), annotation.serverOnly(), annotation.async());
                if (CommandBase.list.keySet().contains(name)) {
                    CommandBase cb = CommandBase.list.get(name);
                    cb.properties.combine(properties);
                    cb.gatherRuns(customObject);
                } else
                    new CommandBase(properties).gatherRuns(customObject);
            }
        } else {
            System.out.println(clazz.getName() + " does not have a valid class with a Command annotation.");
        }
    }

    public void addCommand(Method method) {
        addCommand(method, null);
    }

    public void addCommand(@NotNull Method method, Object object) {
        Command annotation = (Command) method.getAnnotation(Command.class);
        if (annotation != null) {
            Object customObject = object == null ? generateObject(method.getDeclaringClass()) : object;
            if (customObject != null) {
                String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
                CommandProperties properties = new CommandProperties(name, annotation.desc(), annotation.usage(), annotation.example(), annotation.hidden(), annotation.aliases(), annotation.permissions(), annotation.roles(), annotation.serverOnly(), annotation.async());
                if (CommandBase.list.keySet().contains(name)) {
                    CommandBase cb = CommandBase.list.get(name);
                    cb.properties.combine(properties);
                    cb.addRun(method, customObject);
                } else
                    new CommandBase(properties).addRun(method, customObject);
            }
        } else {
            System.out.println(method.getDeclaringClass().getName() + " does not have a valid class with a Command annotation.");
        }
    }

    public boolean removeCommand(@NotNull String name) {
        name = name.toLowerCase();
        if(CommandBase.list.containsKey(name)) {
            CommandBase c = CommandBase.list.get(name);
            for(String alias : c.properties.getAliases()) {
                fullBase.remove(alias);
            }
            fullBase.remove(name);
            CommandBase.list.remove(name);
            return true;
        } else
            return false;
    }

    private ClassGraph requiredInfoGraph() {
        ClassGraph cg = new ClassGraph().enableAnnotationInfo().enableClassInfo().enableMethodInfo();
        if (!DiscordBot.useBasicCommands)
            cg.blacklistPackages("com.suredroid.discord.BasicCommands");
        return cg;
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
        Object obj = generateObject(classInfo.loadClass());
        if (obj != null) {
            ClassInfo[] interfaces = classInfo.getInterfaces().stream().filter(info -> info.implementsInterface("org.javacord.api.listener.GloballyAttachableListener")).toArray(ClassInfo[]::new);
            if (interfaces.length > 0) {
                for (ClassInfo info : interfaces) {
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

    static ArrayList<Object> objectList = new ArrayList<>();

    public static void addObject(Object object) {
        if (!objectList.contains(object))
            objectList.add(object);
    }

    public static boolean removeObject(Object object) {
        return objectList.remove(object);
    }

    private static ArrayList<Object> getObjectList() {
        return objectList;
    }

    public static <T> Optional<T> getObject(Class<T> customClass) {
        //noinspection unchecked
        return ((Optional<T>) objectList.stream().filter(o -> o.getClass().equals(customClass)).findAny());
    }

    @SuppressWarnings("unchecked")
    public <T> T generateObject(@NotNull Class<T> clazz) {
        Optional<T> optobj = (Optional<T>) getObjectList().stream().filter(o -> o.getClass().equals(clazz)).findAny();
        if (optobj.isPresent())
            return optobj.get();
        T customObject = null;
        try {
            if (isInnerClass(clazz)) {
                System.out.println("Inner class " + clazz.getName());
                Optional<Constructor<?>> optConst = Arrays.stream(clazz.getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 1 && construct.getParameterTypes()[0] == clazz.getEnclosingClass()).findFirst();
                Optional<Constructor<?>> outerConst = Arrays.stream(clazz.getEnclosingClass().getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 0).findAny();
                if (optConst.isPresent() && outerConst.isPresent()) {
                    Object outer;
                    Optional<Object> optouter = getObjectList().stream().filter(obj -> obj.getClass().equals(clazz.getEnclosingClass())).findFirst();
                    if (optouter.isPresent()) {
                        outer = optouter.get();
                    } else {
                        outer = outerConst.get().newInstance();
                        getObjectList().add(outer);
                    }
                    optConst.get().setAccessible(true);
                    customObject = (T) optConst.get().newInstance(outer);
                } else {
                    System.out.println("No no-args constructor found for inner class " + clazz.getName() + ". Skipping...");
                }
            } else {
                Optional<Constructor<?>> optConst = Arrays.stream(clazz.getDeclaredConstructors()).filter(construct -> construct.getParameterCount() == 0).findFirst();
                if (optConst.isPresent()) {
                    optConst.get().setAccessible(true);
                    customObject = (T) optConst.get().newInstance();
                } else if (Modifier.isStatic(clazz.getModifiers())) {
                    System.out.println("Static classes are not supported yet. Don't worry about things not being initialized, it is all automatic.");
                } else {
                    System.out.println("No no-args constructor found for class " + clazz.getName() + ". Skipping...");
                }
            }

        } catch (InvocationTargetException e) {
            System.out.println("Cannot create a new class due to internal exception. Cause: " + e.getCause().getMessage() + "\nSkipping class " + clazz.getName() + "...");
            e.getCause().printStackTrace();
        } catch (Exception e) {
            System.out.println("Error loading class: " + e.getMessage());
            e.printStackTrace();
        }
        if (customObject != null)
            getObjectList().add(customObject);

        return customObject;
    }

    private String getCommand(String text) {
        int index = text.indexOf(' ');
        return index > -1 ? text.substring(1, index) : text.substring(1);
    }

    private String getRest(String text) {
        int index = text.indexOf(' ');
        return index > -1 ? text.substring(index + 1) : "";
    }

    class CommandListener implements MessageCreateListener {
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
                if (fullBase.containsKey(getCommand(e.getMessageContent()))) {
                    String command = getCommand(e.getMessageContent()).toLowerCase().trim();

                    DiscordBot.log(e, "Command Run - " + command, "EmbedMessage: " + e.getMessageContent());
                    CommandBase c = fullBase.get(command);

                    if (c.properties.isServerOnly()) {
                        if(!e.isServerMessage()){
                            Error.ServerOnly.send(e);
                            return;
                        }
                    }
                    if (c.properties.getPermissions().length > 0) {
                        if (e.getServer().isPresent()) {
                            if (e.getMessageAuthor().asUser().isPresent()) {
                                for (PermissionType permission : c.properties.getPermissions()) {
                                    if (!e.getServer().get().hasPermission(e.getMessageAuthor().asUser().get(), permission))
                                    {
                                        Error.NoPermission.send(e, permission.toString());
                                        return;
                                    }
                                }
                            } else {
                                Error.UserOnly.send(e);
                            }
                        } else {
                            Error.ServerOnly.send(e);
                        }
                    }
                    if(c.properties.getRoles().length > 0){
                        if (e.getServer().isPresent()) {
                            if (e.getMessageAuthor().asUser().isPresent()) {
                                for (String roleName : c.properties.getRoles()) {
                                    if (e.getServer().get().getRoles(e.getMessageAuthor().asUser().get())
                                            .stream().filter(role -> role.getName().equalsIgnoreCase(roleName)).count() > 0)
                                    {
                                        Error.NoRole.send(e, roleName);
                                        return;
                                    }
                                }
                            } else {
                                Error.UserOnly.send(e);
                            }
                        } else {
                            Error.ServerOnly.send(e);
                        }
                    }
                    if(!c.properties.isAsync())
                        runCommand(e,c);
                    else {
                        DUtils.getApi().getThreadPool().getExecutorService().execute(()->runCommand(e,c));
                    }
                } else {
                    DUtils.sendMessage(e, "I don't know this command!", "Please check command for any errors or use **!help**");
                }
            } else {
                messageCreateListeners.forEach(listener -> listener.onMessageCreate(e));
            }

        }
    }

    private void runCommand(MessageCreateEvent e, CommandBase c) {
        String[] args = getTokens(getRest(e.getMessageContent()));
        try {
            MethodInfo mw;
            Object o;
            if (c.runs.containsKey(args.length)) {
                mw = c.runs.get(args.length);
                o = mw.getMethod().invoke(mw.getObject(), combine(e, args));
            } else if (c.runs.containsKey(-1)) {
                mw = c.runs.get(-1);
                o = mw.getMethod().invoke(mw.getObject(), args, e);
            } else if (c.runs.containsKey(-2)) {
                mw = c.runs.get(-2);
                o = mw.getMethod().invoke(mw.getObject(), getRest(e.getMessageContent()), e);
            } else {
                DUtils.sendMessage(e, "Invalid arguments!", "The correct usage of this command is \"" + c.properties.getUsage() + "\"." + ((c.properties.getExample().isEmpty()) ? "" : " Ex. ``" + c.properties.getExample() + "``"));
                return;
            }
            if (o != null) {
                if (mw.getType().equals(ReturnType.String))
                    DUtils.sendMessage(e, StringUtils.capitalize(c.properties.getName()) + " Command Executed", (String) o);
                else if (mw.getType().equals(ReturnType.EmbedMessage)) {
                    EmbedMessage message = (EmbedMessage) o;
                    if (message.isValid()) {
                        if(message.isUsingEmbed())
                                DUtils.sendMessage(message.getEmbedBuilder(),e.getChannel());
                            else
                                DUtils.sendMessage(e, message.getTitle(), message.getMessage());
                    }
                }
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

    @Data
    private class InfoWrapper {
        private final ClassInfo routeClassInfo;
        private final ScanResult result;

    }

}
