import { Route, Routes } from "react-router";
import { LoginPage } from "./pages/login";
import { HomePage } from "./pages/Home";

export const App = () => {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
    </Routes>
  );
};
