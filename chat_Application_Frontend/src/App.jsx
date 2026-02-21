import { BrowserRouter, Routes, Route } from "react-router-dom";


import ChatPage from "./pages/ChatPage";
import ProtectedRoute from "./routes/protectedroute";
import Login from "./components/Login";
import Register from "./components/Register";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route
          path="/chat"
          element={
            <ProtectedRoute>
              <ChatPage />
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
