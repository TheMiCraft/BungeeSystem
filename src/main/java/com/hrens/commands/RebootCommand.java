package com.hrens.commands;

import com.hrens.BungeeSystem;
import com.mongodb.client.MongoCollection;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;

import java.util.concurrent.TimeUnit;

public class RebootCommand extends Command {
    MongoCollection<Document> banned;

    public RebootCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        banned = BungeeSystem.getInstance().getMongodatabase().getCollection(BungeeSystem.getInstance().getConfig().getString("mongodb.bans"));
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if(!commandSender.hasPermission("bungeesystem.reboot")) return;
        try {
            int n = 10;
            int b = 10;
            for (int i = 1; i <= n; ++i) {
                BungeeSystem.getInstance().getProxy().broadcast(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("serverrestartseconds").replace("{seconds}", String.valueOf(b))));
                b--;
                TimeUnit.SECONDS.sleep(1);
            }

            for (ProxiedPlayer player : BungeeSystem.getInstance().getProxy().getPlayers()) {
                player.disconnect(BungeeSystem.getInstance().getMessage("serverisrestarting"));
            }
            TimeUnit.SECONDS.sleep(1);
            ProxyServer.getInstance().stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
