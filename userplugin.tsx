/*
 * RelationshipDB user plugin
 * Copyright (c) 2023 Linnea Gräf and Exhq
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import { ApplicationCommandInputType, ApplicationCommandOptionType } from "@api/Commands";
import { DataStore } from "@api/index";
import { Devs } from "@utils/constants";
import { sendMessage } from "@utils/discord";
import { openModal } from "@utils/modal";
import { useAwaiter } from "@utils/react";
import definePlugin from "@utils/types";
import { findByProps, findByPropsLazy } from "@webpack";
import { showToast, UserStore } from "@webpack/common";

const OAuth = findByPropsLazy("OAuth2AuthorizeModal");

const WL_HOSTNAME = "https://wedlock.exhq.dev";

function getTokenStorageKey(): string {
    const userId = UserStore.getCurrentUser().id;
    const key = "relationshipDB_" + userId;
    return key;
}

async function getAuthorizationToken(): Promise<string> {
    const key = getTokenStorageKey();
    const savedToken = await DataStore.get(key);
    if (savedToken) {
        return savedToken;
    }
    return new Promise(resolve => {
        openModal(props =>
            <OAuth.OAuth2AuthorizeModal

                {...props}
                scopes={["identify"]}
                responseType="token"
                clientId="394100837032394763"
                cancelCompletesFlow={false}
                callback={async (response: { location: string; }) => {
                    const callbackUrl = (response.location);
                    const query = callbackUrl.split("#")[1];
                    const params = new URLSearchParams(query);
                    const newToken = params.get("access_token")!!;
                    await DataStore.set(key, newToken);
                    resolve(newToken);
                }}
            ></OAuth.OAuth2AuthorizeModal>);
    });

}

async function fetchWedlock(method: "GET" | "POST", url: string, params?: Record<string, string>, hasRetried?: boolean): Promise<any | null> {
    const response = await fetch("http://localhost:8080/" + url + (params ? "?" + new URLSearchParams(params) : ""), {
        method: method,
        headers: method === "POST" ? {
            authorization: await getAuthorizationToken()
        } : {}
    });
    if (response.status === 401) {
        await DataStore.del(getTokenStorageKey());
        if (!hasRetried) {
            return await fetchWedlock(method, url, params, true);
        }
    }
    const jsonResponse = await response.json();
    if ("reason" in jsonResponse || response.status !== 200) {
        showToast("failed to edate for now:" + jsonResponse.reason);
        return null;
    }
    return jsonResponse;
}

export default definePlugin({
    guh(i) {
        const partner = async () => {
            const res = await fetchWedlock("GET", `/v2/marriage?userid=${i}`).then(r => r.json());
            if (!res) {
                return "the fucking server is down";
            }
            if (res.reason === "Not found") {
                console.log(true);
                return true;
            } else {
                console.log(res.bottom ? res.top : res.bottom);
                return i === res.bottom ? res.top : res.bottom;
            }
        };
        const [partnerInfo] = useAwaiter(async () => {
            return await fetch(`https://adu.shiggy.fun/v1/${await partner()}.json`).then(r => r.json());
        }, { fallbackValue: "loading...", });
        console.log(partnerInfo);
        const classNames = findByProps("defaultColor");
        if (partnerInfo == null) {
            return <p className={classNames.defaultColor}> married to THE FUCKING SERVER IS DOWN</p>;
        }
        return <p className={classNames.defaultColor}> married to {partnerInfo.username}</p>;
    },
    name: "relationshipDB",
    authors: [Devs.echo, Devs.nea],
    description: "integration for the edating database",
    commands: [
        {
            name: "sendsomething",
            inputType: ApplicationCommandInputType.BUILT_IN,
            description: "",
            options: [{
                name: "url",
                type: ApplicationCommandOptionType.STRING,
                description: "", required: true
            },
            {
                name: "method",
                type: ApplicationCommandOptionType.STRING,
                required: false,
                description: ""
            }
            ],
            execute(args, ctx) {
                const url = args.find(it => it.name === "url")!!;

                const method = args.find(it => it.name === "method")?.value ?? "GET";
                (async () => {
                    if (method !== "GET" && method !== "POST") {
                        showToast("fuck you");
                        return;
                    }
                    const req = await fetchWedlock(method, url.value);
                    console.log(req);
                })();
            },
        },
        {
            name: "propose",
            inputType: ApplicationCommandInputType.BUILT_IN,
            description: "start the edating!",
            options: [{
                name: "proposee",
                type: ApplicationCommandOptionType.USER,
                required: true,
                description: "who's your cute kitten?"
            }], execute(args, ctx) {
                const proposee = args.find(it => it.name === "proposee")!!.value;
                (async () => {
                    const response = await fetchWedlock("POST", "v2/propose", {
                        to: proposee,
                        msg: "gay"
                    });
                    console.log(response);
                    sendMessage(ctx.channel.id, { content: `will you marry me <@${proposee}>? ${WL_HOSTNAME}/propose/embed?proposalid=` + response.id });
                })();
            }
        }
    ],
    patches: [
        {
            find: ".userTagNoNickname",
            replacement: {
                match: /variant:"text-sm\/normal",className:\i.pronouns,children:\i}\)}\)/,
                replace: "$&, $self.guh(arguments[0].user.id)"
            }
        }
    ],
});


