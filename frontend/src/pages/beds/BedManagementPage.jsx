import React, { useState, useEffect } from 'react';
import bedService from '../../services/bedService';
import wardService from '../../services/wardService';

const BedManagementPage = () => {
  const [beds, setBeds] = useState([]);
  const [wards, setWards] = useState([]);
  const [selectedWardId, setSelectedWardId] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedBed, setSelectedBed] = useState(null);

  // Form State for Bed Creation/Edit
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({
    id: null,
    bedCode: '',
    wardId: '',
    bedNumber: '',
    bedType: 'STANDARD',
    status: 'AVAILABLE',
    isActive: true
  });

  useEffect(() => {
    fetchWards();
    fetchBeds();
  }, [selectedWardId, selectedStatus]);

  const fetchWards = async () => {
    try {
      const res = await wardService.getAllWards();
      setWards(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchBeds = async () => {
    setLoading(true);
    try {
      const res = await bedService.getAllBeds(
        selectedWardId || null,
        null,
        selectedStatus || null
      );
      setBeds(res.data || []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load beds');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreateModal = () => {
    setFormData({
      id: null,
      bedCode: '',
      wardId: wards.length > 0 ? wards[0].id : '',
      bedNumber: '',
      bedType: 'STANDARD',
      status: 'AVAILABLE',
      isActive: true
    });
    setShowModal(true);
  };

  const handleStatusChange = async (bedId, newStatus) => {
    try {
      await bedService.updateBedStatus(bedId, newStatus);
      fetchBeds();
      if (selectedBed && selectedBed.id === bedId) {
        setSelectedBed(prev => ({ ...prev, status: newStatus }));
      }
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update bed status');
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    try {
      if (formData.id) {
        await bedService.updateBed(formData.id, formData);
      } else {
        await bedService.createBed(formData);
      }
      setShowModal(false);
      fetchBeds();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save bed');
    }
  };

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'AVAILABLE': return 'bg-success text-white';
      case 'OCCUPIED': return 'bg-primary text-white';
      case 'CLEANING': return 'bg-warning text-dark';
      case 'RESERVED': return 'bg-info text-dark';
      case 'MAINTENANCE': return 'bg-danger text-white';
      case 'OUT_OF_SERVICE': return 'bg-secondary text-white';
      default: return 'bg-secondary';
    }
  };

  // Group beds by Ward for visual Bed Map
  const bedsByWard = wards.map(w => ({
    ward: w,
    beds: beds.filter(b => b.wardId === w.id)
  })).filter(g => selectedWardId ? g.ward.id === Number(selectedWardId) : true);

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">Bed & Ward Occupancy Map</h2>
          <p className="text-muted small">Visual real-time bed map, allocations, and maintenance workflow</p>
        </div>
        <button onClick={handleOpenCreateModal} className="btn btn-primary shadow-sm">
          <i className="bi bi-plus-lg me-1"></i> Add New Bed
        </button>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      {/* Filter Bar */}
      <div className="card border-0 shadow-sm p-3 mb-4 rounded-3">
        <div className="row g-3 align-items-center">
          <div className="col-md-5">
            <label className="form-label small fw-bold text-uppercase text-muted">Filter by Ward</label>
            <select
              className="form-select"
              value={selectedWardId}
              onChange={(e) => setSelectedWardId(e.target.value)}
            >
              <option value="">All Wards ({wards.length})</option>
              {wards.map(w => (
                <option key={w.id} value={w.id}>{w.wardName} ({w.wardCode})</option>
              ))}
            </select>
          </div>

          <div className="col-md-5">
            <label className="form-label small fw-bold text-uppercase text-muted">Filter by Status</label>
            <select
              className="form-select"
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              <option value="AVAILABLE">AVAILABLE</option>
              <option value="OCCUPIED">OCCUPIED</option>
              <option value="CLEANING">CLEANING</option>
              <option value="RESERVED">RESERVED</option>
              <option value="MAINTENANCE">MAINTENANCE</option>
              <option value="OUT_OF_SERVICE">OUT_OF_SERVICE</option>
            </select>
          </div>

          <div className="col-md-2 d-flex align-items-end">
            <button
              onClick={() => { setSelectedWardId(''); setSelectedStatus(''); }}
              className="btn btn-outline-secondary w-100 mt-4"
            >
              Reset Filters
            </button>
          </div>
        </div>
      </div>

      {loading ? (
        <div className="text-center py-5">
          <div className="spinner-border text-primary"></div>
        </div>
      ) : (
        <div className="row g-4">
          <div className="col-lg-8">
            {bedsByWard.map(group => (
              <div key={group.ward.id} className="card border-0 shadow-sm rounded-3 mb-4">
                <div className="card-header bg-white border-0 py-3 px-4 d-flex justify-content-between align-items-center border-bottom">
                  <div>
                    <h5 className="fw-bold mb-0 text-dark">{group.ward.wardName}</h5>
                    <span className="badge bg-secondary font-monospace mt-1">{group.ward.wardCode}</span>
                    <span className="small text-muted ms-2">&bull; {group.ward.wardType} &bull; {group.ward.building}</span>
                  </div>
                  <span className="small text-muted">
                    Available: <strong className="text-success">{group.beds.filter(b => b.status === 'AVAILABLE').length}</strong> / {group.beds.length}
                  </span>
                </div>
                <div className="card-body p-4">
                  <div className="row g-3">
                    {group.beds.map(b => (
                      <div key={b.id} className="col-6 col-sm-4 col-md-3">
                        <div
                          onClick={() => setSelectedBed(b)}
                          className={`card p-3 rounded-3 cursor-pointer text-center transition hover-lift ${selectedBed?.id === b.id ? 'border-2 border-primary shadow' : 'border'}`}
                        >
                          <div className="fw-bold font-monospace mb-1">{b.bedCode}</div>
                          <span className={`badge ${getStatusBadgeClass(b.status)} mx-auto mb-2`}>
                            {b.status}
                          </span>
                          <div className="small text-muted">{b.bedType}</div>
                          {b.currentPatientName && (
                            <div className="mt-2 pt-2 border-top text-truncate small fw-bold text-primary">
                              <i className="bi bi-person-fill me-1"></i>{b.currentPatientName}
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                    {group.beds.length === 0 && (
                      <div className="col-12 text-center text-muted py-3">No beds configured in this ward.</div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Side Details Panel for Selected Bed */}
          <div className="col-lg-4">
            {selectedBed ? (
              <div className="card border-0 shadow-sm rounded-3 sticky-top" style={{ top: '20px' }}>
                <div className="card-header bg-primary text-white py-3 px-4 d-flex justify-content-between align-items-center">
                  <h5 className="fw-bold mb-0">Bed Details</h5>
                  <button onClick={() => setSelectedBed(null)} className="btn-close btn-close-white"></button>
                </div>
                <div className="card-body p-4">
                  <div className="text-center mb-3">
                    <span className="display-6 font-monospace fw-bold text-primary">{selectedBed.bedCode}</span>
                    <div className="mt-2">
                      <span className={`badge ${getStatusBadgeClass(selectedBed.status)} px-3 py-2 fs-6`}>
                        {selectedBed.status}
                      </span>
                    </div>
                  </div>

                  <hr />

                  <div className="mb-3">
                    <div className="text-muted small fw-bold text-uppercase">Ward</div>
                    <div className="fw-bold text-dark">{selectedBed.wardName} ({selectedBed.wardCode})</div>
                  </div>

                  <div className="mb-3">
                    <div className="text-muted small fw-bold text-uppercase">Bed Number & Type</div>
                    <div className="fw-bold text-dark">Bed-{selectedBed.bedNumber} &bull; {selectedBed.bedType}</div>
                  </div>

                  {selectedBed.status === 'OCCUPIED' && (
                    <div className="bg-light p-3 rounded-3 mb-3 border">
                      <div className="text-muted small fw-bold text-uppercase mb-1">Occupant Information</div>
                      <div className="fw-bold text-primary fs-6">{selectedBed.currentPatientName}</div>
                      <div className="text-muted small font-monospace">Admission ID: {selectedBed.currentAdmissionId}</div>
                    </div>
                  )}

                  <div className="mb-3">
                    <label className="form-label small fw-bold text-uppercase text-muted">Change Bed Status</label>
                    <select
                      className="form-select"
                      value={selectedBed.status}
                      onChange={(e) => handleStatusChange(selectedBed.id, e.target.value)}
                    >
                      <option value="AVAILABLE">AVAILABLE</option>
                      <option value="OCCUPIED">OCCUPIED</option>
                      <option value="CLEANING">CLEANING</option>
                      <option value="RESERVED">RESERVED</option>
                      <option value="MAINTENANCE">MAINTENANCE</option>
                      <option value="OUT_OF_SERVICE">OUT_OF_SERVICE</option>
                    </select>
                  </div>
                </div>
              </div>
            ) : (
              <div className="card border-0 shadow-sm rounded-3 p-4 text-center text-muted">
                <i className="bi bi-info-circle display-4 text-secondary mb-3"></i>
                <p className="mb-0">Click any bed on the bed map to inspect patient occupant details and change administrative status.</p>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Bed Creation Modal */}
      {showModal && (
        <div className="modal show d-block tab-modal-backdrop" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-3">
              <div className="modal-header bg-light">
                <h5 className="modal-title fw-bold">Create New Bed</h5>
                <button onClick={() => setShowModal(false)} className="btn-close"></button>
              </div>
              <form onSubmit={handleFormSubmit}>
                <div className="modal-body p-4">
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Bed Code *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. GEN-A-004"
                      value={formData.bedCode}
                      onChange={(e) => setFormData({ ...formData, bedCode: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Ward *</label>
                    <select
                      className="form-select"
                      value={formData.wardId}
                      onChange={(e) => setFormData({ ...formData, wardId: e.target.value })}
                      required
                    >
                      {wards.map(w => (
                        <option key={w.id} value={w.id}>{w.wardName} ({w.wardCode})</option>
                      ))}
                    </select>
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Bed Number *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. 004"
                      value={formData.bedNumber}
                      onChange={(e) => setFormData({ ...formData, bedNumber: e.target.value })}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label fw-semibold">Bed Type *</label>
                    <select
                      className="form-select"
                      value={formData.bedType}
                      onChange={(e) => setFormData({ ...formData, bedType: e.target.value })}
                    >
                      <option value="STANDARD">STANDARD</option>
                      <option value="SEMI_PRIVATE">SEMI_PRIVATE</option>
                      <option value="PRIVATE">PRIVATE</option>
                      <option value="ICU">ICU</option>
                      <option value="ISOLATION">ISOLATION</option>
                      <option value="EMERGENCY">EMERGENCY</option>
                    </select>
                  </div>
                </div>
                <div className="modal-footer bg-light">
                  <button type="button" onClick={() => setShowModal(false)} className="btn btn-secondary">Cancel</button>
                  <button type="submit" className="btn btn-primary">Save Bed</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BedManagementPage;
