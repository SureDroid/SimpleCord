package com.suredroid.discord.CommandSystem;

import org.javacord.api.event.message.MessageCreateEvent;

@FunctionalInterface
public interface MessageConsumer {
    public void create(MessageCreateEvent e);
}

