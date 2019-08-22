package com.suredroid.discord.Response;

import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Hook implements MessageCreateListener {

    public static final ScheduledThreadPoolExecutor executors = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(0);
    private static HashMap<Long, ListenerManager> managers = new HashMap<>();

    ScheduledFuture<?> timer;
    long userId;
    int timeoutLength;

    public Hook(User user){
        this(user,15);
    }

    public Hook(User user, int defaultTimeOutTime){
        timeoutLength = defaultTimeOutTime;
        userId = user.getId();
        managers.put(userId,user.addMessageCreateListener(this));
        setTimer(false,timeoutLength);
        onInit(user);
    }

    public static boolean hasUser(long userId){
        return managers.keySet().contains(userId);
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageContent().equalsIgnoreCase("stop") || event.getMessageContent().equalsIgnoreCase("exit")){
            close(false);
            return;
        }
        setTimer();
        onMessage(event);
    }

    protected abstract void onInit(User user);

    protected abstract void onMessage(MessageCreateEvent e);

    protected abstract void onFinish(boolean completed);

    protected void onTimeOut(){
        close(false);
    }

    private void setTimer(boolean restart, int time) {
        if (restart) {
            timer.cancel(true);
        }
        timer = executors.schedule(this::onTimeOut, time, TimeUnit.MINUTES);
    }

    protected void setTimer(){
        setTimer(true,timeoutLength);
    }
    protected void setTimer(int time){
        setTimer(true,time);
    }

    public void close(boolean completed) {
        onFinish(completed);
        timer.cancel(true);
        managers.remove(userId).remove();
    }

}
