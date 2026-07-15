/**
 * Halo chat proxy — Cloudflare Worker.
 *
 * A thin, authenticated passthrough between the Halo Android app and the
 * Anthropic Messages API. The app never holds the Anthropic API key: the key
 * lives only in this Worker's secret store and is injected server-side here.
 *
 * Secrets (set via `wrangler secret put`, never committed):
 *   ANTHROPIC_API_KEY  — your Anthropic API key.
 *   APP_TOKEN          — a shared token the app sends in `X-App-Token`. Requests
 *                        without a matching token are rejected, so a leaked
 *                        Worker URL alone cannot spend your credits.
 *
 * The model is pinned server-side to keep cost predictable even if the app
 * token leaks — a caller cannot request a pricier model.
 */

const ANTHROPIC_URL = "https://api.anthropic.com/v1/messages";
const ALLOWED_MODEL = "claude-haiku-4-5";
const MAX_TOKENS_CAP = 1024;
const MAX_BODY_BYTES = 200_000;   // ~a long conversation; bounds input cost/abuse.
const MAX_MESSAGES = 60;

export default {
  async fetch(request, env) {
    if (request.method !== "POST") {
      return json({ error: "Method not allowed" }, 405);
    }

    // Shared-token guard. A plain equality check is fine here because the token
    // is high-entropy and network jitter dwarfs any per-byte timing signal.
    const token = request.headers.get("X-App-Token") || "";
    if (!env.APP_TOKEN || token !== env.APP_TOKEN) {
      return json({ error: "Unauthorized" }, 401);
    }

    if (!env.ANTHROPIC_API_KEY) {
      return json({ error: "Proxy is not configured" }, 500);
    }

    // Bound the request size so a token holder can't send huge inputs (input
    // tokens are billed even though output is capped). Note: this is not a rate
    // limiter — add a Cloudflare rate-limiting rule for volume control.
    const declaredLen = Number(request.headers.get("content-length") || 0);
    if (declaredLen > MAX_BODY_BYTES) {
      return json({ error: "Request too large" }, 413);
    }

    const raw = await request.text();
    if (raw.length > MAX_BODY_BYTES) {
      return json({ error: "Request too large" }, 413);
    }

    let body;
    try {
      body = JSON.parse(raw);
    } catch (_) {
      return json({ error: "Invalid JSON body" }, 400);
    }
    if (body === null || typeof body !== "object" || Array.isArray(body)) {
      return json({ error: "Invalid request body" }, 400);
    }
    if (Array.isArray(body.messages) && body.messages.length > MAX_MESSAGES) {
      return json({ error: "Too many messages" }, 400);
    }

    // Pin the model and clamp output length regardless of what the app sent.
    body.model = ALLOWED_MODEL;
    if (
      typeof body.max_tokens !== "number" ||
      !Number.isInteger(body.max_tokens) ||
      body.max_tokens < 1 ||
      body.max_tokens > MAX_TOKENS_CAP
    ) {
      body.max_tokens = MAX_TOKENS_CAP;
    }

    let upstream;
    try {
      upstream = await fetch(ANTHROPIC_URL, {
        method: "POST",
        headers: {
          "content-type": "application/json",
          "x-api-key": env.ANTHROPIC_API_KEY,
          "anthropic-version": "2023-06-01",
        },
        body: JSON.stringify(body),
      });
    } catch (_) {
      return json({ error: "Upstream request failed" }, 502);
    }

    // Forward Anthropic's response (status + body) back to the app verbatim,
    // preserving the upstream content-type so error pages aren't mislabeled.
    const text = await upstream.text();
    return new Response(text, {
      status: upstream.status,
      headers: {
        "content-type": upstream.headers.get("content-type") || "application/json",
      },
    });
  },
};

function json(obj, status) {
  return new Response(JSON.stringify(obj), {
    status,
    headers: { "content-type": "application/json" },
  });
}
