import { useState, useContext, useRef } from "react";
import { loginUser } from "../api/authApi";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { Lock, User, Loader2, LogIn } from "lucide-react";

export default function Login() {
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const usernameRef = useRef(null);

  const submit = async (e) => {
    e?.preventDefault();
    if (!form.username.trim() || !form.password) {
      setError("Please fill in all fields");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const token = await loginUser({
        username: form.username.trim(),
        password: form.password,
      });
      login(token, form.username.trim());
      navigate("/chat");
    } catch (err) {
      setError(err?.response?.data?.message || "Invalid credentials");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="glow glow-left" aria-hidden />
      <div className="glow glow-right" aria-hidden />

      <div className="login-wrapper">
        <div className="login-card">
          <div className="login-header">
            <div className="logo-blob">
              <LogIn className="logo-icon" />
            </div>
            <h2>Welcome Back</h2>
            <p className="muted">Sign in to start chatting</p>
          </div>

          <form onSubmit={submit} className="login-form">
            <div className="field">
              <label>Username</label>
              <div className="input-wrap">
                <User className="field-icon" />
                <input
                  ref={usernameRef}
                  type="text"
                  placeholder="Enter username"
                  value={form.username}
                  onChange={(e) => setForm({ ...form, username: e.target.value })}
                  autoFocus
                  className="input"
                />
              </div>
            </div>

            <div className="field">
              <label>Password</label>
              <div className="input-wrap">
                <Lock className="field-icon" />
                <input
                  type="password"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  className="input"
                />
              </div>
            </div>

            {error && <div className="error">{error}</div>}

            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? (
                <>
                  <Loader2 className="spinner" />
                  <span>Connecting...</span>
                </>
              ) : (
                "Sign In"
              )}
            </button>
          </form>

          <p className="signup">New here? <span className="linkish">Create an account</span></p>
        </div>
      </div>
    </div>
  );
}