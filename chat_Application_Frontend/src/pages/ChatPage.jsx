import { useEffect, useState, useRef, useContext } from "react";
import { createChatSocket } from "../api/socketClient";
import { getFriends, searchUser, addFriend } from "../api/userApi";
import { getChatHistory } from "../api/chatApi";

import Box from '@mui/material/Box';
import Drawer from '@mui/material/Drawer';
import FriendList from '../components/FriendList';
import ListItemText from '@mui/material/ListItemText';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogTitle from '@mui/material/DialogTitle';
import DialogContent from '@mui/material/DialogContent';
import DialogActions from '@mui/material/DialogActions';
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';
import AddIcon from '@mui/icons-material/Add';
import { AuthContext } from "../context/AuthContext";

export default function ChatPage() {
  const [messages, setMessages] = useState([]);
  const [friends, setFriends] = useState([]);
  const [drawerOpen, setDrawerOpen] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResult, setSearchResult] = useState(null);
  const [snack, setSnack] = useState({ open: false, severity: 'info', msg: '' });
  const token = localStorage.getItem("token");
  const socketRef = useRef(null);
  const { userId, username } = useContext(AuthContext);
  useEffect(() => {
    fetchFriends();

    const socket = createChatSocket(token, (msg) => {
      setMessages(prev => [...prev, msg]);
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
      setSnack({ open: true, severity: 'error', msg: 'Failed to load friends' });
    }
  };

  const [selectedFriend, setSelectedFriend] = useState(null);
  const [outgoing, setOutgoing] = useState('');
  const [selectedKey, setSelectedKey] = useState(null); // username or about or id used to match messages

  const handleSelectFriend = async (friend) => {
    setSelectedFriend(friend);
    // compute a stable key to identify this friend in messages (prefer username)
    const key = friend.username ?? friend.about ?? String(friend.userId ?? friend.id ?? '');
    setSelectedKey(key);
    // load chat history for this friend (pass username and peer key)
    try {
      const res = await getChatHistory(username, key);
      // API should return an array of messages
      setMessages(res.data || []);
    } catch (e) {
      setSnack({ open: true, severity: 'error', msg: 'Failed to load chat history' });
    }
  };

  const sendMessage = () => {
    if (!outgoing.trim()) return;
    const payload = {
      receiver: selectedKey ?? selectedFriend?.about ?? selectedFriend?.username ?? selectedFriend?.id,
      content: outgoing.trim()
    };

    // try sending over socket, fallback to local append
    try {
      if (socketRef.current && socketRef.current.readyState === WebSocket.OPEN) {
        socketRef.current.send(JSON.stringify(payload));
      }
    } catch (e) {
      // ignore
    }

    setMessages(prev => [...prev, { fromMe: true, content: payload.content, receiver: payload.receiver, ts: Date.now() }]);
    setOutgoing('');
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    try {
      const res = await searchUser(searchTerm.trim());
      setSearchResult(res.data || null);
    } catch (e) {
      setSnack({ open: true, severity: 'error', msg: 'Search failed' });
    }
  };

  const handleAdd = async (id) => {
    try {
      await addFriend(id);
      setSnack({ open: true, severity: 'success', msg: 'Friend added' });
      setAddOpen(false);
      fetchFriends();
    } catch (e) {
      setSnack({ open: true, severity: 'error', msg: 'Failed to add friend' });
    }
  };

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      <AppBar position="fixed">
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>Chat Dashboard</Typography>
          <IconButton color="inherit" onClick={() => setAddOpen(true)} aria-label="add friend">
            <AddIcon />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Drawer variant="permanent" open={drawerOpen} sx={{ width: 300, flexShrink: 0, mt: 8 }}>
        <Toolbar />
        <Box sx={{ width: 300, mt: 2 }}>
          <FriendList friends={friends} selectedId={selectedFriend?.id} onSelect={handleSelectFriend} />
        </Box>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, p: 3, mt: 8 }}>
        <Typography variant="h5" gutterBottom>Messages</Typography>

        <Box sx={{ mb: 2, height: '70vh', overflow: 'auto', border: '1px solid #eee', p: 2, borderRadius: 1 }}>
          {!selectedFriend ? (
            <Typography color="text.secondary">Select a friend to start chatting</Typography>
          ) : (() => {
            const conv = messages.filter(m => {
              const to = m.to ?? m.receiver ?? null;
              const from = m.from ?? m.sender ?? null;

              // helpers to test equality against friend and current user
              const isToFriend = to === selectedFriend?.id || to === selectedKey || to === selectedFriend?.username || to === selectedFriend?.about;
              const isFromFriend = from === selectedFriend?.id || from === selectedKey || from === selectedFriend?.username || from === selectedFriend?.about;
              const isToMe = to === username || to === String(userId);
              const isFromMe = from === username || from === String(userId) || m.fromMe === true;

              // message should be between (me <-> friend)
              const between = (isFromFriend && isToMe) || (isFromMe && isToFriend);

              // also include messages that are missing explicit from/to (fallback)
              const unknown = !to && !from;

              return between || unknown;
            });

            if (conv.length === 0) return <Typography color="text.secondary">No messages yet with {selectedFriend.username}</Typography>;

            return conv.map((m, i) => {
              const text = m.text ?? m.content ?? (typeof m === 'string' ? m : '');
              const fromField = m.from ?? m.sender ?? null;
              const isMe = m.fromMe === true || fromField === username || fromField === String(userId);
              const senderName = isMe ? (username || 'You') : (m.sender ?? m.from ?? '');

              return (
                <Box key={i} sx={{ mb: 1, display: 'flex', justifyContent: isMe ? 'flex-end' : 'flex-start' }}>
                  <Box sx={{
                    maxWidth: '72%',
                    bgcolor: isMe ? 'primary.main' : 'grey.100',
                    color: isMe ? 'common.white' : 'text.primary',
                    px: 2,
                    py: 1,
                    borderTopLeftRadius: isMe ? 12 : 4,
                    borderTopRightRadius: isMe ? 4 : 12,
                    borderBottomLeftRadius: 12,
                    borderBottomRightRadius: 12,
                  }}>
                    <Typography variant="caption" sx={{ display: 'block', mb: 0.5, textAlign: isMe ? 'right' : 'left', fontWeight: 600 }}>{senderName}</Typography>
                    <Typography variant="body2">{text}</Typography>
                    {m.timestamp && (
                      <Typography variant="caption" sx={{ display: 'block', mt: 0.5, textAlign: isMe ? 'right' : 'left', color: 'text.secondary' }}>{new Date(m.timestamp).toLocaleString()}</Typography>
                    )}
                  </Box>
                </Box>
              );
            });
          })()}
        </Box>

        <Box sx={{ display: 'flex', gap: 1 }}>
          <TextField fullWidth placeholder={selectedFriend ? `Message ${selectedFriend.username}` : 'Select a friend'} value={outgoing} onChange={(e) => setOutgoing(e.target.value)} disabled={!selectedFriend} onKeyDown={(e) => e.key === 'Enter' && sendMessage()} />
          <Button variant="contained" onClick={sendMessage} disabled={!selectedFriend || !outgoing.trim()}>Send</Button>
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
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
          />

          {searchResult && (
            <Box sx={{ mt: 2 }}>
              <Typography>{searchResult.username}</Typography>
              <Button variant="contained" onClick={() => handleAdd(searchResult.id)} sx={{ mt: 1 }}>Add</Button>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddOpen(false)}>Cancel</Button>
          <Button onClick={handleSearch}>Search</Button>
        </DialogActions>
      </Dialog>

      <Snackbar open={snack.open} autoHideDuration={4000} onClose={() => setSnack(s => ({ ...s, open: false }))}>
        <Alert severity={snack.severity} sx={{ width: '100%' }}>{snack.msg}</Alert>
      </Snackbar>
    </Box>
  );
}
