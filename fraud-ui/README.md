# fraud-ui

React UI for interacting with the system (login, transactions, fraud alerts).

## Runs on

- `http://localhost:5173` (default Vite port)

## Setup

```bash
npm install
npm run dev
```

## Backend API base URL

The Axios client is configured in `src/api/axiosConfig.js` and currently points to:

- `http://localhost:8081` (`payment_service`)

## Notes & lessons learned

### authConfig

- Defines a global auth header using the token stored in `localStorage`.
- Response interceptor clears token + redirects to `/login` on 401.

### AuthContext

- Provides basic login, logout, and authentication via a token flow.

### ProtectedRoute: refresh redirect issue

**Issue**: On refresh, token exists in `localStorage`, but user is redirected to login.

**Root cause**: React rendering lifecycle timing—`useEffect()` runs after the initial render commit. If auth restoration happens only in `useEffect`, protected route checks can run before auth state is restored.

**Key takeaway**: Initialize auth state during render (e.g., `useState(() => localStorage.getItem("token"))`) or add a loading/rehydration guard to prevent redirects until auth is resolved.