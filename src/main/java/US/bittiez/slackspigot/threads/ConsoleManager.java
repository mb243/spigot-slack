package US.bittiez.slackspigot.threads;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class ConsoleManager extends AbstractAppender {

    private SlackChannel channel;
    private SlackSession session;
    private FileConfiguration config;

    static PatternLayout layout;
    static {
        PatternLayout.createDefaultLayout();
    }

    public void setConfig(FileConfiguration fileConfiguration){
        this.config = fileConfiguration;
    }

    public ConsoleManager(SlackChannel slackChannel, SlackSession slackSession, FileConfiguration fileConfiguration){
        super("Spigot-Slack Console", null, layout, false);
        channel = slackChannel;
        session = slackSession;
        config = fileConfiguration;
        try {
            ((Logger) LogManager.getRootLogger()).addAppender(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStarted(){
        return true;
    }

    @Override
    public void append(LogEvent logEvent) {

        String message = logEvent.getMessage().getFormattedMessage();
        if(config.getBoolean("console-time", true)){
            Date date = new Date(logEvent.getTimeMillis());
            DateFormat dateFormat = new SimpleDateFormat(config.getString("console-time-format", "[HH:mm:ss] "));
            message = dateFormat.format(date) + message;
        }

        sendMessageToSlack(message);
    }

    private void sendMessageToSlack(String message){
        if(message.length() < 1)
            return;
        message = ChatColor.stripColor(message); //Removes colors
        message = message.replaceAll("(\\[m)$", ""); //Replaces the weird [m at the end of console chat messages
        message = message.replaceAll("(\\[[0-9]*;[0-9]*;[0-9]*m)", ""); //Remove the console color code things that look like -> [0;32;1m

        List<String> filters = config.getStringList("console-regex-filter");
        if(filters.size() > 0)
            for(String filter : filters)
                if(!filter.isEmpty())
                    message = message.replaceAll(filter, "");

        SlackPreparedMessage msg = new SlackPreparedMessage.Builder().withMessage(message).build();
        SlackChatConfiguration chatConfig = SlackChatConfiguration.getConfiguration().withName("CONSOLE").withIcon(config.getString("console-avatar-url", "http://i65.tinypic.com/14jak2x.png"));
        session.sendMessage(channel, msg, chatConfig);
    }
}
