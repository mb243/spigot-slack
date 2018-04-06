package US.bittiez.slackspigot.events;

import com.ullink.slack.simpleslackapi.*;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutgoingMessage implements Runnable {

    private String formattedMessage;
    private Player player;
    private String username;
    private String icon;
    private SlackSession session;
    private SlackChannel chan;
    private FileConfiguration fileConfiguration;

    public OutgoingMessage(String formattedMessage, Player player, SlackSession session, SlackChannel chan, FileConfiguration fileConfiguration){

        this.formattedMessage = formattedMessage;
        this.player = player;
        this.session = session;
        this.chan = chan;
        this.fileConfiguration = fileConfiguration;
    }

    public OutgoingMessage(String formattedMessage, String username, String icon, SlackSession session, SlackChannel chan, FileConfiguration fileConfiguration){

        this.formattedMessage = formattedMessage;
        this.username = username;
        this.session = session;
        this.chan = chan;
        this.icon = icon;
        this.fileConfiguration = fileConfiguration;
    }

    private String convertMentions(String formattedMessage){
        final String regex = "@(.+.+)";

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(formattedMessage);

        while (matcher.find()) {
            //System.out.println("Full match: " + matcher.group(0)); //  @blahblah
            //System.out.println("Group 1: " + matcher.group(1));    //  blahblah

            for (SlackUser user : session.getUsers())
                if (user.getUserName().equalsIgnoreCase(matcher.group(1))) {
                    formattedMessage = formattedMessage.replace(matcher.group(0), "<@" + user.getId() + ">");
                    break;
                }
        }

        return formattedMessage;
    }

    @Override
    public void run() {
        formattedMessage = ChatColor.stripColor(formattedMessage);
        formattedMessage = convertMentions(formattedMessage);

        SlackPreparedMessage msg = new SlackPreparedMessage.Builder().withMessage(formattedMessage).build();
        SlackChatConfiguration config;
        if(player != null)
            config = SlackChatConfiguration.getConfiguration().withName(player.getName()).withIcon("https://www.mc-heads.net/avatar/" + player.getUniqueId());
        else
            config = SlackChatConfiguration.getConfiguration().withName(username).withIcon(icon);
        session.sendMessage(chan, msg, config);
        try {
            Thread.sleep(fileConfiguration.getInt("post-message-delay", 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
