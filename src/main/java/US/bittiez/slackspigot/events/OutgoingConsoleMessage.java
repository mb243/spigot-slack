package US.bittiez.slackspigot.events;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.bukkit.configuration.file.FileConfiguration;

public class OutgoingConsoleMessage implements Runnable {

    private String formattedMessage;
    private SlackSession session;
    private SlackChannel chan;
    private FileConfiguration config;

    public OutgoingConsoleMessage(String formattedMessage, SlackSession session, SlackChannel chan, FileConfiguration fileConfiguration){

        this.formattedMessage = formattedMessage;
        this.session = session;
        this.chan = chan;
        config = fileConfiguration;
    }

    @Override
    public void run() {

        SlackPreparedMessage msg = new SlackPreparedMessage.Builder().withMessage(formattedMessage).build();
        SlackChatConfiguration chatConfig = SlackChatConfiguration.getConfiguration().withName("CONSOLE").withIcon(config.getString("console-avatar-url", "http://i65.tinypic.com/14jak2x.png"));
        session.sendMessage(chan, msg, chatConfig);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
