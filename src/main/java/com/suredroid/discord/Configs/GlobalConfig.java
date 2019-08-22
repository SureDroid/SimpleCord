package com.suredroid.discord.Configs;

public class GlobalConfig implements Config {
    private String loggingChannelId, reportingChannelId;

    public GlobalConfig(String loggingChannelId, String reportingChannelId) {
        this.loggingChannelId = loggingChannelId;
        this.reportingChannelId = reportingChannelId;
    }

    public GlobalConfig() { }

    @Override
    public String getLoggingChannelId(String serverId) {
        return loggingChannelId;
    }

    @Override
    public String getReportingChannelId(String serverId) {
        return reportingChannelId;
    }


    public GlobalConfig setLoggingChannelId(String loggingChannelId) {
        this.loggingChannelId = loggingChannelId;
        return this;
    }

    public GlobalConfig setReportingChannelId(String reportingChannelId) {
        this.reportingChannelId = reportingChannelId;
        return this;
    }
}
