import React, { useEffect, useState } from "react";
import { api } from "../api/axiosConfig";

const Transactions = () => {
  const [transactions, setTransactions] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchTransactions = async (pageNumber = 0) => {
    try {
      setLoading(true);
      const response = await api.get(`/transaction?page=${pageNumber}&size=10`);

      setTransactions(response.data.content);
      setTotalPages(response.data.totalPages);
      setPage(response.data.number);
    } catch (error) {
      console.error("Error fetching transactions", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTransactions(0);
  }, []);

  const handleNext = () => {
    if (page < totalPages - 1) {
      fetchTransactions(page + 1);
    }
  };

  const handlePrev = () => {
    if (page > 0) {
      fetchTransactions(page - 1);
    }
  };

  return (
    <div>
      <>
        <table border="1" cellPadding="10">
          <thead>
            <tr>
              <th>UTR</th>
              <th>Amount</th>
              <th>Type</th>
              <th>Status</th>
              <th>Created At</th>
            </tr>
          </thead>
          <tbody>
            {transactions.map((txn) => (
              <tr key={txn.id}>
                <td>{txn.utr}</td>
                <td>{txn.amount}</td>
                <td>{txn.type}</td>
                <td>{txn.status}</td>
                <td>{txn.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>

        <div style={{ marginTop: "20px" }}>
          <button onClick={handlePrev} disabled={page === 0}>
            Previous
          </button>

          <span style={{ margin: "0 15px" }}>
            Page {page + 1} of {totalPages}
          </span>

          <button onClick={handleNext} disabled={page >= totalPages - 1}>
            Next
          </button>
        </div>
      </>
    </div>
  );
};

export default Transactions;
