import axios from "axios";

const API = "http://localhost:8081/auth";

export const loginUser = async (data) => {
  const res = await axios.post(
    `${API}/login`,
    data,
    {
      headers: {
        "Content-Type": "application/json"
      }
    }
  );

  return res.data; // JWT token
};
