// src/context/AuthContext.tsx

import type { UserProfileDTO } from "@/features/User/types/user.types";
import { createContext, useContext, useState } from "react";

interface AuthContextType {
  role: string | null;
  setRole: (role: string | null) => void;
  user: UserProfileDTO | null;
  setUser: (user: UserProfileDTO | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [role, setRole] = useState<string | null>(null);
  const [user, setUser] = useState<UserProfileDTO | null>(null);

  return (
    <AuthContext.Provider value={{ role, setRole, user, setUser }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
