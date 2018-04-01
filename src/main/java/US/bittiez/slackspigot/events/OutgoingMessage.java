package US.bittiez.slackspigot.events;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class OutgoingMessage implements Runnable {

    private String formattedMessage;
    private Player player;
    private SlackSession session;
    private SlackChannel chan;

    public OutgoingMessage(String formattedMessage, Player player, SlackSession session, SlackChannel chan){

        this.formattedMessage = formattedMessage;
        this.player = player;
        this.session = session;
        this.chan = chan;
    }

    @Override
    public void run() {
        formattedMessage = ChatColor.stripColor(formattedMessage);

        SlackPreparedMessage msg = new SlackPreparedMessage.Builder().withMessage(formattedMessage).build();
        SlackChatConfiguration config = SlackChatConfiguration.getConfiguration().withName(player.getName()).withIcon("https://www.mc-heads.net/avatar/" + player.getUniqueId());
        session.sendMessage(chan, msg, config);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
