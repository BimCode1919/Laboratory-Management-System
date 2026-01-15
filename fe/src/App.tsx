// src/App.tsx

import "./App.css";
import { BrowserRouter } from "react-router-dom";
import AppRouting from "./routes";

export default function App() {
  return (
    <>
      <BrowserRouter>
        <AppRouting />
      </BrowserRouter>
    </>
  );
}
