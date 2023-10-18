package dev.exhq.wedlock;

import io.javalin.Javalin;
import io.javalin.plugin.bundled.CorsPluginConfig;

public class Wedlock {
    public static void main(String[] args) {
        var marriageManager = new MarriageManager();
        marriageManager.registerMarriage(new Marriage("712639419785412668", "310702108997320705"));
        var app = Javalin.create(config -> {
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

                })
                .post("/v2/propose", (ctx) -> {
                    var user = ctx.queryParam("to");
                    var message = ctx.queryParam("msg");
                    if (user == null) {
                        ctx.status(400).json(new Failure("No userid provided"));
                    }
                    if (message == null) message = "Wanna fuck?";
                    var from = DiscordRequests.selfAuthenticate(ctx.header("Authorize"));
                    if (from == null) {
                        ctx.status(401).json(new Failure("Could not authenticate"));
                        return;
                    }
                    var proposal = Proposal.createProposal(from.user().id(), user, message);
                    marriageManager.insertProposal(proposal);
                    ctx.json(proposal);
                })
                .post("/v2/propose/accept", ctx -> {
                    ctx.queryParam("proposalid");
                })
                .post("/v2/propose/deny", ctx -> {
                    ctx.queryParam("proposalid");
                })
                .post("/v2/propose/view", ctx -> {

                })
                .error(404, ctx -> ctx.status(404).json(new Failure("Not found")))
                .error(500, ctx -> ctx.status(500).json(new Failure("Internal Error")))
                .start(8080);


    }
}
