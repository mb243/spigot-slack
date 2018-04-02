package US.bittiez.slackspigot.events;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

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

    @Override
    public void run() {
        formattedMessage = ChatColor.stripColor(formattedMessage);

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
