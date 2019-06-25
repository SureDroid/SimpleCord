package com.suredroid.discord;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public enum Error {
    ServerOnly("Server Only", "This command can only be run on a server that I am in."),
    NoPermission("No Permission", "You do not have the permission to run this command."),
    InvalidArguments("Invalid Arguments","You have provided invalid arguments for this command.\nPlease use !help [command] to get the proper usage for this command."),
    IncorrectArgumentNumber("Incorrect Argument Number","You do not have the correct number of arguments to run this command.\nPlease use !help [command] to get the proper usage for this command."),
    RoleNotFound("Role Not Found","One of your arguments, a role, was not found on this server.\nPlease make sure you are using correct formatting (either an ID, Role name with an *@* before it, or mentioning it.)"),
    UserNotFound("User Not Found", "One of your arguments, a user, was not found. Please make sure you are either mentioning the user, providing the user's discriminated name, or providing the user's id."),
    //Audio
    NotPlayingTrack("Not Playing Anything","The bot isn't playing anything right now."),
    UserNotConnected("You Aren't Connected","You are not connected to a voice channel. To play a song, you must be in a voice channel."),

    //General
    NotANumber("Not A Number","One of the values that you provided should have been a number but is not.\nPlease use !help [command] to get the proper usage for this command."),
    FriendlyCancel("Canceling This Action","Alright \uD83D\uDE01, never mind!")
    ;

    String errorTitle, errorMessage;
    Error(String errorTitle, String errorMessage){
        this.errorTitle = errorTitle;
        this.errorMessage = errorMessage;
    }

    public void send(MessageCreateEvent e){
        DUtils.sendMessage(e,errorTitle,errorMessage);
    }

    public EmbedBuilder getEmbed(MessageCreateEvent e){
        return DUtils.createEmbed(e,errorTitle,errorMessage);
    }
}
