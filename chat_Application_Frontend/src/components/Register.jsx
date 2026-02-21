import { useState } from "react";
import { registerUser } from "../api/authApi";
import { useNavigate } from "react-router-dom";

export default function Register() {
  const [form, setForm] = useState({ username: "", password: "" });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const navigate = useNavigate();

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    if (!form.username.trim() || !form.password) {
      setError("Please fill in all fields");
      return;
    }
    if (form.password.length < 5) {
      setError("Password must be at least 5 characters");
      return;
    }
    setLoading(true);
    try {
      await registerUser({
        username: form.username.trim(),
        password: form.password,
      });
      setSuccess("Registration successful! Please login.");
      setTimeout(() => navigate("/login"), 1500);
    } catch (err) {
      setError(err?.response?.data?.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-wrapper">
        <div className="login-card">
          <div className="login-header">
            <h2>Register</h2>
            <p className="muted">Create your account</p>
          </div>
          <form onSubmit={submit} className="login-form">
            <div className="field">
              <label>Username</label>
              <div className="input-wrap">
                <input
                  type="text"
                  placeholder="Enter username"
                  value={form.username}
                  onChange={(e) => setForm({ ...form, username: e.target.value })}
                  className="input"
                />
              </div>
            </div>
            <div className="field">
              <label>Password</label>
              <div className="input-wrap">
                <input
                  type="password"
                  placeholder="At least 5 characters"
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  className="input"
                />
              </div>
            </div>
            {error && <div className="error">{error}</div>}
            {success && <div className="success">{success}</div>}
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? "Registering..." : "Register"}
            </button>
          </form>
          <p className="signup">Already have an account? <span className="linkish" onClick={() => navigate("/login")}>Login</span></p>
        </div>
      </div>
    </div>
  );
}
