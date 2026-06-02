const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL;
const normalizedApiBaseUrl = configuredApiBaseUrl?.replace(/\/$/, "");
const useVercelProxy = import.meta.env.PROD && (!normalizedApiBaseUrl || normalizedApiBaseUrl === "/api");
const API_BASE_URL = configuredApiBaseUrl || "";

async function parseResponse(response) {
  const text = await response.text();
  const data = text ? safeJsonParse(text) : null;

  if (!response.ok) {
    const message = data?.message || data?.error || text || `${response.status} ${response.statusText}`;
    throw new Error(message);
  }

  return data;
}

function safeJsonParse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export async function request(path, options = {}) {
  const headers = {
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...options.headers
  };

  const url = useVercelProxy ? `/api/proxy?path=${encodeURIComponent(path)}` : `${API_BASE_URL}${path}`;

  const response = await fetch(url, {
    ...options,
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined
  });

  return parseResponse(response);
}

export function authHeaders(token) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}
