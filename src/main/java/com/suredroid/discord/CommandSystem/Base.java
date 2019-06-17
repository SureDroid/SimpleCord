package com.suredroid.discord.CommandSystem;

import java.util.HashMap;

public abstract class Base {
    public String command, description, usage, example;
    public boolean visible = true;
    public static HashMap<String,Base> list = new HashMap<>();
    public Base(String command, String description, String usage, String example){
        this.command = command;
        this.description = description;
        this.usage = usage;
        this.example = example;
        list.put(command,this);
    }
    public void set(String command, String description, String usage){
        list.remove(this.command);
        this.command = command;
        this.description = description;
        this.usage = usage;
        list.put(command,this);
    }

    /*
    @Override
    public boolean equals(Object o){
        if (o == this) {
            return true;
        }
        if (!(o instanceof String)) {
            return false;
        }
        String b = (String) o;
        return (b == command);
    }
    */
}
