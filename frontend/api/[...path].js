const BACKEND_URL = process.env.BACKEND_URL || "http://54.91.130.165";

const HOP_BY_HOP_HEADERS = new Set([
  "host",
  "connection",
  "content-length",
  "accept-encoding",
  "x-forwarded-host",
  "x-forwarded-proto"
]);

export default async function handler(req, res) {
  if (req.method === "OPTIONS") {
    res.status(204).end();
    return;
  }

  const path = Array.isArray(req.query.path) ? req.query.path.join("/") : req.query.path || "";
  const targetUrl = new URL(`/${path}`, BACKEND_URL);

  for (const [key, value] of Object.entries(req.query)) {
    if (key !== "path") {
      targetUrl.searchParams.append(key, value);
    }
  }

  const headers = {};
  for (const [key, value] of Object.entries(req.headers)) {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase()) && value !== undefined) {
      headers[key] = Array.isArray(value) ? value.join(",") : value;
    }
  }

  const response = await fetch(targetUrl, {
    method: req.method,
    headers,
    body: ["GET", "HEAD"].includes(req.method) ? undefined : await readBody(req)
  });

  res.status(response.status);
  response.headers.forEach((value, key) => {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase())) {
      res.setHeader(key, value);
    }
  });

  const buffer = Buffer.from(await response.arrayBuffer());
  res.send(buffer);
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on("data", (chunk) => chunks.push(chunk));
    req.on("end", () => resolve(Buffer.concat(chunks)));
    req.on("error", reject);
  });
}
