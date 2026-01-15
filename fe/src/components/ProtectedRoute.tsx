// src/components/ProtectedRoute.tsx
import { Navigate, Outlet } from "react-router-dom";
import { jwtDecode } from "jwt-decode";

interface DecodedToken {
  sub: string;
  ["cognito:groups"]?: string[];
  exp?: number;
}

type ProtectedRouteProps = {
  allowedRoles?: string[];
};

export default function ProtectedRoute({
  allowedRoles = [],
}: ProtectedRouteProps) {
  const token = localStorage.getItem("accessToken");

  if (!token) {
    return <Navigate to="/" replace />;
  }

  try {
    const decoded = jwtDecode<DecodedToken>(token);
    const userRoles = decoded["cognito:groups"] || [];

    const hasAccess = userRoles.some((r) =>
      allowedRoles.includes(r.toUpperCase())
    );

    if (!hasAccess) {
      return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />;
  } catch {
    return <Navigate to="/" replace />;
  }
}
