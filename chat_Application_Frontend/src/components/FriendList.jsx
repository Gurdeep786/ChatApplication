import React from 'react';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import ListItemAvatar from '@mui/material/ListItemAvatar';
import Avatar from '@mui/material/Avatar';
import Badge from '@mui/material/Badge';

export default function FriendList({ friends = [], selectedId, onSelect }) {
  return (
    <List>
      {friends.map((f) => (
        <ListItemButton key={f.id} selected={selectedId === f.id} onClick={() => onSelect(f)}>
          <ListItemAvatar>
            <Badge color={f.name ? 'success' : 'default'} variant="dot" overlap="circular">
              <Avatar>{(f.name || '').charAt(0).toUpperCase()}</Avatar>
            </Badge>
          </ListItemAvatar>
          <ListItemText primary={f.name} secondary={f.status || (f.online ? 'Online' : 'Offline')} />
        </ListItemButton>
      ))}
    </List>
  );
}
