package com.suredroid.discord;

import org.javacord.api.entity.message.embed.EmbedBuilder;

public class EmbedMessage {
    private String title, message;
    private boolean useEmbed = false;
    private EmbedBuilder embedBuilder;

    public EmbedMessage(String title, String message) {
        setTitle(title);
        setMessage(message);
    }

    public EmbedMessage(EmbedBuilder embedBuilder) {
        setEmbedBuilder(embedBuilder);
    }

    public EmbedMessage() {
    }

    public EmbedBuilder getEmbedBuilder() {
        return embedBuilder;
    }

    public void setEmbedBuilder(EmbedBuilder embedBuilder) {
        useEmbed = true;
        this.embedBuilder = embedBuilder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        useEmbed = false;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        useEmbed = false;
        this.message = message;
    }

    public boolean isValid(){
        return useEmbed ? embedBuilder != null : (title != null && message != null);
    }

    public boolean isUsingEmbed() {
        return useEmbed;
    }
}
