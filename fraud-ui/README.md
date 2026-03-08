# Hello

- Just me writing some rough notes for the sake of undertsanding the concepts. Follow along if you will.
- Thanks for passing by

# authConfig

- Defined a global level authentication header for the request using the token stored in localstorage.
- response : catch error if any or else return the response.

# AuthContext

- Provides basic login, logout and authentication via token flow.

# ProtectedRoute

### Issue: On Refresh, Token Exists in localStorage, But User Gets Redirected to Login

#### Root Cause: React Rendering Lifecycle Timing Problem

React has three main phases during rendering:

1. **Render Phase**
2. **Commit Phase**
3. **Effects Phase** (`useEffect` runs here)

> ##### Important Detail
> `useEffect()` does **NOT** run during the initial render.
> It runs **after** the component has already rendered and committed to the DOM.
> 
> This means:
> - On page refresh, the component renders first.
> - If authentication state depends on `useEffect()` (e.g., reading token from `localStorage` and setting user state),
> - The initial render may execute redirect logic **before** the auth state is restored.
> - As a result, the user is redirected to the login page even though the token exists.
> Authentication state must be resolved before protected route logic runs, or a loading guard should be implemented to prevent premature redirects.
> React state is memory-based. localStorage is browser-based. This is called a: Race condition between render and authentication restoration.
Because useState initializer runs during render phase. so setting its value to the token directly would lead it to be populated with the token during render itself so when it comes to the token value check it will not be null.