import axiosClient from './axiosClient';

export const getChatHistory = (userA, userB) => {
  // backend expects: /chat/history?userA=...&userB=...
  return axiosClient.get(`/chat/history?userA=${encodeURIComponent(userA)}&userB=${encodeURIComponent(userB)}`);
};
