package dev.exhq.wedlock;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

import java.nio.charset.StandardCharsets;

public class Wedlock {
    public static void main(String[] args) {
        var marriageManager = new MarriageManager();
        marriageManager.registerMarriage(new Marriage("712639419785412668", "343383572805058560"));
        Javalin.create(config -> {
                    config.jsonMapper(GsonMapper.createGsonMapper());
                    config.plugins.enableCors(cors ->
                            cors.add(CorsPluginConfig::anyHost));
                })
                .get("/v2/marriage", (ctx) -> {
                    var user = ctx.queryParam("userid");
                    if (user == null) {
                        ctx.status(404).json(new Failure("No userid provided"));
                        return;
                    }
                    var marriage = marriageManager.getMarriage(user);
                    ctx.status(marriage == null ? 404 : 200);
                    if (marriage != null)
                        ctx.json(marriage);
                })
                .post("/v2/divorce", (ctx) -> {
                    var user = DiscordRequests.requireAuthentication(ctx);
                    var marriage = marriageManager.getMarriage(user.user().id());
                    if (marriage == null) {
                        ctx.status(404).json(new Failure("You are not married and cannot divorce"));
                        return;
                    }
                    marriageManager.unregisterMarriage(marriage);
                    ctx.status(200).json(marriage);
                })
                .post("/v2/propose", (ctx) -> {
                    var user = ctx.queryParam("to");
                    var message = ctx.queryParam("msg");
                    if (user == null) {
                        ctx.status(400).json(new Failure("No userid provided"));
                    }
                    if (message == null) message = "Wanna fornicate?";
                    var from = DiscordRequests.requireAuthentication(ctx);
                    var proposal = Proposal.createProposal(from.user().id(), user, message);
                    marriageManager.insertProposal(proposal);
                    ctx.json(proposal);
                })
                .post("/v2/propose/accept", ctx -> {
                    var user = DiscordRequests.requireAuthentication(ctx);
                    var proposalId = ctx.queryParam("proposalid");
                    if (proposalId == null) {
                        ctx.status(400).json(new Failure("No proposalid provided"));
                        return;
                    }
                    var proposal = marriageManager.getProposal(proposalId);
                    if (proposal == null) {
                        ctx.status(404).json(new Failure("No proposal found"));
                        return;
                    }
                    if (!proposal.to().equals(user.user().id())) {
                        ctx.status(403).json(new Failure("You cannot accept this proposal"));
                        return;
                    }
                    var marriage = marriageManager.acceptProposal(proposal);
                    if (marriage == null) {
                        ctx.status(400).json(new Failure("Could not accept proposal. Did you already accept this proposal before?"));
                        return;
                    }
                    ctx.json(marriage);
                })
                .post("/v2/propose/deny", ctx -> {
                    var user = DiscordRequests.requireAuthentication(ctx);
                    var proposalId = ctx.queryParam("proposalid");
                    if (proposalId == null) {
                        ctx.status(400).json(new Failure("No proposalid provided"));
                        return;
                    }
                    var proposal = marriageManager.getProposal(proposalId);
                    if (proposal == null) {
                        ctx.status(404).json(new Failure("No proposal found"));
                        return;
                    }
                    if (!proposal.to().equals(user.user().id())) {
                        ctx.status(403).json(new Failure("You cannot deny this proposal"));
                        return;
                    }
                    if (!marriageManager.denyProposal(proposal)) {
                        ctx.status(400).json(new Failure("Could not deny proposal. Did you already accept this proposal before?"));
                        return;
                    }
                    ctx.status(200).result("");
                })
                .get("/v2/propose/view", ctx -> {
                    var proposalId = ctx.queryParam("proposalid");
                    if (proposalId == null) {
                        ctx.status(400).json(new Failure("No proposalid provided"));
                        return;
                    }
                    var proposal = marriageManager.getProposal(proposalId);
                    if (proposal == null) {
                        ctx.status(404).json(new Failure("No proposal found"));
                        return;
                    }
                    ctx.status(200).json(proposal);
                })
                .get("/v2/propose/embed", ctx -> {
                    String html;
                    try(var inputStream = Wedlock.class.getResourceAsStream("embed.html")){
                        html = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                    }
                    ctx.status(200).html(html);
                })
                .exception(AuthenticationMissingException.class, (exception, ctx) -> ctx.status(401).json(new Failure("You need to log in first!")))
                .error(404, ctx -> ctx.status(404).json(new Failure("Not found")))
                .error(500, ctx -> ctx.status(500).json(new Failure("Internal Error")))
                .start(8080);


    }
}
