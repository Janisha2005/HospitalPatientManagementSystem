import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import pharmacyService from '../../services/pharmacyService';

const MedicineListPage = () => {
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchMedicines();
  }, []);

  const fetchMedicines = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getMedicines(0, 100);
      setMedicines(res.data.content || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch medicine master list');
    } finally {
      setLoading(false);
    }
  };

  const filteredMedicines = medicines.filter(m =>
    m.medicineName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    m.medicineCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
    (m.genericName && m.genericName.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Medicine Master Catalog</h2>
          <p className="text-muted small">Manage hospital pharmaceutical catalog, reorder thresholds & category mappings</p>
        </div>
        <div>
          <Link to="/pharmacy/medicines/new" className="btn btn-primary shadow-sm me-2">
            <i className="bi bi-plus-lg me-1"></i> Add Medicine
          </Link>
          <Link to="/pharmacy/categories" className="btn btn-outline-secondary shadow-sm">
            <i className="bi bi-tags me-1"></i> Categories
          </Link>
        </div>
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
                  placeholder="Search by brand name, generic name or code..."
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
                    <th>Code</th>
                    <th>Medicine Details</th>
                    <th>Category</th>
                    <th>Dosage / Strength</th>
                    <th>Available Stock</th>
                    <th>Reorder Level</th>
                    <th>Rx Req.</th>
                    <th>Status</th>
                    <th className="text-end">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredMedicines.length > 0 ? (
                    filteredMedicines.map((m) => (
                      <tr key={m.id}>
                        <td className="fw-bold text-primary">{m.medicineCode}</td>
                        <td>
                          <div className="fw-bold">{m.medicineName}</div>
                          <div className="text-muted small">{m.genericName || 'N/A'} • {m.manufacturer || 'General'}</div>
                        </td>
                        <td>{m.category?.categoryName || 'Uncategorized'}</td>
                        <td>{m.dosageForm} ({m.strength})</td>
                        <td>
                          <span className={`badge ${m.isLowStock ? 'bg-danger' : 'bg-success'} fs-6`}>
                            {m.availableStock || 0} {m.unit || 'Units'}
                          </span>
                        </td>
                        <td>{m.reorderLevel}</td>
                        <td>
                          {m.isPrescriptionRequired ? (
                            <span className="badge bg-warning text-dark">Rx</span>
                          ) : (
                            <span className="badge bg-secondary">OTC</span>
                          )}
                        </td>
                        <td>
                          <span className={`badge ${m.isActive ? 'bg-success-subtle text-success' : 'bg-danger-subtle text-danger'}`}>
                            {m.isActive ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td className="text-end">
                          <Link to={`/pharmacy/medicines/edit/${m.id}`} className="btn btn-sm btn-outline-primary me-1">
                            <i className="bi bi-pencil"></i> Edit
                          </Link>
                          <Link to={`/pharmacy/batches?medicineId=${m.id}`} className="btn btn-sm btn-outline-info">
                            <i className="bi bi-boxes"></i> Batches
                          </Link>
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="9" className="text-center py-4 text-muted">No medicines found in master catalog.</td>
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

export default MedicineListPage;
