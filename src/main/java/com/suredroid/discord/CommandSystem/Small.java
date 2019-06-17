package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.DiscordBot;

import java.util.HashMap;

public class Small extends Base {
    MessageConsumer consumer;
    public static HashMap<String, Small> list = new HashMap<>();
    public Small(String command, String description, MessageConsumer consumer){
        this(command,description, DiscordBot.getPrefix() +command,consumer);
    }
    public Small(String command, String description, String usage, MessageConsumer consumer){
        super(command,description,usage, "");
        this.consumer = consumer;
        list.put(command,this);
    }
}