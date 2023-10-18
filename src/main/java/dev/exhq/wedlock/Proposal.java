package dev.exhq.wedlock;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;

public record Proposal(
        String from,
        String to,
        String message,
        String id,
        boolean accepted
) {
    public static Proposal createProposal(String from, String to, String message) {
        return new Proposal(from, to, message, NanoIdUtils.randomNanoId(), false);
    }
}
