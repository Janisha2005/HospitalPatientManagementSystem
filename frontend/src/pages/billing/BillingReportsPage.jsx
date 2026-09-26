import React, { useState, useEffect } from 'react';
import billingService from '../../services/billingService';

const BillingReportsPage = () => {
  const [period, setPeriod] = useState('TODAY');
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchReport();
  }, [period]);

  const fetchReport = async () => {
    try {
      setLoading(true);
      const res = await billingService.getRevenueReport(period);
      if (res.data && res.data.success) {
        setReport(res.data.data);
      }
    } catch (err) {
      setError('Failed to fetch revenue report.');
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
            <i className="bi bi-graph-up-arrow text-info me-2"></i>Financial & Revenue Reports
          </h2>
          <p className="text-muted small mb-0">Daily collections, departmental revenue breakdown, and payment method summaries</p>
        </div>
        <div className="btn-group" role="group">
          <button
            className={`btn btn-sm ${period === 'TODAY' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setPeriod('TODAY')}
          >
            Today
          </button>
          <button
            className={`btn btn-sm ${period === 'THIS_WEEK' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setPeriod('THIS_WEEK')}
          >
            This Week
          </button>
          <button
            className={`btn btn-sm ${period === 'THIS_MONTH' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setPeriod('THIS_MONTH')}
          >
            This Month
          </button>
          <button
            className={`btn btn-sm ${period === 'YEAR_TO_DATE' ? 'btn-primary' : 'btn-outline-secondary'}`}
            onClick={() => setPeriod('YEAR_TO_DATE')}
          >
            Year to Date
          </button>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {loading ? (
        <div className="d-flex justify-content-center py-5">
          <div className="spinner-border text-primary" role="status">
            <span className="visually-hidden">Loading Revenue Report...</span>
          </div>
        </div>
      ) : report ? (
        <>
          {/* Summary Cards */}
          <div className="row g-3 mb-4">
            <div className="col-12 col-sm-6 col-md-3">
              <div className="card border-0 shadow-sm rounded-4 p-3 bg-white">
                <div className="text-muted small mb-1 fw-bold">Total Gross Billed</div>
                <div className="fw-bold text-dark fs-4">{formatCurrency(report.totalBilled)}</div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="card border-0 shadow-sm rounded-4 p-3 bg-white">
                <div className="text-muted small mb-1 fw-bold">Total Collections</div>
                <div className="fw-bold text-success fs-4">{formatCurrency(report.totalCollected)}</div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="card border-0 shadow-sm rounded-4 p-3 bg-white">
                <div className="text-muted small mb-1 fw-bold">Total Outstanding</div>
                <div className="fw-bold text-danger fs-4">{formatCurrency(report.totalOutstanding)}</div>
              </div>
            </div>
            <div className="col-12 col-sm-6 col-md-3">
              <div className="card border-0 shadow-sm rounded-4 p-3 bg-white">
                <div className="text-muted small mb-1 fw-bold">Time Period</div>
                <div className="fw-bold text-info fs-5 mt-1">{report.period}</div>
              </div>
            </div>
          </div>

          <div className="row g-4">
            {/* Revenue by Category */}
            <div className="col-12 col-md-6">
              <div className="card border-0 shadow-sm rounded-4 bg-white h-100">
                <div className="card-header bg-transparent border-0 pt-4 px-4 pb-2">
                  <h5 className="fw-bold text-dark mb-0">Revenue by Service Category</h5>
                </div>
                <div className="card-body px-0 pb-0">
                  <table className="table table-hover align-middle mb-0">
                    <thead className="table-light text-muted small text-uppercase">
                      <tr>
                        <th className="ps-4">Category</th>
                        <th className="text-end pe-4">Total Revenue</th>
                      </tr>
                    </thead>
                    <tbody>
                      {report.revenueByCategory && Object.entries(report.revenueByCategory).map(([cat, amt]) => (
                        <tr key={cat}>
                          <td className="ps-4 fw-medium text-dark">{cat}</td>
                          <td className="text-end pe-4 fw-bold">{formatCurrency(amt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>

            {/* Collection by Method */}
            <div className="col-12 col-md-6">
              <div className="card border-0 shadow-sm rounded-4 bg-white h-100">
                <div className="card-header bg-transparent border-0 pt-4 px-4 pb-2">
                  <h5 className="fw-bold text-dark mb-0">Collections by Payment Method</h5>
                </div>
                <div className="card-body px-0 pb-0">
                  <table className="table table-hover align-middle mb-0">
                    <thead className="table-light text-muted small text-uppercase">
                      <tr>
                        <th className="ps-4">Payment Method</th>
                        <th className="text-end pe-4">Collected Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {report.collectionByPaymentMethod && Object.entries(report.collectionByPaymentMethod).map(([method, amt]) => (
                        <tr key={method}>
                          <td className="ps-4 fw-medium text-dark">{method}</td>
                          <td className="text-end pe-4 fw-bold text-success">{formatCurrency(amt)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </>
      ) : null}
    </div>
  );
};

export default BillingReportsPage;
