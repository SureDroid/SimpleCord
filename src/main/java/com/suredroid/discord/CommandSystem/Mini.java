package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.DiscordBot;

import java.util.HashMap;

public class Mini extends Base{
    public String title, message;
    public static HashMap<String, Mini> list = new HashMap<>();
    public Mini(String command, String description, String title, String message){
        super(command,description, DiscordBot.getPrefix() + command, "");
        this.title = title;
        this.message = message;
        list.put(command,this);
    }
}
