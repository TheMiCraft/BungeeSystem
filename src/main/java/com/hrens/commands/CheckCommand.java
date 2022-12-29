package com.hrens.commands;

import com.hrens.BungeeSystem;
import com.hrens.utils.LogManager;
import com.hrens.utils.UUIDFetcher;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CheckCommand extends Command {
    MongoCollection<Document> banned;
    public CheckCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        banned = BungeeSystem.getInstance().getMongodatabase().getCollection("bans");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        UUID senderUUID = sender instanceof ProxiedPlayer ? ((ProxiedPlayer) sender).getUniqueId() : null;
        if(!sender.hasPermission("bungeesystem.check")) {
            sender.sendMessage(BungeeSystem.getInstance().getMessage("notallowed"));
            return;
        }
        if (args.length != 1) {
            sender.sendMessage(BungeeSystem.getInstance().getMessage("checkformat"));
            return;
        }

        UUID target;
        if (UUIDFetcher.getUUID(args[0]) != null) {
            target = UUIDFetcher.getUUID(args[0]);
        } else if (args[0].matches("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/")
                && UUIDFetcher.getName(UUID.fromString(args[0])) != null) {
            target = UUID.fromString(args[0]);
        } else if (args[0].matches("-?\\d+") && banned.countDocuments(Filters.eq("_id", args[0])) != 0) {
            target = banned.find(Filters.eq("_id", args[0])).first().get("bannedUUID", UUID.class);
        } else{
            sender.sendMessage(BungeeSystem.getInstance().getMessage("checkformat"));
            return;
        }
        Collection<LogManager.LogEntry> log = BungeeSystem.getInstance().getLogManager().getLogByTarget(Objects.requireNonNull(target));
        List<String> check_log = log.stream().map(logEntry -> BungeeSystem.getInstance().getMessageString("checklog")
                .replace("{name}", Objects.nonNull(logEntry.getExecutor()) ? UUIDFetcher.getName(UUID.fromString(logEntry.getExecutor())) : "Console")
                .replace("{date}", new SimpleDateFormat(BungeeSystem.getInstance().getMessageString("dateformat")).format(new Date(logEntry.getTime())))
                .replace("{reason}", Objects.nonNull(logEntry.getReason()) ? logEntry.getReason() : "No Reason")
                .replace("{type}", logEntry.getType().toString())
        ).collect(Collectors.toList());
        sender.sendMessage(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("checkheader") + "\n" + String.join("\n", check_log)));
    }
}
