# spigot-slack
Slack plugin for Spigot

## Configuration file

```
# Bot User OAuth Access Token:
# You will need to have configured a bot user and installed your app first
slack-bot-token: ""

chat-channel: ""
chat-format: "[PLAYER]: [MSG]"
first-join-format: "Welcome [PLAYER] to the server!"
normal-join-format: "[PLAYER] has logged in."
player-quit-format: "[PLAYER] has left the server."
player-advancement-format: "[PLAYER] has earned the advancement: [ADVANCEMENT]"

#These are the slack channels that will be sent to the server chat:
incoming-channels: ["chan1", "chan2"]
incoming-chat-format: "[[CHANNEL]][DISPLAYNAME]: [MSG]"
ignore-ids: ["BAAAAAAQ"]
```

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

