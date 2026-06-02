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

Add this environment variable in Vercel:

```text
VITE_API_BASE_URL=https://your-backend-domain
```
