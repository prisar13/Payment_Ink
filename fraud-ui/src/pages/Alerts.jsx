import { useEffect, useState } from "react";
import { api } from "../api/axiosConfig";

const Alerts = () => {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);

  const handleNextPage = () => {
    if (page < totalPages) {
      fetchAlerts(page + 1);
    }
  };

  const handlePrevPage = () => {
    if (page > 0) {
      fetchAlerts(page - 1);
    }
  };

  const fetchAlerts = async (pageNum) => {
    try {
      setLoading(true);
      setError(null);
      const response = await api.get(`/fraud/alerts?page=${pageNum}&size=10`); 
      const pageData = response.data.data; 
      setAlerts(pageData.content);
      setTotalPages(pageData.totalPages-1);
      setPage(pageNum); //Note: need to check if pageData has page number or not, if not then use pageNum
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAlerts(page);
  }, []);

  return (
  <div>
    {loading && <p>Loading...</p>}
    {error && <p>Error: {error}</p>}
    <table border="1" cellPadding="8">
        <thead>
          <tr>
            <th>Transaction ID</th>
            <th>Decision</th>
            <th>Risk Score</th>
            <th>Evaluated At</th>
          </tr>
        </thead>
        <tbody>
          {alerts.map((alert) => (
            <tr key={alert.transactionId}>
              <td>{alert.transactionId}</td>
              <td>{alert.decision}</td>
              <td>{alert.riskScore}</td>
              <td>{alert.evaluatedAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
    <div style={{ marginTop: "20px" }}>
        <button onClick={handlePrevPage} disabled={page === 0}>
          Prev
        </button>

        <span style={{ margin: "0 10px" }}>
          Page {page} of {totalPages}
        </span>

        <button onClick={handleNextPage} disabled={page >= totalPages}>
          Next
        </button>
      </div>
    </div>
  );
};

export default Alerts;
