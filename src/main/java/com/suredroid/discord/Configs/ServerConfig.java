package com.suredroid.discord.Configs;

import java.util.HashMap;

public class ServerConfig implements Config {

    HashMap<String, String> loggingMap = new HashMap<>();
    HashMap<String, String> reportingMap = new HashMap<>();

    @Override
    public String getLoggingChannelId(String serverId) {
        return loggingMap.get(serverId);
    }

    @Override
    public String getReportingChannelId(String serverId) {
        return reportingMap.get(serverId);
    }


    public ServerConfig setLoggingChannelId(String serverId, String loggingChannelId) {
        loggingMap.put(serverId, loggingChannelId);
        return this;
    }

    public ServerConfig setReportingChannelId(String serverId, String reportingChannelId) {
        reportingMap.put(serverId, reportingChannelId);
        return this;
    }

    public void setBothChannelId(String serverId, String loggingChannelId, String reportingChannelId) {
        setLoggingChannelId(serverId, loggingChannelId);
        setReportingChannelId(serverId, reportingChannelId);
    }
}
