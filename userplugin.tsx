import { ApplicationCommandInputType, ApplicationCommandOptionType } from "@api/Commands";
import { Devs } from "@utils/constants";
import { openModal } from "@utils/modal";
import definePlugin from "@utils/types";
import { findByPropsLazy } from "@webpack";

const OAuth = findByPropsLazy("OAuth2AuthorizeModal");
let token = null as null | string;

async function getAuthorizationToken(): Promise<string> {
    if (token != null) return Promise.resolve(token);
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
                    console.log("params", [...params.entries()]);
                    token = params.get("access_token");
                    resolve(token!!);
                }}
            ></OAuth.OAuth2AuthorizeModal>);
    });

}

export default definePlugin({
    name: "Wedlock Auth",
    description: "Shitty authentication test",
    authors: [Devs.nea],
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
                    const req = await fetch(url.value, { method: method, headers: { Authorize: await getAuthorizationToken() } });
                    const json = await req.json();
                    console.log(req);
                    console.log(json);
                })();
            },
        }
    ],
});
