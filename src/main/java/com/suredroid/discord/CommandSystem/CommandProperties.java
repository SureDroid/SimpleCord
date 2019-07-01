package com.suredroid.discord.CommandSystem;

import com.suredroid.discord.DiscordBot;
import lombok.Data;
import org.javacord.api.entity.permission.PermissionType;

@Data
public class CommandProperties {
    private String name = "", desc = "", usage = "", example = "";
    private boolean hidden = false;

    private String[] aliases = {};
    private PermissionType[] permissions = {};
    private String[] roles = {};

    //Opt
    private boolean serverOnly =  false, async = false;

    public CommandProperties() {
    }

    public CommandProperties(String name, String desc, String usage, String example, boolean hidden, String[] aliases, PermissionType[] permissions, String[] roles, boolean serverOnly, boolean async) {
        setName(name);
        setDesc(desc);
        setUsage(usage);
        setExample(example);
        this.hidden = hidden;
        setAliases(aliases);
        setPermissions(permissions);
        setRoles(roles);
        this.serverOnly = serverOnly;
        this.async = async;
    }

    public CommandProperties setName(String name) {
        if(name == null)
            return this;
        if(name.isEmpty()) {
            pError("name","command");
            name = "command";
        }
        this.name = name.toLowerCase();
        return this;
    }

    public CommandProperties setUsage(String usage) {
        if(usage == null)
            return this;
        this.usage = DiscordBot.getPrefix() + name + " " + usage;
        return this;
    }

    public CommandProperties setDesc(String desc) {
        if(usage == null)
            return this;
        this.desc = desc;
        return this;
    }

    public CommandProperties setExample(String example) {
        if(example == null)
            return this;
        this.example = example;
        return this;
    }

    public CommandProperties setAliases(String[] aliases) {
        if(aliases == null)
            return this;
        this.aliases = aliases;
        return this;
    }

    public CommandProperties setPermissions(PermissionType[] permissions) {
        if(permissions == null)
            return this;
        this.permissions = permissions;
        return this;
    }

    public CommandProperties setRoles(String[] roles) {
        if(roles == null)
            return this;
        this.roles = roles;
        return this;
    }

    public void check(){
        if(desc.isEmpty()) {
            pError("desc(ription)","N/A");
            desc = "N/A";
        }

        if(usage.isEmpty()) {
            pError("usage","N/A");
            usage = "N/A";
        }
    }

    public void combine(CommandProperties other) {
        if(!name.equals(other.name)) {
            System.out.println("You cannot two command properties with different names: " + name + " and " + other.name + ". Skipping combination.");
            return;
        }
        if(!other.desc.isEmpty())
            this.desc = other.desc;
        if(!other.usage.isEmpty())
            this.usage = other.usage;
        if(!other.example.isEmpty())
            this.example = other.example;

        if(other.aliases.length != 0)
            this.aliases = other.aliases;
        if(other.permissions.length != 0)
            this.permissions = other.permissions;
        if(other.roles.length != 0)
            this.roles = other.roles;

        if(other.hidden)
            this.hidden = true;
        if(other.serverOnly)
            serverOnly = true;
        if(other.async)
            async = true;

    }

    private void pError(String name, String dVal){
        System.out.println("You must provide a default " + name + " value for " + this.name + ". Populating with default value '" + dVal + "' for now.");
    }
}
