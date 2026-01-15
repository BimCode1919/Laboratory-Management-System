import React from "react";
import { Link, useLocation } from "react-router-dom";
import { FaTachometerAlt, FaUsers, FaUserCircle, FaUsersCog } from "react-icons/fa";

const AdminSidebar: React.FC = () => {
  const { pathname } = useLocation();

  const navItems = [
    { label: "Dashboard", icon: <FaTachometerAlt />, to: "/admin" },
    { label: "User", icon: <FaUsers />, to: "/admin/users" },
    { label: "Role", icon: <FaUsersCog />, to: "/admin/roles" },
  ];

  return (
    <aside className="fixed top-0 left-0 h-screen w-64 bg-white shadow-md flex flex-col">
      <div className="flex flex-col items-center py-6 border-b border-gray-200">
        <div className="flex items-center space-x-2">
          <FaUserCircle className="text-blue-600 text-2xl" />
          <span className="font-semibold text-gray-700">Admin</span>
        </div>
      </div>

      {/* Menu */}
      <nav className="flex-1 mt-4 px-4 space-y-1">
        {navItems.map((item) => (
          <Link
            key={item.to}
            to={item.to}
            className={`flex items-center space-x-3 px-4 py-2 rounded-md transition ${
              pathname === item.to
                ? "bg-blue-600 text-white"
                : "text-gray-600 hover:bg-blue-100"
            }`}
          >
            {item.icon}
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
    </aside>
  );
};

export default AdminSidebar;
