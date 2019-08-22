package com.suredroid.discord.Configs;

/**
 *
 */
public interface Config {
    String getLoggingChannelId(String serverId);
    String getReportingChannelId(String serverId);
}
