import { createContext, useState, useEffect } from "react";

export const AuthContext = createContext();

function parseJwt(token) {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;

    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map(c => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

export const AuthProvider = ({ children }) => {
  const [token, setToken] = useState(localStorage.getItem("token"));
const [username, setUsername] = useState(
    localStorage.getItem("username") || null
  );
 const [userId, setUserId] = useState(localStorage.getItem("userId") || null);

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("userId");

    setToken(null);
    setUsername(null);
    setUserId(null);

    // Redirect to login
    window.location.href = "/login";
  };

  useEffect(() => {
    if (!token) return;

    const payload = parseJwt(token);

    if (!payload) {
      logout();
      return;
    }

    // 🔥 EXPIRY CHECK
    if (payload.exp * 1000 < Date.now()) {
      logout();
      return;
    }

    // Restore user
    setUserId(payload.userId);
    setUsername(payload.sub);
  }, [token]);

const login = (jwt, user) => {
    // normalize jwt: some endpoints return { token: '...' }
    debugger
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


  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider
      value={{ token, username, userId, login, logout, isAuthenticated }}
    >
      {children}
    </AuthContext.Provider>
  );
};
