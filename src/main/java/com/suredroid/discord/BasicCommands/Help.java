package com.suredroid.discord.BasicCommands;

import com.suredroid.discord.Annotations.Command;
import com.suredroid.discord.CommandSystem.CommandProperties;
import com.suredroid.discord.CommandSystem.CommandBase;
import com.suredroid.discord.DUtils;
import com.suredroid.discord.DiscordBot;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

@Command(desc = "A command to get the commands for this bot.",usage = "(command)",example = "!help")
public class Help {
    public void run(MessageCreateEvent e) {
            EmbedBuilder builder = DUtils.createEmbed();
            builder.setTitle("Require Help? I'm Here!");
            builder.setColor(Color.ORANGE);
            builder.setAuthor(e.getMessage().getAuthor().getName(), null, e.getMessage().getAuthor().getAvatar());
            builder.setAuthor(e.getMessage().getAuthor().getName(), null, e.getMessage().getAuthor().getAvatar());
            for(CommandBase base : CommandBase.getBaseList().values()){
                if(!base.properties.isHidden()) builder.addField("**" + DiscordBot.getPrefix() + base.properties.getName() + "**", base.properties.getDesc() + "\nUsage: " + base.properties.getUsage() + ((base.properties.getExample().isEmpty()) ? "" : " Ex. ``" + base.properties.getExample() + "``"));
            }
            new MessageBuilder().setEmbed(builder).send(e.getChannel());
    }
    public void run(MessageCreateEvent e, String commandName){
        commandName = commandName.toLowerCase();
        if(CommandBase.getBaseList().containsKey(commandName)){
            CommandProperties properties = CommandBase.getBaseList().get(commandName).properties;
            DUtils.sendMessage(e,"Help for command " + commandName + ".","**"+ properties.getDesc() + "**\nUsage: " + properties.getUsage() + ((properties.getExample().isEmpty()) ? "" : " Ex. ``" + properties.getExample() + "``"));
        }
        else DUtils.sendMessage(e,"This command does not exist.","Please do !help for a full list of all commands.");
    }

}