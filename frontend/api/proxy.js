const BACKEND_URL = process.env.BACKEND_URL || "http://54.91.130.165";

export const config = {
  api: {
    bodyParser: false
  }
};

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

  const path = typeof req.query.path === "string" ? req.query.path : "";
  if (!path) {
    res.status(200).json({
      service: "Vercel API proxy",
      status: "ok",
      backendUrl: BACKEND_URL
    });
    return;
  }

  if (!path.startsWith("/")) {
    res.status(400).json({ message: "path query must start with /" });
    return;
  }

  const targetUrl = new URL(path, BACKEND_URL);

  for (const [key, value] of Object.entries(req.query)) {
    if (key !== "path") {
      targetUrl.searchParams.append(key, Array.isArray(value) ? value.join(",") : value);
    }
  }

  const headers = {};
  for (const [key, value] of Object.entries(req.headers)) {
    if (!HOP_BY_HOP_HEADERS.has(key.toLowerCase()) && value !== undefined) {
      headers[key] = Array.isArray(value) ? value.join(",") : value;
    }
  }

  try {
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

    res.send(Buffer.from(await response.arrayBuffer()));
  } catch (error) {
    res.status(502).json({ message: `Backend proxy failed: ${error.message}` });
  }
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    req.on("data", (chunk) => chunks.push(chunk));
    req.on("end", () => resolve(Buffer.concat(chunks)));
    req.on("error", reject);
  });
}
