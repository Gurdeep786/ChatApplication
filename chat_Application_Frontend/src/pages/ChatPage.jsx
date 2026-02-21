import { useEffect, useState, useRef, useContext } from "react";
import { createChatSocket } from "../api/socketClient";
import { getFriends, addFriend } from "../api/userApi";
import { getChatHistory } from "../api/chatApi";

import Box from "@mui/material/Box";
import Drawer from "@mui/material/Drawer";
import FriendList from "../components/FriendList";
import ListItemText from "@mui/material/ListItemText";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import IconButton from "@mui/material/IconButton";
import Button from "@mui/material/Button";
import TextField from "@mui/material/TextField";
import Dialog from "@mui/material/Dialog";
import DialogTitle from "@mui/material/DialogTitle";
import DialogContent from "@mui/material/DialogContent";
import DialogActions from "@mui/material/DialogActions";
import Snackbar from "@mui/material/Snackbar";
import Alert from "@mui/material/Alert";
import AddIcon from "@mui/icons-material/Add";
import { AuthContext } from "../context/AuthContext";
import { Sun, Moon, LogOut } from "lucide-react";

export default function ChatPage() {
  const [messages, setMessages] = useState([]);
  const [friends, setFriends] = useState([]);
  const [drawerOpen, setDrawerOpen] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  // Remove searchResult state
  const [snack, setSnack] = useState({
    open: false,
    severity: "info",
    msg: "",
  });
  const token = localStorage.getItem("token");
  const socketRef = useRef(null);
  const { userId, username, logout } = useContext(AuthContext);

  // Theme toggle (no changes to chat logic)
  const [theme, setTheme] = useState(() => {
    try {
      return localStorage.getItem("theme") || null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (theme) {
      document.documentElement.setAttribute("data-theme", theme);
      localStorage.setItem("theme", theme);
    } else {
      document.documentElement.removeAttribute("data-theme");
      localStorage.removeItem("theme");
    }
  }, [theme]);

  const toggleTheme = () => {
    setTheme((t) => (t === "light" ? "dark" : "light"));
  };
  useEffect(() => {
  const interval = setInterval(() => {
    if (socketRef.current?.readyState === WebSocket.OPEN) {
      socketRef.current.send(
        JSON.stringify({ type: "HEARTBEAT" })
      );
    }
  }, 10000); // every 10 sec

  return () => clearInterval(interval);
}, []);
  useEffect(() => {
    fetchFriends();

    const socket = createChatSocket(token, (msg) => {
      try {
        const parsed = typeof msg === "string" ? JSON.parse(msg) : msg;

        // 🔥 PRESENCE MESSAGE
        if (parsed.type === "PRESENCE") {
          setFriends((prev) =>
            prev.map((f) =>
              f.name === parsed.username ? { ...f, online: parsed.online } : f,
            ),
          );
          return; // do NOT add to messages
        }
        if (!friends.find((f) => f.name === parsed.sender)) {
            fetchFriends();
        }
        // 🔥 NORMAL CHAT MESSAGE
        setMessages((prev) => [...prev, parsed]);
      } catch (err) {
        console.error("Invalid WS message:", msg);
      }
    });

    socketRef.current = socket;

    return () => socket && socket.close();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchFriends = async () => {
    try {
      const res = await getFriends(userId);

      setFriends(res.data || []);
    } catch (e) {
      setSnack({
        open: true,
        severity: "error",
        msg: "Failed to load friends",
      });
    }
  };

  const [selectedFriend, setSelectedFriend] = useState(null);
  const [outgoing, setOutgoing] = useState("");
  const [selectedKey, setSelectedKey] = useState(null); // username or about or id used to match messages

  const handleSelectFriend = async (friend) => {
    setSelectedFriend(friend);

    // compute a stable key to identify this friend in messages (prefer username)
    const key = friend.name;
    setSelectedKey(key);
    // load chat history for this friend (pass username and peer key)
    try {
      const res = await getChatHistory(username, key);
      // API should return an array of messages
      setMessages(res.data || []);
    } catch (e) {
      setSnack({
        open: true,
        severity: "error",
        msg: "Failed to load chat history",
      });
    }
  };

  const sendMessage = () => {
    debugger;
    if (!outgoing.trim()) return;
    const payload = {
      receiver: selectedKey ?? selectedFriend?.name,
      content: outgoing.trim(),
    };

    // try sending over socket, fallback to local append
    try {
      if (
        socketRef.current &&
        socketRef.current.readyState === WebSocket.OPEN
      ) {
        socketRef.current.send(JSON.stringify(payload));
      }
    } catch (e) {
      // ignore
    }

    setMessages((prev) => [
      ...prev,
      {
        fromMe: true,
        content: payload.content,
        receiver: payload.receiver,
        ts: Date.now(),
      },
    ]);
    setOutgoing("");
  };

  // Removed handleSearch logic

  const handleAdd = async (friendUsername) => {
    if (!friendUsername.trim()) {
      setSnack({
        open: true,
        severity: "error",
        msg: "Please enter a username",
      });
      return;
    }
    try {
      await addFriend(username, friendUsername.trim());
      setSnack({ open: true, severity: "success", msg: "Friend added" });
      setAddOpen(false);
      fetchFriends();
    } catch (e) {
      setSnack({
        open: true,
        severity: "error",
        msg: e?.response?.data?.message || "Failed to add friend",
      });
    }
  };
  console.log(messages);
  return (
    <Box sx={{ display: "flex", height: "100vh" }}>
      <AppBar position="fixed">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Chat Dashboard
          </Typography>
          <IconButton
            color="inherit"
            onClick={() => setAddOpen(true)}
            aria-label="add friend"
          >
            <AddIcon />
          </IconButton>

          {/* Theme toggle */}
          <IconButton
            color="inherit"
            onClick={toggleTheme}
            aria-label="toggle theme"
            className="theme-toggle-btn"
            title="Toggle theme"
          >
            {theme === "light" ? <Sun /> : <Moon />}
          </IconButton>

          {/* Sign out */}
          <Button
            color="inherit"
            onClick={logout}
            className="signout-btn"
            startIcon={<LogOut />}
          >
            Sign out
          </Button>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        open={drawerOpen}
        sx={{ width: 300, flexShrink: 0, mt: 8 }}
      >
        <Toolbar />
        <Box sx={{ width: 300, mt: 2 }}>
          <FriendList
            friends={friends}
            selectedId={selectedFriend?.id}
            onSelect={handleSelectFriend}
          />
        </Box>
      </Drawer>

      <Box
        component="main"
        className="chat-main"
        sx={{
          flexGrow: 1,
          p: 3,
          mt: 8,
          display: "flex",
          flexDirection: "column",
          minHeight: 0,
        }}
      >
        <Typography variant="h5" gutterBottom>
          Messages
        </Typography>
        <Box
          className="messages-container"
          sx={{
            flex: 1,
            minHeight: 0,
            overflowY: "auto",
            p: 2,
            borderRadius: 1,
            mb: 2,
          }}
        >
          {!selectedFriend ? (
            <Typography color="text.secondary">
              Select a friend to start chatting
            </Typography>
          ) : (
            (() => {
              const conv = messages.filter((m) => {
                const to = m.to ?? m.receiver ?? null;
                const from = m.from ?? m.sender ?? null;
                const isToFriend =
                  to === selectedFriend?.id ||
                  to === selectedKey ||
                  to === selectedFriend?.name ||
                  to === selectedFriend?.about;
                const isFromFriend =
                  from === selectedFriend?.id ||
                  from === selectedKey ||
                  from === selectedFriend?.name ||
                  from === selectedFriend?.about;
                const isToMe = to == username;
                const isFromMe = from == username || m.fromMe === true;
                const between =
                  (isFromFriend && isToMe) || (isFromMe && isToFriend);
                const unknown = !to && !from;
                return between || unknown;
              });
              if (conv.length === 0)
                return (
                  <Typography color="text.secondary">
                    No messages yet with {selectedFriend.username}
                  </Typography>
                );
              return conv.map((m, i) => {
                const text =
                  m.text ?? m.content ?? (typeof m === "string" ? m : "");
                const fromField = m.from ?? m.sender ?? null;
                const isMe =
                  m.fromMe === true ||
                  fromField === username ||
                  fromField === String(userId);
                const senderName = isMe
                  ? username || "You"
                  : (m.sender ?? m.from ?? "");
                return (
                  <Box
                    key={i}
                    className={
                      isMe ? "msg-row msg-row-me" : "msg-row msg-row-them"
                    }
                  >
                    {!isMe && (
                      <div className="msg-meta">
                        <div className="msg-time">
                          {m.timestamp
                            ? new Date(m.timestamp).toLocaleTimeString()
                            : ""}
                        </div>
                      </div>
                    )}
                    <Box
                      className={
                        isMe ? "msg-bubble msg-me" : "msg-bubble msg-them"
                      }
                    >
                      <div className="msg-header">
                        <span className="msg-sender">{senderName}</span>
                      </div>
                      <div className="msg-body">{text}</div>
                    </Box>
                    {isMe && (
                      <div className="msg-meta msg-meta-right">
                        <div className="msg-time">
                          {m.timestamp
                            ? new Date(m.timestamp).toLocaleTimeString()
                            : ""}
                        </div>
                      </div>
                    )}
                  </Box>
                );
              });
            })()
          )}
        </Box>
        <Box sx={{ display: "flex", gap: 1 }}>
          <TextField
            fullWidth
            placeholder={
              selectedFriend
                ? `Message ${selectedFriend.name}`
                : "Select a friend"
            }
            value={outgoing}
            onChange={(e) => setOutgoing(e.target.value)}
            disabled={!selectedFriend}
            onKeyDown={(e) => e.key === "Enter" && sendMessage()}
          />
          <Button
            variant="contained"
            onClick={sendMessage}
            disabled={!selectedFriend || !outgoing.trim()}
          >
            Send
          </Button>
        </Box>
      </Box>

      <Dialog open={addOpen} onClose={() => setAddOpen(false)}>
        <DialogTitle>Add Friend</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Username"
            fullWidth
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleAdd(searchTerm)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>Cancel</Button>
          <Button onClick={() => handleAdd(searchTerm)}>Add</Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snack.open}
        autoHideDuration={4000}
        onClose={() => setSnack((s) => ({ ...s, open: false }))}
      >
        <Alert severity={snack.severity} sx={{ width: "100%" }}>
          {snack.msg}
        </Alert>
      </Snackbar>
    </Box>
  );
}
