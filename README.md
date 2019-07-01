# SimpleCord [![Jitpack](https://jitpack.io/v/SureDroid/SimpleCord.svg)](https://jitpack.io/#SureDroid/SimpleCord) [![Discord](https://img.shields.io/discord/440681682799034408.svg?label=Discord%20Chat)](https://discord.suredroid.com)

Simplecord is [Javacord](https://github.com/Javacord/Javacord/) based framework dedicated to making the production of bots and commmands easier. 

#### Features:
- Custom Annotations (@Command, @Listener, @Create, etc.)
- Custom Discord Utilities (Confirmation Messages, Get User by Name, Id, or Mention, etc)
- Useful General Utilities (Writing and Reading Files/JSON, Get Formatted Date, Etc.)
- Command Tokenization and Parsing + Dynamic Injection
- Reply With Common User Errors
- **And So Much More.**

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
This is all you need. It checks the command the user sent has two numbers, then sends it.

### More Info Coming Soon...
