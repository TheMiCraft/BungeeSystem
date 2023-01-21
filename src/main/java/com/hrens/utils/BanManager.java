package com.hrens.utils;

import com.hrens.BungeeSystem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Duration;
import java.util.*;

public class BanManager {
    private final MongoCollection<Document> banned;

    public BanManager() {
        banned = BungeeSystem.getInstance().getMongodatabase().getCollection("bans");
    }

    public void mute(UUID uuid, int reason, UUID moderator) {
        int _id = generateId();
        String reasonAsString = BungeeSystem.getInstance().getConfig().getString("mute." + reason + ".reason");
        ProxiedPlayer p = BungeeSystem.getInstance().getProxy().getPlayer(uuid);
        long l = System.currentTimeMillis() + BungeeSystem.getInstance().getConfig().getLong("mute." + reason + ".time");
        Document document = new Document()
                .append("_id", _id)
                .append("bannedUUID", uuid.toString())
                .append("moderatorUUID", Objects.nonNull(moderator) ? moderator.toString() : null)
                .append("reason", reason)
                .append("timestamp", System.currentTimeMillis())
                .append("end", (BungeeSystem.getInstance().getConfig().getLong("mute." + reason + ".time")!=-1L) ? l : -1L)
                .append("type", "mute");
        banned.insertOne(document);
        ProxiedPlayer player = BungeeSystem.getInstance().getProxy().getPlayer(uuid);
        if(player != null){
            String s = duration(reason, "mute");
            player.sendMessage(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("mutemessage")
                    .replace("{id}", String.valueOf(_id))
                    .replace("{reason}", reasonAsString)
                    .replace("{duration}", s.toLowerCase())));
        }
        LogManager.LogEntry logEntry = new LogManager.LogEntry(LogManager.LogEntry.LogType.MUTE, Objects.nonNull(moderator) ? moderator.toString() : null, uuid.toString(), System.currentTimeMillis(), reasonAsString);
        BungeeSystem.getInstance().getLogManager().addEntry(logEntry);
    }

    private String duration(int reason, String type) {
        long l = BungeeSystem.getInstance().getConfig().getLong(type + "." + reason + ".time");
        if(l != -1L) {
            Duration diff = Duration.ofMillis((System.currentTimeMillis() + l) - System.currentTimeMillis());
            return BungeeSystem.getInstance().getMessageString("bandateformat")
                    .replace("{d}", String.valueOf(diff.toDays()))
                    .replace("{h}", String.valueOf(diff.toHours() % 24))
                    .replace("{m}", String.valueOf(diff.toMinutes() % 60))
                    .replace("{s}", String.valueOf((diff.toMillis() / 1000) % 60));
        } else {
            return BungeeSystem.getInstance().getMessageString("permanent");
        }
    }

    public void ban(UUID uuid, int reason, UUID moderator) {
        int _id = generateId();
        String reasonAsString = BungeeSystem.getInstance().getConfig().getString("ban." + reason + ".reason");
        ProxiedPlayer player = BungeeSystem.getInstance().getProxy().getPlayer(uuid);
        if(player != null){
            String s = duration(reason, "ban");
            player.disconnect(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("banmessage")
                    .replace("{id}", String.valueOf(_id))
                    .replace("{reason}", reasonAsString)
                    .replace("{duration}", s.toLowerCase())));
        }
        String executor = Objects.nonNull(moderator) ? moderator.toString() : null;
        long l = System.currentTimeMillis() + BungeeSystem.getInstance().getConfig().getLong("ban." + reason + ".time");
        Document document = new Document()
                .append("_id", _id)
                .append("bannedUUID", uuid.toString())
                .append("moderatorUUID", executor)
                .append("reason", reason)
                .append("timestamp", System.currentTimeMillis())
                .append("end", (BungeeSystem.getInstance().getConfig().getLong("ban." + reason + ".time")!=-1L) ? l : -1L)
                .append("type", "ban");
        banned.insertOne(document);

        LogManager.LogEntry logEntry = new LogManager.LogEntry(LogManager.LogEntry.LogType.BAN, executor, uuid.toString(), System.currentTimeMillis(), reasonAsString);
        BungeeSystem.getInstance().getLogManager().addEntry(logEntry);
    }

    public void warn(UUID uuid, String reason, UUID moderator){
        int _id = generateId();
        ProxiedPlayer player = BungeeSystem.getInstance().getProxy().getPlayer(uuid);
        if(player != null){
            player.sendMessage(TextComponent.fromLegacyText(BungeeSystem.getInstance().getMessageString("warnmessage")
                    .replace("{id}", String.valueOf(_id))
                    .replace("{reason}", Objects.nonNull(reason) ? reason : BungeeSystem.getInstance().getMessageString("noreason"))));
        }
        String executor = Objects.nonNull(moderator) ? moderator.toString() : null;
        Document document = new Document()
                .append("_id", _id)
                .append("bannedUUID", uuid.toString())
                .append("moderatorUUID", executor)
                .append("reason", reason)
                .append("timestamp", System.currentTimeMillis())
                .append("end", null)
                .append("type", "warn");
        banned.insertOne(document);

        LogManager.LogEntry logEntry = new LogManager.LogEntry(LogManager.LogEntry.LogType.WARN, executor, uuid.toString(), System.currentTimeMillis(), reason);
        BungeeSystem.getInstance().getLogManager().addEntry(logEntry);
    }

    public boolean isMuted(UUID uuid) {
        Bson b = Filters.and(Filters.eq("bannedUUID", uuid.toString()), Filters.eq("type", "mute"), Filters.or(Filters.not(Filters.lt("end", System.currentTimeMillis())), Filters.eq("end", -1)) );
        if(Objects.nonNull(banned.find(b).first())) return true;
        return false;
    }

    public boolean isBanned(UUID uuid) {
        Bson b = Filters.and(Filters.eq("bannedUUID", uuid.toString()), Filters.eq("type", "ban"), Filters.or(Filters.not(Filters.lt("end", System.currentTimeMillis())), Filters.eq("end", -1)) );
        if(Objects.nonNull(banned.find(b).first())) return true;
        return false;
    }

    public List<Integer> getBanIds() {
        return BungeeSystem.getInstance().getConfig().getIntList("banids");
    }

    public List<Integer> getMuteIds() {
        return BungeeSystem.getInstance().getConfig().getIntList(("muteids"));
    }

    public boolean canBan(CommandSender commandSender, int id) {
        String permisson = BungeeSystem.getInstance().getConfig().getString("ban." + id + ".permission");
        if (permisson == null) return true;
        return commandSender.hasPermission(permisson);
    }

    public boolean canMute(CommandSender commandSender, int id) {
        String permisson = BungeeSystem.getInstance().getConfig().getString("mute." + id + ".permission");
        if (permisson == null) return true;
        return commandSender.hasPermission(permisson);

    }

    public void unban(int _id, UUID moderator, UUID target, String reason) {
        if (banned.find(Filters.eq("_id", _id)).first() == null || banned.find(Filters.eq("_id", _id)).first().isEmpty())
            return;
        if (banned.find(Filters.eq("_id", _id)).first().getString("type").equals("ban"))
            banned.deleteOne(banned.find(Filters.eq("_id", _id)).first());

        LogManager.LogEntry logEntry = new LogManager.LogEntry(LogManager.LogEntry.LogType.UNBAN, Objects.nonNull(moderator) ? moderator.toString() : null, target.toString(), System.currentTimeMillis(), reason);
        BungeeSystem.getInstance().getLogManager().addEntry(logEntry);
    }

    public void unmute(int _id, UUID moderator, UUID target, String reason) {
        if (banned.find(Filters.eq("_id", _id)).first() == null || banned.find(Filters.eq("_id", _id)).first().isEmpty())
            return;
        if (banned.find(Filters.eq("_id", _id)).first().getString("type").equals("mute"))
            banned.deleteOne(banned.find(Filters.eq("_id", _id)).first());
        LogManager.LogEntry logEntry = new LogManager.LogEntry(LogManager.LogEntry.LogType.UNMUTE, Objects.nonNull(moderator) ? moderator.toString() : null, target.toString(), System.currentTimeMillis(), reason);
        BungeeSystem.getInstance().getLogManager().addEntry(logEntry);
    }


    public int generateId() {
        Random random = new Random();
        int i = random.nextInt(1000000);
        if (banned.countDocuments(Filters.eq("_id", i)) != 0) {
            while (banned.find().into(new ArrayList<>()).contains(i)) {
                i = random.nextInt(1000000);
            }
        }
        return i;
    }

}
