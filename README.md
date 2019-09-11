# SimpleCord [![Jitpack](https://jitpack.io/v/SureDroid/SimpleCord.svg)](https://jitpack.io/#SureDroid/SimpleCord) [![Discord](https://img.shields.io/discord/440681682799034408.svg?label=Discord%20Chat)](https://discord.suredroid.com)

Simplecord is [Javacord](https://github.com/Javacord/Javacord/) based framework dedicated to making the production of bots and commmands easier. 

**Notice: Since SimpleCord is still in development, you may experience api breaking changed.**

#### Features:
- Custom Annotations (@Command, @Listener, @Create, etc.)
- Custom Discord Utilities (Confirmation Messages, Get User by Name, Id, or Mention, etc)
- Useful General Utilities (Writing and Reading Files/JSON, Get Formatted Date, Etc.)
- Command Tokenization and Parsing + Dynamic Injection
- Class Scanning and Automatic Command Implementation
- Common User Errors, Easy SwearFiltering
- **And So Much More.**

### [How To Use](#use)
The first thing you should note is that all of DiscordBot is static. But, none of the objects will be created until you start it.

You can start it by using ``DiscordBot#Start``. When you start it, it will first connect to your bot using a provided token. Then, it will start looking for the custom annotations and register them. Note, if you use the json storage and file utilities of SimpleCord, you will need to specify the location beforehand. This also goes for anything your commands may use before you start. For this reason, I prefer to setup the parameters for DiscordBot beforehand.

Its as simple as that. With one method, it automatically registers your @Commands and detects and forwards the events.

### [Add To Project](#add)
SimpleCord is published through [JitPack](https://jitpack.io/#SureDroid/SimpleCord/master-SNAPSHOT). Click the link to figure out how to add it to your project. You also need to add [jcenter](https://bintray.com/bintray/jcenter) to your project's repository centers. 

Here is a gradle implementation.
```$xslt
repositories {
    jcenter()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.SureDroid:SimpleCord:master-SNAPSHOT'
}
```

### [Commands](#commands)

Lets say you want to create a simple **multiply** command, which multiplies two user provided numbers together, and tells you the product.
```java
    @Command(desc="Multiplies two numbers",usage = "firstNumber secondNumber")
    public void multiply(MessageCreateEvent e, String numberOne, String numberTwo)
    {
        if(CommonUtils.isInteger(numberOne) && CommonUtils.isInteger(numberTwo)) {
            DUtils.sendMessage(e,"Multiplied number " + numberOne + " by " + numberTwo,
                    "The result is " + (Integer.parseInt(numberOne) * Integer.parseInt(numberTwo)));
        } else {
            Error.NotANumber.send(e);
        }
    }
```
This is all you need. It checks the command the user sent has two tokens, checks if both are integers, then sends you the result.

![Example Image](https://image.prntscr.com/image/KwHUQbH2QjuSpoKXrOG2Xg.png)

Now lets say we want to multiply however many arguments the user provided. We can do that as well. All you have to do is switch the Place MessageCreateEvent with the String, and change the String to a String array.
```java
    @Command(desc="Multiplies how many ever numbers",usage = "firstNumber secondNumber thirdNumber ....")
    public void multiply(String[] args, MessageCreateEvent e)
    {
        // Implementaiton Hidden....
    }
```

You can provide as many variants of arguments as you would like. It goes from checking if method available with the exact number of arguments, then checking if there is a method that accepts all arguments.

Now you may have noticed that we provide a description and a usage for our commands. While this is not required, it allows us to use an already provided Help command.

![Example Image](https://image.prntscr.com/image/QQv-6UeBQkOemNGK4GFWcA.png)

It also allows you to list all commands by just typing ``!help``. You can also access the description and usage and use it for your needs.

Now lets say your method is complex, and needs to be contained in a class. Well you can do that, just slap an @Command annotation on the class, and it will be registered.
The difference here is that the methods you want to be registered by the command must be named "run".
```java
@Command(desc = "A command for testing purposes", usage = "!test {...}", aliases = {"t", "testing"}, serverOnly = true, roles = "tester")
public class Test {
    public String run(MessageCreateEvent e, String argumentOne) {
        return "One argument test";
    }
    public void run(MessageCreateEvent e, String argumentOne, String argumentTwo) {
        DUtils.sendMessage(e,"Two Argument Test","This is a two argument Test");
    }
    public EmbedMessage run(String[] args, MessageCreateEvent e) {
        return new EmbedMessage("All argument Test","This is an all argument test"); //(Title,Message)
    }
}
```

There are two other things to notice here. 
1. The command has a wide variety of parameters that can be given. Here we provided a description and usage, provided some aliases (shortcut names), told it to only work on a server, and only a user with the role name of tester can use it. There are even more parameters that you can specify to customize the command. 
(Also note I set serverOnly to true for demonstration, but its not needed. In this case, it will automatically check if you are part of a server since we are checking for a role (and will also work for a permissions check)).
2. You can return three different values. A EmbedMessage, String, or void. By returning an EmbedMessage or String, it will send your message automatically. Note if you provide null, it doesn't send anything.

You can also delete and create commands during runtime. Grab the commandManager after the bot started using ``DiscordBot#getCommandManager``, and you will get access to a variety of methods to create objects, add commands and listeners, and more. When creating a command at runtime, you can either link an existing class that has the Command annotation, or provide an object, and provide the CommandProperties for it.

**This concludes the main part of what you need to know. There are a lot of additional features which I still haven't covered. I will try to summerize most of them, but check out the wiki for full details on everything.**

### [Annotations](#annotations)
There are three custom annotations SimpleCord provides. The @Command annotation is shown in use above. You can put it on any method or class, as it will be registered as a command as long as you have a valid format (metioned above). 
You can also slap an @Listener and @Create annotation on any class. @Listener will automatically create your class and add it as a Global Listener, and @Create will just create your class for you on startup. (Useful if you are too lazy to initialize it yourself somewhere).

### [Logging/Reporting](#logging)
By default, SimpleCord provides a way to log and report, as well as logs command usage. You can do this by setting the config implementation. We provide two versions, a global logging for all servers, or server specific. If they don't fit your needs, you can create your own config implementation. Remember that if you use the ``DiscordBot#log`` or ``DiscordBot#report`` without providing a serverid, a null value will be provided.

#### Congrats, you reached the end!
Check out the wiki for more information on everything that simplecord provides and try adding it to your project and experimenting around. Have fun 👍