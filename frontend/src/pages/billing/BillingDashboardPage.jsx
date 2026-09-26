import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import billingService from '../../services/billingService';

const BillingDashboardPage = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboardMetrics();
  }, []);

  const fetchDashboardMetrics = async () => {
    try {
      setLoading(true);
      const res = await billingService.getDashboardMetrics();
      if (res.data && res.data.success) {
        setMetrics(res.data.data);
      }
    } catch (err) {
      setError('Failed to load billing dashboard metrics.');
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

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center py-5">
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading Billing Dashboard...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1" style={{ color: '#1e293b' }}>
            <i className="bi bi-cash-stack text-success me-2"></i>Billing & Financial Dashboard
          </h2>
          <p className="text-muted small mb-0">Overview of hospital billing revenue, payments, ledgers, and outstanding balances</p>
        </div>
        <div className="d-flex gap-2">
          <Link to="/billing/bills/new" className="btn btn-primary btn-sm rounded-pill px-3">
            <i className="bi bi-plus-lg me-1"></i>Create Draft Bill
          </Link>
          <Link to="/billing/charges" className="btn btn-outline-secondary btn-sm rounded-pill px-3">
            <i className="bi bi-list-columns-reverse me-1"></i>Charge Master
          </Link>
          <Link to="/billing/reports" className="btn btn-outline-info btn-sm rounded-pill px-3">
            <i className="bi bi-graph-up-arrow me-1"></i>Revenue Reports
          </Link>
        </div>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {metrics && (
        <>
          {/* KPI Stat Cards */}
          <div className="row g-3 mb-4">
            <div className="col-12 col-sm-6 col-xl-3">
              <div className="card border-0 shadow-sm rounded-4 h-100" style={{ background: 'linear-gradient(135deg, #0f172a 0%, #1e293b 100%)', color: '#fff' }}>
                <div className="card-body p-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-uppercase fw-semibold text-white-50 small">Today's Invoices</span>
                    <div className="rounded-circle bg-primary bg-opacity-25 p-2 d-flex align-items-center justify-content-center" style={{ width: '42px', height: '42px' }}>
                      <i className="bi bi-receipt fs-5 text-info"></i>
                    </div>
                  </div>
                  <h3 className="fw-bold mb-1">{metrics.todayBillsCount}</h3>
                  <div className="small text-white-50">Invoices Billed Today</div>
                </div>
              </div>
            </div>

            <div className="col-12 col-sm-6 col-xl-3">
              <div className="card border-0 shadow-sm rounded-4 h-100 bg-white">
                <div className="card-body p-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-uppercase fw-semibold text-muted small">Today's Billed Value</span>
                    <div className="rounded-circle bg-success bg-opacity-10 p-2 d-flex align-items-center justify-content-center" style={{ width: '42px', height: '42px' }}>
                      <i className="bi bi-currency-rupee fs-5 text-success"></i>
                    </div>
                  </div>
                  <h3 className="fw-bold text-dark mb-1">{formatCurrency(metrics.todayBilledAmount)}</h3>
                  <div className="small text-muted">Total Gross Billed Today</div>
                </div>
              </div>
            </div>

            <div className="col-12 col-sm-6 col-xl-3">
              <div className="card border-0 shadow-sm rounded-4 h-100 bg-white">
                <div className="card-body p-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-uppercase fw-semibold text-muted small">Today's Collections</span>
                    <div className="rounded-circle bg-info bg-opacity-10 p-2 d-flex align-items-center justify-content-center" style={{ width: '42px', height: '42px' }}>
                      <i className="bi bi-wallet2 fs-5 text-info"></i>
                    </div>
                  </div>
                  <h3 className="fw-bold text-dark mb-1">{formatCurrency(metrics.todayCollectionsAmount)}</h3>
                  <div className="small text-success">Total Cash/UPI/Card Received</div>
                </div>
              </div>
            </div>

            <div className="col-12 col-sm-6 col-xl-3">
              <div className="card border-0 shadow-sm rounded-4 h-100 bg-white">
                <div className="card-body p-4">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="text-uppercase fw-semibold text-muted small">Total Outstanding</span>
                    <div className="rounded-circle bg-danger bg-opacity-10 p-2 d-flex align-items-center justify-content-center" style={{ width: '42px', height: '42px' }}>
                      <i className="bi bi-exclamation-octagon fs-5 text-danger"></i>
                    </div>
                  </div>
                  <h3 className="fw-bold text-danger mb-1">{formatCurrency(metrics.totalOutstandingAmount)}</h3>
                  <div className="small text-muted">Pending Accounts Receivable</div>
                </div>
              </div>
            </div>
          </div>

          {/* Revenue Breakdown by Category */}
          <div className="row g-4 mb-4">
            <div className="col-12 col-lg-7">
              <div className="card border-0 shadow-sm rounded-4 h-100 bg-white">
                <div className="card-header bg-transparent border-0 pt-4 px-4 pb-0 d-flex justify-content-between align-items-center">
                  <h5 className="fw-bold mb-0 text-dark">Revenue by Department Service</h5>
                  <Link to="/billing/reports" className="btn btn-link btn-sm text-decoration-none">View Detailed Report &rarr;</Link>
                </div>
                <div className="card-body p-4">
                  <div className="row g-3">
                    <div className="col-6 col-md-4">
                      <div className="p-3 rounded-3 bg-light border border-light-subtle">
                        <div className="text-muted small mb-1"><i className="bi bi-hospital text-primary me-1"></i>OPD Revenue</div>
                        <div className="fw-bold text-dark fs-5">{formatCurrency(metrics.opdRevenue)}</div>
                      </div>
                    </div>
                    <div className="col-6 col-md-4">
                      <div className="p-3 rounded-3 bg-light border border-light-subtle">
                        <div className="text-muted small mb-1"><i className="bi bi-building text-info me-1"></i>IPD Revenue</div>
                        <div className="fw-bold text-dark fs-5">{formatCurrency(metrics.ipdRevenue)}</div>
                      </div>
                    </div>
                    <div className="col-6 col-md-4">
                      <div className="p-3 rounded-3 bg-light border border-light-subtle">
                        <div className="text-muted small mb-1"><i className="bi bi-capsule text-success me-1"></i>Pharmacy Revenue</div>
                        <div className="fw-bold text-dark fs-5">{formatCurrency(metrics.pharmacyRevenue)}</div>
                      </div>
                    </div>
                    <div className="col-6 col-md-6">
                      <div className="p-3 rounded-3 bg-light border border-light-subtle">
                        <div className="text-muted small mb-1"><i className="bi bi-virus text-warning me-1"></i>Laboratory Revenue</div>
                        <div className="fw-bold text-dark fs-5">{formatCurrency(metrics.laboratoryRevenue)}</div>
                      </div>
                    </div>
                    <div className="col-12 col-md-6">
                      <div className="p-3 rounded-3 bg-light border border-light-subtle">
                        <div className="text-muted small mb-1"><i className="bi bi-file-earmark-medical text-danger me-1"></i>Radiology Revenue</div>
                        <div className="fw-bold text-dark fs-5">{formatCurrency(metrics.radiologyRevenue)}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div className="col-12 col-lg-5">
              <div className="card border-0 shadow-sm rounded-4 h-100 bg-white">
                <div className="card-header bg-transparent border-0 pt-4 px-4 pb-0">
                  <h5 className="fw-bold mb-0 text-dark">Quick Financial Links</h5>
                </div>
                <div className="card-body p-4">
                  <div className="list-group list-group-flush rounded-3 border">
                    <Link to="/billing/bills" className="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-semibold text-dark"><i className="bi bi-file-earmark-text text-primary me-2"></i>Manage Bills & Invoices</div>
                        <div className="small text-muted">View all OPD, IPD, and Pharmacy bills</div>
                      </div>
                      <i className="bi bi-chevron-right text-muted"></i>
                    </Link>
                    <Link to="/billing/payments" className="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-semibold text-dark"><i className="bi bi-credit-card text-success me-2"></i>Payment Receipts</div>
                        <div className="small text-muted">Record Cash, UPI, and Card collections</div>
                      </div>
                      <i className="bi bi-chevron-right text-muted"></i>
                    </Link>
                    <Link to="/billing/refunds" className="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-semibold text-dark"><i className="bi bi-arrow-return-left text-warning me-2"></i>Refunds & Adjustments</div>
                        <div className="small text-muted">Process patient refunds and returns</div>
                      </div>
                      <i className="bi bi-chevron-right text-muted"></i>
                    </Link>
                    <Link to="/billing/credit-notes" className="list-group-item list-group-item-action d-flex justify-content-between align-items-center py-3">
                      <div>
                        <div className="fw-semibold text-dark"><i className="bi bi-file-earmark-diff text-danger me-2"></i>Credit Notes</div>
                        <div className="small text-muted">Issue billing credit notes</div>
                      </div>
                      <i className="bi bi-chevron-right text-muted"></i>
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Recent Bills Table */}
          <div className="card border-0 shadow-sm rounded-4 bg-white">
            <div className="card-header bg-transparent border-0 pt-4 px-4 pb-2 d-flex justify-content-between align-items-center">
              <h5 className="fw-bold mb-0 text-dark">Recent Invoices</h5>
              <Link to="/billing/bills" className="btn btn-outline-primary btn-sm rounded-pill px-3">View All Invoices</Link>
            </div>
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
                      <th>Status</th>
                      <th className="text-end pe-4">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {metrics.recentBills && metrics.recentBills.length > 0 ? (
                      metrics.recentBills.map((bill) => (
                        <tr key={bill.id}>
                          <td className="ps-4 fw-semibold text-primary">{bill.billNumber}</td>
                          <td>
                            <div className="fw-medium text-dark">{bill.patientName}</div>
                            <div className="small text-muted">{bill.patientCode}</div>
                          </td>
                          <td><span className="badge bg-secondary bg-opacity-10 text-secondary border">{bill.billType}</span></td>
                          <td>{bill.billDate}</td>
                          <td className="fw-bold text-dark">{formatCurrency(bill.grandTotal)}</td>
                          <td>
                            <span className={`badge ${
                              bill.status === 'PAID' ? 'bg-success' :
                              bill.status === 'PARTIALLY_PAID' ? 'bg-warning text-dark' :
                              bill.status === 'GENERATED' ? 'bg-info text-dark' :
                              bill.status === 'CANCELLED' ? 'bg-danger' : 'bg-secondary'
                            }`}>
                              {bill.status}
                            </span>
                          </td>
                          <td className="text-end pe-4">
                            <Link to={`/billing/bills/${bill.id}`} className="btn btn-sm btn-light border rounded-pill">View</Link>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="7" className="text-center py-4 text-muted">No recent bills found.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default BillingDashboardPage;
