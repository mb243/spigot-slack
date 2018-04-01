package US.bittiez.slackspigot.events;

import US.bittiez.slackspigot.MessagePosted;
import com.google.gson.GsonBuilder;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageReceived implements Runnable {

    private SlackChannel channelOnWhichMessageWasPosted;
    private String messageContent;
    private SlackUser messageSender;
    private SlackSession session;
    private String jsonSource;
    private FileConfiguration config;
    private Server server;

    public MessageReceived(SlackMessagePosted event, SlackSession slackSession, FileConfiguration fileConfiguration, Server spigotServer) {
        channelOnWhichMessageWasPosted = event.getChannel();
        messageContent = event.getMessageContent();
        messageSender = event.getSender();
        session = slackSession;
        jsonSource = event.getJsonSource();
        config = fileConfiguration;
        server = spigotServer;
    }

    @Override
    public void run() {
        if (messageSender.getId().equals(session.sessionPersona().getId()))
            return;

        for (String id : config.getStringList("ignore-ids"))
            if (messageSender.getId().equals(id))
                return;

        if(channelOnWhichMessageWasPosted.getName().equals(config.getString("console-channel", ""))){
            if(config.getStringList("authorized-users").contains(messageSender.getId()))
                server.dispatchCommand(server.getConsoleSender(), messageContent);
            return;
        }

        MessagePosted mp = new GsonBuilder().create().fromJson(jsonSource, MessagePosted.class);
        if (mp.file != null) {
            if (config.getBoolean("ignore-attachments", false))
                return;
            messageContent = mp.file.name + ": " + mp.file.permalink;
        }

        List<String> channels = config.getStringList("incoming-channels");
        for (String chan : channels) {
            if (channelOnWhichMessageWasPosted.getName().equals(chan)) {
                String formattedMsg = config.getString("incoming-chat-format")
                        .replace("[DISPLAYNAME]", messageSender.getUserName())
                        .replace("[MSG]", messageContent)
                        .replace("[CHANNEL]", channelOnWhichMessageWasPosted.getName());
                formattedMsg = ChatColor.translateAlternateColorCodes('&', formattedMsg);
                if (!config.getBoolean("remove-mentions")) formattedMsg = convertMentionsToUser(formattedMsg);
                formattedMsg = parseMentions(formattedMsg);
                formattedMsg = formattedMsg.trim();
                server.broadcastMessage(formattedMsg);
                break;
            }
        }
    }

    private void handleConsoleCommand() {

    }

    private String convertMentionsToUser(String message) {
        final String regex = "<@([A-Za-z0-9]*)>";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(message);

        while (matcher.find()) {
            System.out.println("Full match: " + matcher.group(0));
            System.out.println("Group 1: " + matcher.group(1));

            for (SlackUser user : session.getUsers())
                if (user.getId().equals(matcher.group(1))) {
                    message = message.replace(matcher.group(0), "@" + user.getUserName());
                    break;
                }
        }

        return message;
    }

    private String parseMentions(String message) {
        final String regex = "(<@[A-Za-z0-9]*>)";
        message = message.replaceAll(regex, "");
        return message;
    }
}
