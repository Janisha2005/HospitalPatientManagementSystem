import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import billingService from '../../services/billingService';

const BillListPage = () => {
  const [bills, setBills] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchBills();
  }, [searchTerm]);

  const fetchBills = async () => {
    try {
      setLoading(true);
      const res = await billingService.getAllBills({ query: searchTerm, page: 0, size: 50 });
      if (res.data && res.data.success) {
        setBills(res.data.data.content || []);
      }
    } catch (err) {
      setError('Failed to fetch bills list.');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return (amount || 0).toLocaleString('en-IN', {
      style: 'currency',
      currency: 'INR'
    });
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1" style={{ color: '#1e293b' }}>
            <i className="bi bi-file-earmark-text text-primary me-2"></i>Invoices & Billing Directory
          </h2>
          <p className="text-muted small mb-0">Master listing of OPD, IPD, Pharmacy, Laboratory, and Radiology bills</p>
        </div>
        <div className="d-flex gap-2">
          <Link to="/billing/bills/new" className="btn btn-primary rounded-pill px-3">
            <i className="bi bi-plus-lg me-1"></i>Create Draft Bill
          </Link>
          <Link to="/billing/dashboard" className="btn btn-outline-secondary rounded-pill px-3">
            <i className="bi bi-speedometer2 me-1"></i>Dashboard
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-4 bg-white mb-4">
        <div className="card-body p-3">
          <div className="row g-3">
            <div className="col-12 col-md-6 col-lg-4">
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0 text-muted">
                  <i className="bi bi-search"></i>
                </span>
                <input
                  type="text"
                  className="form-control border-start-0 ps-0"
                  placeholder="Search by bill number, patient name..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="card border-0 shadow-sm rounded-4 bg-white">
        <div className="card-body px-0 pb-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light text-muted small text-uppercase">
                <tr>
                  <th className="ps-4">Bill Number</th>
                  <th>Patient</th>
                  <th>Bill Type</th>
                  <th>Bill Date</th>
                  <th>Grand Total</th>
                  <th>Paid</th>
                  <th>Outstanding</th>
                  <th>Status</th>
                  <th className="text-end pe-4">Actions</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan="9" className="text-center py-4 text-muted">Loading invoices...</td>
                  </tr>
                ) : bills.length > 0 ? (
                  bills.map((b) => (
                    <tr key={b.id}>
                      <td className="ps-4 fw-semibold text-primary">{b.billNumber}</td>
                      <td>
                        <div className="fw-medium text-dark">{b.patientName}</div>
                        <div className="small text-muted">{b.patientCode}</div>
                      </td>
                      <td><span className="badge bg-secondary bg-opacity-10 text-secondary border">{b.billType}</span></td>
                      <td>{b.billDate}</td>
                      <td className="fw-bold text-dark">{formatCurrency(b.grandTotal)}</td>
                      <td className="text-success fw-medium">{formatCurrency(b.paidAmount)}</td>
                      <td className="text-danger fw-medium">{formatCurrency(b.outstandingAmount)}</td>
                      <td>
                        <span className={`badge ${
                          b.status === 'PAID' ? 'bg-success' :
                          b.status === 'PARTIALLY_PAID' ? 'bg-warning text-dark' :
                          b.status === 'GENERATED' ? 'bg-info text-dark' :
                          b.status === 'CANCELLED' ? 'bg-danger' : 'bg-secondary'
                        }`}>
                          {b.status}
                        </span>
                      </td>
                      <td className="text-end pe-4">
                        <Link to={`/billing/bills/${b.id}`} className="btn btn-sm btn-light border me-1">View</Link>
                        <Link to={`/billing/bills/${b.id}/invoice`} className="btn btn-sm btn-outline-primary">Invoice</Link>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="9" className="text-center py-4 text-muted">No invoices found.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BillListPage;
