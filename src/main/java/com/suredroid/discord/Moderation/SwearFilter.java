package com.suredroid.discord.Moderation;

import java.util.ArrayList;

@FunctionalInterface
public interface SwearFilter {
    ArrayList<String> detect(String message);
}
