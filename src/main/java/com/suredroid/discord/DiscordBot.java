package com.suredroid.discord;

import com.suredroid.discord.CommandSystem.CommandBase;
import com.suredroid.discord.CommandSystem.CommandListener;
import com.suredroid.discord.Configs.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.GloballyAttachableListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Optional;

import static com.suredroid.discord.DUtils.*;

public class DiscordBot {


    public static final Logger logger = LogManager.getRootLogger();
    public static boolean debug = false;
    private static DiscordApi api;
    private static String prefix = "!";
    static Config config;
    private static String storagePath = "./";


    private static CommandListener commandListener;

    public static DiscordApi start(String token) {
        return start(token, AccountType.BOT);
    }

    public static DiscordApi start(String token, Config newConfig) {
        config = newConfig;
        return start(token);
    }

    public static DiscordApi start(String token, AccountType type) {
        return start(new DiscordApiBuilder().setToken(token).setAccountType(type));
    }

    public static DiscordApi start(String token, AccountType type, Config newConfig) {
        config = newConfig;
        return start(token, type);
    }

    public static DiscordApi start(DiscordApiBuilder builder) {
        if (api != null) {
            System.out.println("Disconnecting the previous api.");
            api.disconnect();
        }
        System.out.println("Starting The Bot...");
        api = builder.login().join();
        System.out.println("Connected. Now Loading Classes...");
        commandListener = new CommandListener();
        api.addListener(commandListener);
        System.out.println("Loaded. Bot is ready to go!");
        return api;
    }

    public static void stop() {
        api.disconnect();
        api = null;
    }

    public static String getStoragePath() {
        return storagePath;
    }

    public static void setStoragePath(@NotNull String storagePath) {
        File loc = new File(storagePath);
        if(!loc.exists())
            loc.mkdir();
        DiscordBot.storagePath = storagePath;
    }

    public static void setPrefix(@NotNull String newPrefix){
        prefix = newPrefix;
    }

    public static String getPrefix(){
        return prefix;
    }

    public static DiscordApi getApi() {
        return api;
    }

    public static <T> Optional<T> getObject(Class<T> customClass) {
        return CommandBase.getObject(customClass);
    }

    public static void setConfig(Config newConfig) {
        config = newConfig;
    }

    public static Config getConfig() { return config; }

    //CommandListener Static Extensions

    public static <T extends GloballyAttachableListener> T addListener(T object) {
        return commandListener.addListener(object);
    }

    public static <T extends GloballyAttachableListener> T addListener(Class<T> clazz) {
        return commandListener.addListener(clazz);
    }

    public static <T> T generateObject(Class<T> clazz) {
        return commandListener.generateObject(clazz);
    }

    // Logging Functionality

    public static void warnError(MessageCreateEvent e) {
        sendMessage(e, "Internal Error", "Something in the bot went wrong. Don't worry, ChosenQuill has automatically been informed.");
        api.getTextChannelById(524830688797786123L).ifPresent(textChannel ->
                sendMessage("New Internal Error", "Internal error has been logged.", "<@217015504932438026> New Bot Error", e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar(), textChannel)
        );
    }

    public static void warnError() {
        report(new MessageBuilder().setEmbed(createEmbed("New Internal Error", "Internal error has been logged.")));
    }

    public static void report(MessageBuilder mb) {
        if (config != null && config.getReportingChannelId(null) != null)
            api.getTextChannelById(config.getReportingChannelId(null)).ifPresent(channel -> sendMessage(mb, channel));
    }

    public static void report(MessageBuilder mb, String serverId) {
        if (config != null && config.getReportingChannelId(serverId) != null)
            api.getTextChannelById(config.getReportingChannelId(serverId)).ifPresent(channel -> sendMessage(mb, channel));
    }

    public static void report(MessageCreateEvent e, String title, String message, String extra) {
        report(createMessage(title, message, extra, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar()));
    }

    public static void report(MessageCreateEvent e, String title, String message) {
        String serverId = e.getServer().isPresent() ? e.getServer().get().getIdAsString() : null;
        if (config != null && config.getReportingChannelId(serverId) != null)
            api.getTextChannelById(config.getReportingChannelId(serverId)).ifPresent(channel -> sendMessage(title, message, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar(), channel));
    }

    public static void log(MessageCreateEvent e, String title, String message, String extra) {
        String serverId = e.getServer().isPresent() ? e.getServer().get().getIdAsString() : null;
        if (config != null && config.getLoggingChannelId(serverId) != null)
            api.getTextChannelById(config.getLoggingChannelId(serverId)).ifPresent(channel -> sendMessage(title, message, extra, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar(), channel));
    }

    public static void log(MessageCreateEvent e, String title, String message) {
        String serverId = e.getServer().isPresent() ? e.getServer().get().getIdAsString() : null;
        if (config != null && config.getLoggingChannelId(serverId) != null)
            api.getTextChannelById(config.getLoggingChannelId(serverId)).ifPresent(channel -> sendMessage(title, message, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar(), channel));
    }




}
