import React from 'react';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Badge from '@mui/material/Badge';
import Box from '@mui/material/Box';

function StatusDot({ online }) {
  return (
    <span className={online ? 'status-dot online' : 'status-dot offline'} aria-hidden />
  );
}

export default function FriendList({ friends = [], selectedId, onSelect }) {
  return (
    <List>
      {friends.map((f) => (
        <ListItemButton key={f.id} selected={selectedId === f.id} onClick={() => onSelect(f)}>
          <ListItemAvatar>
            <Box sx={{ position: 'relative' }}>
              <Avatar sx={{ bgcolor: 'var(--accent)' }}>{(f.name || '').charAt(0).toUpperCase()}</Avatar>
              <span style={{ position: 'absolute', right: -2, bottom: -2 }}>
                <StatusDot online={!!f.online} />
              </span>
            </Box>
          </ListItemAvatar>
          <ListItemText
            primary={<span className="friend-name">{f.name}</span>}
            secondary={<span className="friend-status">{f.status || (f.online ? 'Online' : 'Offline')}</span>}
          />
        </ListItemButton>
      ))}
    </List>
  );
}
