# Blog Project Frontend

React/Vite frontend for the Spring Boot API.

## Local development

```bash
npm install
npm run dev
```

Local requests use the Vite proxy for `/auth`, `/users`, `/posts`, and `/comments`, so the backend should run on `http://localhost:8080`.

## Vercel

Set the Vercel project root directory to `frontend`.

If the backend has HTTPS, add this environment variable in Vercel:

```text
VITE_API_BASE_URL=https://your-backend-domain
```

If the backend only has HTTP, leave `VITE_API_BASE_URL` empty or set it to `/api`.
The Vercel serverless function under `api/[...path].js` proxies `/api/*` to EC2.

Optionally set this Vercel server environment variable if the EC2 endpoint changes:

```text
BACKEND_URL=http://54.91.130.165
```
