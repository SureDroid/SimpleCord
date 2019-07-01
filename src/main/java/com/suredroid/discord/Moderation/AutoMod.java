package com.suredroid.discord.Moderation;

import com.suredroid.discord.DUtils;
import com.suredroid.discord.DiscordBot;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAttachment;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.listener.message.MessageEditListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AutoMod implements MessageCreateListener, MessageEditListener {

    private SwearFilter filter;

    public AutoMod(){
        super();
    }

    public void setFilter(SwearFilter filter){
        this.filter = filter;
    }

    private ExecutorService executor = Executors.newCachedThreadPool();


    @Override
    public void onMessageCreate(MessageCreateEvent e) {
        executor.execute(() -> detect(e));
    }
    @Override
    public void onMessageEdit(MessageEditEvent e) { executor.execute(() -> detect(e)); }

    private void detect(MessageEditEvent e){
        e.getMessage().ifPresent(message->{
            if(!message.getAuthor().isYourself() && message.getAuthor().isUser())
                detect(message);
        });
    }
    private void detect(MessageCreateEvent e){
        if(!e.getMessageAuthor().isYourself() && e.getMessageAuthor().isUser())
            detect(e.getMessage());
    }

    private void detect(Message message) {
        if (message.getAuthor().isYourself()) {
            return;
        }
        String input = (message.getAttachments().size() > 0) ? message.getContent() + "\n" + message.getAttachments().stream().map(MessageAttachment::getFileName).collect(Collectors.joining(", ")) : message.getContent();

        ArrayList<String> badWords = filter.detect(input);

        if (badWords.size() > 0) {
            String send = "EmbedMessage: " + input + "\n Bad Words: " + Arrays.toString(badWords.toArray());
            String serverId = message.getServer().isPresent() ? message.getServer().get().getIdAsString() : null;
            DiscordBot.report(DUtils.createMessage("Swear Word Blocked", send, message.getAuthor().getDiscriminatedName(), message.getAuthor().getAvatar()), serverId);
            message.getAuthor().asUser().ifPresent(user -> {
                user.sendMessage(DUtils.createEmbed("Your EmbedMessage Contains Swear Words", "To use your message, remove all profanity from your message.\nYour EmbedMessage is Below (For Copy Paste)\nBad Words: " + Arrays.toString(badWords.toArray()), user.getName(), user.getAvatar()));
                user.sendMessage(message.getContent());
            });
            message.delete();

        }

        if (message.getMentionedUsers().size() > 5) {
            DUtils.sendMessage(DUtils.createEmbed("Do not mass mention.", "Mentioning 5+ people is not permitted, and repeated use can result in a ban.", message.getAuthor().getDiscriminatedName(), message.getAuthor().getAvatar()), message.getChannel());
            String serverId = message.getServer().isPresent() ? message.getServer().get().getIdAsString() : null;
            DiscordBot.report(DUtils.createMessage("Mass Mention Report", "EmbedMessage: " + input), serverId);
        }
    }


}

