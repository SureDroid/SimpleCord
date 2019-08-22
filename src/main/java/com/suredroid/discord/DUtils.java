package com.suredroid.discord;

import com.suredroid.discord.Response.Hook;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.util.DiscordRegexPattern;
import org.javacord.api.util.event.ListenerManager;

import java.awt.*;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public class DUtils {
    //Discord Utils
    private static Color[] colors = {Color.cyan,
            Color.orange, Color.black, Color.darkGray, Color.gray,
            Color.green, Color.magenta, Color.pink, Color.red,
            Color.white, Color.yellow
    };

    private static Consumer<EmbedBuilder> modifyBase;
    private static Supplier<String> footerSupplier;
    private static String footerString;
    private static Supplier<EmbedBuilder> baseEmbed = new Supplier<EmbedBuilder>() {
        @Override
        public EmbedBuilder get() {
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(colors[new Random().nextInt(colors.length)]);
            if (modifyBase != null)
                modifyBase.accept(builder);
            if (footerSupplier != null)
                builder.setFooter(footerSupplier.get());
            else if (footerString != null)
                builder.setFooter(footerString);
            return builder;
        }
    };

    public static DiscordApi getApi(){
        return DiscordBot.getApi();
    }

    //Confirmation
    public static CompletableFuture<Boolean> confirm(MessageCreateEvent e, String action){
        return confirm(e.getChannel(),e.getMessageAuthor(),action);
    }
    public static CompletableFuture<Boolean> confirm(TextChannel channel, MessageAuthor author, String action){
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        long authorId = author.getId();
        channel.sendMessage(DUtils.createEmbed("Action Confirmation Required","Are you sure you want to **" + action + "**?",author.getName(),author.getAvatar()))
                .whenComplete((message, throwable) -> {
                    if(message==null){
                        completableFuture.completeExceptionally(throwable);
                        return;
                    }
                    message.addReactions("\uD83D\uDC4D","\uD83D\uDC4E");
                    long messageId = message.getId();
                    ListenerManager manager = message.addReactionAddListener(event -> {
                        if(!event.getUser().isBot()) {
                            if (event.getUser().getId() != authorId || !event.getEmoji().isUnicodeEmoji() || !(event.getEmoji().asUnicodeEmoji().get().equals("\uD83D\uDC4E")  || event.getEmoji().asUnicodeEmoji().get().equals("\uD83D\uDC4D"))) {
                                event.removeReaction();
                            } else {
                                Optional<Message> createdMessage = DUtils.getApi().getCachedMessageById(messageId);
                                createdMessage.ifPresentOrElse(value -> value.delete().whenComplete((aVoid, ex)->{
                                    if (event.getEmoji().equalsEmoji("\uD83D\uDC4D"))
                                        completableFuture.complete(true);
                                    else
                                        completableFuture.complete(false);
                                }),()->{
                                    if (event.getEmoji().equalsEmoji("\uD83D\uDC4D"))
                                        completableFuture.complete(true);
                                    else
                                        completableFuture.complete(false);
                                });
                            }
                        }
                    }).removeAfter(30,TimeUnit.SECONDS);
                    manager.addRemoveHandler(()->{
                        if(!completableFuture.isDone()){
                            channel.sendMessage(DUtils.createEmbed("Time Out Response", "I have been waiting for more than 30 seconds \uD83D\uDE25. Sadly, the time has come to go. Denying the confirmation."));
                            completableFuture.complete(false);
                        }
                    });
                });
        return completableFuture;
    }


    //Checking stuff
    public static boolean hasRole(long serverId, long userId, long roleId) {
        //return DUtils.getApi().getRoleById(roleId).map(messages -> messages.getUsers().contains(api.getUserById(userId))).orElse(false);
        return DUtils.getApi().getServerById(serverId).flatMap(server -> server.getRoleById(roleId)).map(role -> role.getUsers().stream().map(DiscordEntity::getId).anyMatch(id -> id == userId)).orElse(false);
        /*
        return DUtils.getApi().getServerById(serverId)
                .flatMap(server -> server.getRoleById(roleId))
                .map(Role::getUsers)
                .map(users -> users.stream().map(DiscordEntity::getId).anyMatch(id -> id == userId))
                .orElse(false);
        */
    }

    public static Optional<User> getUser(String userval) {
        Matcher match = DiscordRegexPattern.USER_MENTION.matcher(userval);
        if (match.find()) {
            return DUtils.getApi().getCachedUserById(match.group("id"));
        } else if (userval.contains("#")) {
            return DUtils.getApi().getCachedUserByDiscriminatedNameIgnoreCase(userval);
        } else if (userval.chars().allMatch(Character::isDigit)) {
            try {
                return DUtils.getApi().getCachedUserById(Long.parseUnsignedLong(userval));
            } catch (NumberFormatException nfe) {
                return Optional.empty();
            }
        } else return Optional.empty();
    }


    public static boolean isUser(MessageCreateEvent e, String userval) {
        Optional<User> user = getUser(userval);
        if (user.isPresent()) {
            return true;
        }
        sendMessage(e, "User not found.", "This user does not exist or is not on the server.");
        return false;
    }

    @SuppressWarnings("Duplicates")
    public static Optional<Role> getRole(String roleval) {
        Matcher match = DiscordRegexPattern.ROLE_MENTION.matcher(roleval);
        if (match.find()) {
            return DUtils.getApi().getRoleById(match.group("id"));
        } else if (roleval.charAt(0) == '@') {
            return DUtils.getApi().getRolesByNameIgnoreCase(roleval.substring(1)).stream().findAny();
        } else if (roleval.chars().allMatch(Character::isDigit) && (roleval.length() <= 20)) {
            try {
                return DUtils.getApi().getRoleById(Long.parseUnsignedLong(roleval));
            } catch (NumberFormatException nfe) {
                return Optional.empty();
            }
        } else return Optional.empty();
    }

    @SuppressWarnings("Duplicates")
    public static Optional<Role> getRole(String roleval, Server server) {
        Matcher match = DiscordRegexPattern.ROLE_MENTION.matcher(roleval);
        if (match.find()) {
            return server.getRoleById(match.group("id"));
        } else if (roleval.charAt(0) == '@') {
            return server.getRolesByNameIgnoreCase(roleval.substring(1)).stream().findAny();
        } else if (roleval.chars().allMatch(Character::isDigit) && (roleval.length() <= 20)) {
            try {
                return server.getRoleById(Long.parseUnsignedLong(roleval));
            } catch (NumberFormatException nfe) {
                return Optional.empty();
            }
        } else return Optional.empty();
    }


    // EmbedMessage Stuff

    public static void setFooter(String footer) {
        footerString = footer;
    }

    public static void setFooter(Supplier<String> footer) {
        footerSupplier = footer;
    }

    public static void setBaseChange(Consumer<EmbedBuilder> baseChange) {
        modifyBase = baseChange;
    }

    public static EmbedBuilder createEmbed() {
        return baseEmbed.get();
    }

    public static EmbedBuilder createEmbed(MessageCreateEvent e) {
        return createEmbed().setAuthor(e.getMessageAuthor());
    }

    public static EmbedBuilder createEmbed(String title, String message) {
        return createEmbed()
                .setTitle(title)
                .setDescription(message);
    }

    public static MessageBuilder createMessage(EmbedBuilder embed) {
        return new MessageBuilder().setEmbed(embed);
    }


    public static EmbedBuilder createEmbed(String title, String message, String username, Icon avatar) {
        return createEmbed(title, message)
                .setAuthor(username, null, avatar)
                .setTitle(title);
    }

    public static EmbedBuilder createEmbed(MessageCreateEvent e, String title, String message) {
        return createEmbed(title, message, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar());
    }

    public static MessageBuilder createMessage(MessageCreateEvent e, String title, String message) {
        return createMessage(title, message, e.getMessageAuthor().getDisplayName(), e.getMessageAuthor().getAvatar());
    }

    public static MessageBuilder createMessage(String title, String message, String username, Icon avatar) {
        return new MessageBuilder().setEmbed(createEmbed(title, message, username, avatar));
    }

    public static MessageBuilder createMessage(String title, String message, String extra, String username, Icon avatar) {
        return createMessage(title, message, username, avatar).append(extra);
    }

    public static MessageBuilder createMessage(String title, String message) {
        return new MessageBuilder().setEmbed(createEmbed(title, message));
    }

    public static CompletableFuture<Message> sendMessage(String title, String message, String username, Icon avatar, TextChannel channel) {
        return sendMessage(createMessage(title, message, username, avatar), channel);//.thenAccept((msg) -> ex.schedule(() -> msg.delete(),15,TimeUnit.SECONDS));
    }

    public static CompletableFuture<Message> sendMessage(String title, String message, String extra, String username, Icon avatar, TextChannel channel) {
        return sendMessage(createMessage(title, message, extra, username, avatar), channel);//.thenAccept((msg) -> ex.schedule(() -> msg.delete(),15,TimeUnit.SECONDS));
    }

    public static CompletableFuture<Message> sendMessage(MessageCreateEvent e, String title, String message) {
        //e.deleteMessage();
        return sendMessage(createMessage(title, message, e.getMessage().getAuthor().getName(), e.getMessage().getAuthor().getAvatar()), e.getChannel());
    }

    public static CompletableFuture<Message> sendMessage(MessageBuilder mb, TextChannel channel) {
        return mb.send(channel);
    }

    public static CompletableFuture<Message> sendMessage(EmbedBuilder eb, TextChannel channel) {
        return sendMessage(new MessageBuilder().setEmbed(eb), channel);
    }


    public static void sendTimedMessage(MessageBuilder mb, TextChannel channel) {
        mb.send(channel).thenAccept((msg) -> Hook.executors.schedule((Callable<CompletableFuture<Void>>) msg::delete, 15, TimeUnit.SECONDS));
    }


    public static boolean hasRole(MessageCreateEvent e, long roleId) {
        //return DUtils.getApi().getRoleById(roleId).map(messages -> messages.getUsers().contains(e.getMessageAuthor().asUser())).orElse(false);
        //return DUtils.getApi().getServerById(serverid).flatMap(server -> server.getRoleById(roleId)).map(messages -> messages.getUsers().contains(e.getMessageAuthor().asUser())).orElse(false);
        //return e.getServer().flatMap(server -> server.getRoleById(roleId)).map(messages -> messages.getUsers().stream().map(DiscordEntity::getId).anyMatch(id -> id == e.getMessageAuthor().getId())).orElse(false);
        return e.getMessageAuthor()
                .asUser()
                .flatMap(user ->
                        e.getServer()
                                .flatMap(server -> server.getRoleById(roleId))
                                .map(role -> role.getUsers().contains(user)))
                .orElse(false);
    }
}
