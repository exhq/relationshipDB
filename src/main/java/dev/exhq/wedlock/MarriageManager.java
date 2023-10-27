package dev.exhq.wedlock;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class MarriageManager {
    Logger logger = LoggerFactory.getLogger("MarriageManager");
    private final Index<Marriage, String> marriageLookup = new Index<>(Marriage::top, Marriage::bottom);
    private final IndexedList<Marriage> marriages = new IndexedList<>(new ArrayList<>(), marriageLookup);
    private final Index<Proposal, String> proposalLookup = new Index<>(Proposal::id);
    private final IndexedList<Proposal> proposals = new IndexedList<>(new ArrayList<>(), proposalLookup);

    public synchronized void saveTo(File file) throws IOException {
        logger.info("Saving to " + file);
        Gson gson = new Gson();
        JsonObject storage = new JsonObject();
        storage.add("marriage", marriages.serialize(gson));
        storage.add("proposals", proposals.serialize(gson));
        try (var fos = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(storage, fos);
        }
    }

    public synchronized void loadFrom(File file) throws IOException {
        logger.info("Loading from " + file);
        if (!file.exists()) return;
        Gson gson = new Gson();
        JsonObject storage;
        try (var fis = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            storage = gson.fromJson(fis, JsonObject.class);
        } catch (JsonParseException | ClassCastException exception) {
            Path backupFile = file.toPath().resolveSibling(System.currentTimeMillis() + "-backup.json");
            logger.error("Could not load data from database saving to backup " + backupFile + ": ", exception);
            Files.copy(file.toPath(), backupFile);
            return;
        }
        if (storage == null) {
            logger.error("Loaded empty database file");
            return;
        }
        marriages.deserialize(gson, storage.get("marriage"), Marriage.class);
        proposals.deserialize(gson, storage.get("proposals"), Proposal.class);
    }


    public synchronized void insertProposal(@NotNull Proposal proposal) {
        proposals.add(proposal);
    }

    public synchronized @Nullable Proposal getProposal(@NotNull String proposalId) {
        return CollectionHelper.getFirstOrNull(proposalLookup.get(proposalId));
    }

    public synchronized boolean denyProposal(@NotNull Proposal proposal) {
        if (proposal.accepted()) return false;
        proposals.remove(proposal);
        proposals.add(new Proposal(proposal.from(), proposal.to(), proposal.message(), proposal.id(), true));
        return true;
    }

    public synchronized @Nullable Marriage acceptProposal(@NotNull Proposal proposal) {
        if (proposal.accepted()) return null;
        proposals.remove(proposal);
        proposals.add(new Proposal(proposal.from(), proposal.to(), proposal.message(), proposal.id(), true));
        Marriage marriage = new Marriage(proposal.to(), proposal.from());
        registerMarriage(marriage);
        return marriage;
    }


    public synchronized @Nullable Marriage getMarriage(@NotNull String user) {
        return CollectionHelper.getFirstOrNull(marriageLookup.get(user));
    }

    public synchronized boolean breakup(@NotNull String userid) {
        var marriages = marriageLookup.get(userid);
        for (Marriage marriage : marriages) {
            unregisterMarriage(marriage);
        }
        return !marriages.isEmpty();
    }

    public synchronized void unregisterMarriage(@NotNull Marriage marriage) {
        marriages.remove(marriage);
    }

    public synchronized void registerMarriage(@NotNull Marriage marriage) {
        breakup(marriage.bottom());
        breakup(marriage.top());

        marriages.add(marriage);
    }
}
