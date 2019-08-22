package com.suredroid.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {
    private static Date date = new Date();
    private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm aa");

    public static boolean isInteger(String s) {
        return isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        // if (s.isEmpty()) return false; Already Checked if Empty
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    public static Date getDate() {
        date.setTime(System.currentTimeMillis());
        return date;
    }

    public static String getDateFormatted() {
        return sdf.format(getDate());
    }


    private static final Pattern periodPattern = Pattern.compile("([0-9]+)([shdwmy])");
    public static Instant parsePeriod(String period){
        if(period == null) return null;
        period = period.toLowerCase(Locale.ENGLISH);
        Matcher matcher = periodPattern.matcher(period);
        Instant instant=Instant.EPOCH;
        while(matcher.find()){
            int num = Integer.parseInt(matcher.group(1));
            String typ = matcher.group(2);
            switch (typ) {
                case "s":
                    instant=instant.plus(Duration.ofSeconds(num));
                    break;
                case "m":
                    instant=instant.plus(Duration.ofMinutes(num));
                    break;
                case "h":
                    instant=instant.plus(Duration.ofHours(num));
                    break;
                case "d":
                    instant=instant.plus(Duration.ofDays(num));
                    break;
                case "w":
                    instant=instant.plus(Period.ofWeeks(num));
                    break;
                case "y":
                    instant=instant.plus(Period.ofYears(num));
                    break;
            }
        }
        return instant;
    }


    //File Stuff
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static Gson getGson() {
        return gson;
    }

    public static <T> Optional<T> getJson(String filename, T object){
        File file = new File(DiscordBot.getStoragePath() +  filename);
        if(file.exists()){
            try {
                FileReader fr = new FileReader(file);
                //noinspection unchecked
                return Optional.of((T) gson.fromJson(fr, object.getClass()));
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + filename);
            }
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getJson(String filename, Type type){
        File file = new File(DiscordBot.getStoragePath() + filename);
        if(file.exists()) try {
            FileReader fr = new FileReader(file);
            return Optional.of(gson.fromJson(fr, type));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filename);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getInternalJson(String filePath,  T object){
        try {
            InputStream stream = DiscordBot.class.getResourceAsStream(filePath);
            InputStreamReader reader = new InputStreamReader(stream);
            //noinspection unchecked
            return Optional.of((T) gson.fromJson(reader, object.getClass()));
        } catch (NullPointerException e) {
            System.out.println("File not found / Can't be read: " + filePath);
        }
        return Optional.empty();
    }

    public static <T> void writeJson(String fileName, T object){
        try {
            FileWriter writer = new FileWriter(DiscordBot.getStoragePath() + fileName);
            gson.toJson(object,writer);
            writer.close();
        } catch (IOException e1) {
            DiscordBot.warnError();
            DiscordBot.logger.error(e1.getMessage(),e1);
        }
    }

    public static <T> void writeJson(String fileName, T object, Type type){
        try {
            FileWriter writer = new FileWriter(DiscordBot.getStoragePath() + fileName);
            gson.toJson(object,type,writer);
            writer.close();
        } catch (IOException e1) {
            DiscordBot.warnError();
            DiscordBot.logger.error(e1.getMessage(),e1);
        }
    }



    public static Optional<String> readInternalFile(String path) {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(DiscordBot.class.getResourceAsStream(path),writer, StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            DiscordBot.logger.error(e.getMessage(),e);
            return Optional.empty();
        }
        return Optional.of(writer.toString());
    }

    public static boolean writeFile(String fileName, String data) {
        return writeFile(fileName, data, false);
    }
    public static boolean writeFile(String filename, String data, boolean append) {
        File file = new File(DiscordBot.getStoragePath() + filename);
        try {
            FileUtils.writeStringToFile(file, data, StandardCharsets.UTF_8, append);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
