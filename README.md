# spigot-slack
Slack plugin for Spigot

## Configuration file

The default configuration file is available [here](https://github.com/mb243/spigot-slack/blob/master/src/main/resources/config.yml).

## Ignoring the bot's own messages

To do this, you will need to tell the bot to ignore itself on Slack, using its bot ID. To get the ID, right-click on a message that the bot posts in the channel, and click **Copy Link**. You will get a link similar to the following on your clipboard:

```
https://workspace.slack.com/services/BAAAAAAQ
```

The bot ID is the very last part of that URL: `BAAAAAAQ`. Simply add this to the `ignore-ids`, like so:

```
ignore-ids: ["BAAAAAAQ"]
```


## Commands

`/ssreload`: Reload the plugin config file

## Permissions

`spigotslack.ignore`: Prevent messages from being sent to Slack

`spigotslack.silent`: Prevent join/quit messages from being sent to Slack

`spigotslack.reload`: Use `/ssreload`
