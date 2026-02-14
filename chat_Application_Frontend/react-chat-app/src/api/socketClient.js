import { CONFIG } from "../config/config";

export const createChatSocket = (token, onMessage) => {
  const socket = new WebSocket(
    `${CONFIG.WS_BASE_URL}/chat?token=${token}`
  );

  socket.onopen = () => console.log("WebSocket Connected");
  socket.onclose = () => console.log("WebSocket Closed");
  socket.onerror = (e) => console.error("WebSocket Error", e);

  socket.onmessage = (event) => {
    if (onMessage) onMessage(event.data);
  };

  return socket;
};
