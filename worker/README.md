# Halo chat proxy (Cloudflare Worker)

A tiny authenticated proxy that lets the Halo Android app talk to Claude without
ever shipping the Anthropic API key inside the app. The key lives only in this
Worker's secret store; the Worker injects it server-side and forwards the request
to the Anthropic Messages API.

```
Halo app  ──POST (X-App-Token)──▶  this Worker  ──x-api-key──▶  api.anthropic.com/v1/messages
```

The model is pinned to `claude-haiku-4-5` and `max_tokens` is capped server-side,
so a leaked app token can't be used to request a pricier model or huge outputs.
Request size and message count are bounded too. This does **not** limit request
*volume* — for that, add a [Cloudflare rate-limiting rule](https://developers.cloudflare.com/waf/rate-limiting-rules/)
on the Worker route (e.g. N requests/min per IP) before relying on it in production.

## Deploy

Prerequisites: a Cloudflare account and Node.js.

```bash
npm install -g wrangler          # one-time
cd worker
wrangler login                   # opens a browser to authorise

# Set the two secrets (stored by Cloudflare, never written to the repo):
wrangler secret put ANTHROPIC_API_KEY   # paste your Anthropic API key
wrangler secret put APP_TOKEN           # paste any long random string you choose

wrangler deploy
```

`wrangler deploy` prints the public URL, e.g.
`https://halo-chat-proxy.<your-subdomain>.workers.dev`.

## Wire it into the app

Build the app with the proxy URL and the same `APP_TOKEN` you set above, passed as
Gradle properties (kept out of the repo — put them in `~/.gradle/gradle.properties`
or pass on the command line):

```bash
gradle -p android app:assembleRelease \
  -PchatProxyUrl=https://halo-chat-proxy.<your-subdomain>.workers.dev/ \
  -PchatAppToken=<the-same-APP_TOKEN>
```

If these are omitted the app still builds, and the chat screen shows a
"not configured" notice instead of calling the network.

## Smoke test

```bash
# Rejected without the token:
curl -si -X POST https://halo-chat-proxy.<your-subdomain>.workers.dev/ \
  -H 'content-type: application/json' \
  -d '{"messages":[{"role":"user","content":"hi"}]}' | head -1     # -> HTTP/2 401

# Works with the token (model is forced to claude-haiku-4-5 server-side):
curl -s -X POST https://halo-chat-proxy.<your-subdomain>.workers.dev/ \
  -H 'content-type: application/json' \
  -H 'X-App-Token: <the-same-APP_TOKEN>' \
  -d '{"max_tokens":128,"messages":[{"role":"user","content":"Say hello in one short sentence."}]}'
```
