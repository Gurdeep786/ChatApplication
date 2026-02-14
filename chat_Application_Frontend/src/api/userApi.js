import axiosClient from "./axiosClient";

export const getFriends = (userId) => {
  return axiosClient.get(`/user/friends/list/${userId}`);
};

export const searchUser = (username) => {
  return axiosClient.get(`/user/search?username=${username}`);
};

export const addFriend = (id) => {
  return axiosClient.post(`/user/friends/add/${id}`);
};
