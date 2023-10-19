package dev.exhq.wedlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MarriageManager {
    private final Index<Marriage, String> marriageLookup = new Index<>(Marriage::top, Marriage::bottom);
    private final IndexedList<Marriage> marriages = new IndexedList<>(new ArrayList<>(), marriageLookup);
    private final Index<Proposal, String> proposalLookup = new Index<>(Proposal::id);
    private final IndexedList<Proposal> proposals = new IndexedList<>(new ArrayList<>(), proposalLookup);

    public void insertProposal(@NotNull Proposal proposal) {
        proposals.add(proposal);
    }

    public @Nullable Proposal getProposal(@NotNull String proposalId) {
        return CollectionHelper.getFirstOrNull(proposalLookup.get(proposalId));
    }

    public boolean denyProposal(@NotNull Proposal proposal) {
        if (proposal.accepted()) return false;
        proposals.remove(proposal);
        proposals.add(new Proposal(proposal.from(), proposal.to(), proposal.message(), proposal.id(), true));
        return true;
    }

    public @Nullable Marriage acceptProposal(@NotNull Proposal proposal) {
        if (proposal.accepted()) return null;
        proposals.remove(proposal);
        proposals.add(new Proposal(proposal.from(), proposal.to(), proposal.message(), proposal.id(), true));
        Marriage marriage = new Marriage(proposal.to(), proposal.from());
        registerMarriage(marriage);
        return marriage;
    }


    public @Nullable Marriage getMarriage(@NotNull String user) {
        return CollectionHelper.getFirstOrNull(marriageLookup.get(user));
    }

    public boolean breakup(@NotNull String userid) {
        var marriages = marriageLookup.get(userid);
        for (Marriage marriage : marriages) {
            unregisterMarriage(marriage);
        }
        return !marriages.isEmpty();
    }

    public void unregisterMarriage(@NotNull Marriage marriage) {
        marriages.remove(marriage);
    }

    public void registerMarriage(@NotNull Marriage marriage) {
        breakup(marriage.bottom());
        breakup(marriage.top());

        marriages.add(marriage);
    }


}
