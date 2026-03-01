import React, { useContext } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";

const Dashboard = () => {
  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <div>
      <div style={{ display: "flex", gap: "20px", marginTop: "20px" }}>
        <button onClick={() => navigate("/transactions")}>
          View Transactions
        </button>

        <button onClick={() => navigate("/alerts")}>View Fraud Alerts</button>
        <button onClick={handleLogout}>Logout</button>
      </div>
    </div>
  );
};

export default Dashboard;
