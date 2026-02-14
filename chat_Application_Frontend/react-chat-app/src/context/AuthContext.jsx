import { createContext, useState } from "react";

export const AuthContext = createContext();

// small jwt decode (no validation) to extract payload
function parseJwt(token) {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const payload = parts[1];
    // base64url -> base64
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map(function (c) {
          return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
        })
        .join("")
    );
    return JSON.parse(json);
  } catch (e) {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem("token"));
  const [username, setUsername] = useState(
    localStorage.getItem("username") || null
  );
  const [userId, setUserId] = useState(localStorage.getItem("userId") || null);

  // initialize from token if present
  if (token && !userId) {
    const payload = parseJwt(token);
    if (payload) {
      const id = payload.sub || payload.id || payload.userId;
      const name = payload.username || payload.name || null;
      if (id) {
        localStorage.setItem("userId", id);
        setUserId(id);
      }
      if (name) {
        localStorage.setItem("username", name);
        setUsername(name);
      }
    }
  }

  // login accepts jwt and optional username
  const login = (jwt, user) => {
    // normalize jwt: some endpoints return { token: '...' }
    const tokenStr =
      jwt && typeof jwt === "object" && jwt.token ? jwt.token : jwt;
    localStorage.setItem("token", tokenStr);
    setToken(tokenStr);
    // always try to extract id from the token payload
    const payload = parseJwt(tokenStr);
    const id = payload?.userId || null;
    if (id) {
      localStorage.setItem("userId", id);
      setUserId(id);
    }

    // prefer explicit username param, otherwise read from token payload
    if (user) {
      localStorage.setItem("username", user);
      setUsername(user);
    } else {
      const name = payload?.username || payload?.name || null;
      if (name) {
        localStorage.setItem("username", name);
        setUsername(name);
      }
    }
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("userId");
    setToken(null);
    setUsername(null);
    setUserId(null);
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider
      value={{ token, username, userId, login, logout, isAuthenticated }}
    >
      {children}
    </AuthContext.Provider>
  );
};
