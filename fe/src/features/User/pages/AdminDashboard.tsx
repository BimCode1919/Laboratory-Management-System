import React from "react";

const AdminDashboard: React.FC = () => {
  return (
    <div>
      <h1 className="text-3xl font-bold mb-6 text-gray-800">Tổng quan</h1>
      <div className="grid grid-cols-3 gap-6">
        {[
          { title: "Người dùng", value: "124" },
          { title: "Đơn hiến máu", value: "52" },
          { title: "Cơ sở y tế", value: "10" },
        ].map((stat) => (
          <div
            key={stat.title}
            className="bg-white shadow-sm rounded-lg p-6 border border-gray-200"
          >
            <h2 className="text-gray-500 text-sm uppercase mb-2">
              {stat.title}
            </h2>
            <p className="text-2xl font-bold text-blue-600">{stat.value}</p>
          </div>
        ))}
      </div>
    </div>
  );
};

export default AdminDashboard;
