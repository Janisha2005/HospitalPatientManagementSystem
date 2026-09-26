import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const SupplierListPage = () => {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchSuppliers();
  }, []);

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getSuppliers(0, 100);
      setSuppliers(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch suppliers');
    } finally {
      setLoading(false);
    }
  };

  const filteredSuppliers = suppliers.filter(s =>
    s.supplierName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    s.supplierCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (s.phone && s.phone.includes(searchTerm))
  );

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Pharmaceutical Suppliers</h2>
          <p className="text-muted small">Manage accredited vendors, drug license numbers, GST and payment terms</p>
        </div>
        <Link to="/pharmacy/suppliers/new" className="btn btn-primary shadow-sm">
          <i className="bi bi-plus-lg me-1"></i> Register Supplier
        </Link>
      </div>

      {error && <div className="alert alert-danger mb-4">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-header bg-white py-3">
          <div className="row g-3 align-items-center">
            <div className="col-md-6">
              <div className="input-group">
                <span className="input-group-text bg-light border-end-0"><i className="bi bi-search"></i></span>
                <input
                  type="text"
                  className="form-control bg-light border-start-0"
                  placeholder="Search by supplier code, name or phone..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="card-body p-0">
          {loading ? (
            <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>
          ) : (
            <div className="table-responsive">
              <table className="table table-hover align-middle mb-0">
                <thead className="table-light small text-uppercase">
                  <tr>
                    <th>Supplier Code</th>
                    <th>Supplier Name</th>
                    <th>Contact Person</th>
                    <th>Phone / Email</th>
                    <th>City / State</th>
                    <th>GST Number</th>
                    <th>Drug License</th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredSuppliers.length > 0 ? (
                    filteredSuppliers.map((s) => (
                      <tr key={s.id}>
                        <td className="fw-bold text-primary">{s.supplierCode}</td>
                        <td className="fw-bold">{s.supplierName}</td>
                        <td>{s.contactPerson || 'N/A'}</td>
                        <td>
                          <div>{s.phone || 'N/A'}</div>
                          <div className="text-muted small">{s.email || ''}</div>
                        </td>
                        <td>{s.city ? `${s.city}, ${s.state}` : 'N/A'}</td>
                        <td><span className="badge bg-light text-dark border">{s.gstNumber || 'N/A'}</span></td>
                        <td><span className="badge bg-light text-dark border">{s.drugLicenseNumber || 'N/A'}</span></td>
                        <td>
                          <span className={`badge ${s.isActive ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'}`}>
                            {s.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="text-end">
                          <Link to={`/pharmacy/suppliers/edit/${s.id}`} className="btn btn-sm btn-outline-primary">
                            <i className="bi bi-pencil"></i> Edit
                          </Link>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9" className="text-center py-4 text-muted">No suppliers registered.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default SupplierListPage;
