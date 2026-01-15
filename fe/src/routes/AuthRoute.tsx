// src/routes/AuthRoute.tsx

import { Route, Routes } from "react-router-dom";
import AuthLayout from "../layouts/AuthLayout";

export default function AuthRoute() {
    return (
        <>
            <Routes>
                <Route element={<AuthLayout />}>
                    {/* <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/forgot-password" element={<ForgotPassword />} />
                    <Route path="/reset-password" element={<ResetPassword />} /> */}
                </Route>
            </Routes>
        </>
    )
}