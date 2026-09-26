import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';
import nursingService from '../../services/nursingService';

const AdmissionDetailPage = () => {
  const { id } = useParams();
  const [admission, setAdmission] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');
  const [vitals, setVitals] = useState([]);
  const [nursingNotes, setNursingNotes] = useState([]);
  const [bedHistory, setBedHistory] = useState([]);
  const [transfers, setTransfers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAdmissionDetails();
  }, [id]);

  const fetchAdmissionDetails = async () => {
    setLoading(true);
    try {
      const res = await ipdService.getAdmissionById(id);
      setAdmission(res.data);
      
      // Load sub-resources asynchronously
      nursingService.getVitalsForAdmission(id).then(r => setVitals(r.data || [])).catch(() => {});
      nursingService.getNursingNotesForAdmission(id).then(r => setNursingNotes(r.data || [])).catch(() => {});
      ipdService.getBedHistory(id).then(r => setBedHistory(r.data || [])).catch(() => {});
      ipdService.getTransferHistory(id).then(r => setTransfers(r.data || [])).catch(() => {});
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load admission details');
    } finally {
      setLoading(false);
    }
  };

  const handleApprove = async () => {
    try {
      await ipdService.approveAdmission(id);
      fetchAdmissionDetails();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to approve admission');
    }
  };

  if (loading) return <div className="text-center py-5"><div className="spinner-border text-primary"></div></div>;
  if (error) return <div className="alert alert-danger m-4">{error}</div>;
  if (!admission) return <div className="alert alert-warning m-4">Admission record not found</div>;

  return (
    <div className="container-fluid py-4">
      {/* Header Banner */}
      <div className="card border-0 shadow-sm rounded-3 mb-4 bg-gradient text-dark">
        <div className="card-body p-4 d-flex justify-content-between align-items-center">
          <div>
            <div className="d-flex align-items-center gap-2 mb-1">
              <span className="badge bg-primary fs-6 font-monospace">{admission.admissionId}</span>
              <span className="badge bg-secondary">{admission.status}</span>
              <span className="badge bg-info text-dark">{admission.admissionType}</span>
            </div>
            <h3 className="fw-bold mb-1">{admission.patientName}</h3>
            <p className="text-muted small mb-0">
              <i className="bi bi-telephone me-1"></i> {admission.patientPhone} &bull; {admission.patientGender}, {admission.patientAge} yrs
              &bull; Admitting Doctor: Dr. {admission.doctorName} ({admission.departmentName})
            </p>
          </div>

          <div className="d-flex gap-2">
            {admission.status === 'REQUESTED' && (
              <button onClick={handleApprove} className="btn btn-success shadow-sm">
                <i className="bi bi-check-lg me-1"></i> Approve Admission
              </button>
            )}
            {admission.status === 'APPROVED' && (
              <Link to={`/ipd/admissions/${id}/allocate-bed`} className="btn btn-primary shadow-sm">
                <i className="bi bi-lamp me-1"></i> Allocate Bed & Admit
              </Link>
            )}
            {admission.status === 'ADMITTED' && (
              <>
                <Link to={`/ipd/admissions/${id}/transfer`} className="btn btn-outline-primary shadow-sm">
                  <i className="bi bi-arrow-left-right me-1"></i> Transfer Ward/Bed
                </Link>
                <Link to={`/ipd/admissions/${id}/discharge-plan`} className="btn btn-outline-success shadow-sm">
                  <i className="bi bi-box-arrow-right me-1"></i> Discharge Plan / Summary
                </Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <ul className="nav nav-tabs mb-4 border-bottom-0">
        <li className="nav-item">
          <button
            className={`nav-link fw-semibold ${activeTab === 'overview' ? 'active border-primary border-bottom-0' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            <i className="bi bi-card-text me-1"></i> Overview
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link fw-semibold ${activeTab === 'vitals' ? 'active border-primary border-bottom-0' : ''}`}
            onClick={() => setActiveTab('vitals')}
          >
            <i className="bi bi-heart-pulse me-1"></i> Vitals ({vitals.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link fw-semibold ${activeTab === 'nursing' ? 'active border-primary border-bottom-0' : ''}`}
            onClick={() => setActiveTab('nursing')}
          >
            <i className="bi bi-journal-medical me-1"></i> Nursing Notes ({nursingNotes.length})
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link fw-semibold ${activeTab === 'history' ? 'active border-primary border-bottom-0' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            <i className="bi bi-clock-history me-1"></i> Bed History & Transfers
          </button>
        </li>
      </ul>

      {/* Tab Contents */}
      {activeTab === 'overview' && (
        <div className="row g-4">
          <div className="col-lg-8">
            <div className="card border-0 shadow-sm rounded-3 mb-4">
              <div className="card-header bg-white border-0 py-3 px-4">
                <h5 className="fw-bold mb-0">Admission Clinical Summary</h5>
              </div>
              <div className="card-body p-4">
                <div className="mb-4">
                  <h6 className="text-muted small fw-bold text-uppercase">Reason for Admission</h6>
                  <p className="fs-6 text-dark">{admission.reasonForAdmission}</p>
                </div>
                {admission.clinicalSummary && (
                  <div className="mb-4">
                    <h6 className="text-muted small fw-bold text-uppercase">Initial Clinical Notes</h6>
                    <p className="text-dark bg-light p-3 rounded-3">{admission.clinicalSummary}</p>
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="col-lg-4">
            <div className="card border-0 shadow-sm rounded-3 p-4 mb-4">
              <h5 className="fw-bold mb-3">Bed Allocation</h5>
              {admission.bedCode ? (
                <div className="bg-light p-3 rounded-3 text-center border">
                  <div className="text-muted small fw-bold text-uppercase">Current Ward & Bed</div>
                  <div className="h4 fw-bold text-primary mb-1">{admission.wardName}</div>
                  <span className="badge bg-primary font-monospace px-3 py-2 fs-6">{admission.bedCode}</span>
                </div>
              ) : (
                <div className="alert alert-warning mb-0">No bed allocated yet.</div>
              )}
            </div>
          </div>
        </div>
      )}

      {activeTab === 'vitals' && (
        <div className="card border-0 shadow-sm rounded-3 p-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0">Inpatient Vital Signs</h5>
            <Link to={`/ipd/admissions/${id}/vitals/new`} className="btn btn-sm btn-primary">
              <i className="bi bi-plus-lg me-1"></i> Record Vitals
            </Link>
          </div>
          <div className="table-responsive">
            <table className="table table-hover align-middle">
              <thead className="table-light">
                <tr>
                  <th>Timestamp</th>
                  <th>Recorded By</th>
                  <th>Temp</th>
                  <th>BP</th>
                  <th>Pulse</th>
                  <th>SpO2</th>
                  <th>Resp Rate</th>
                  <th>Pain Score</th>
                  <th>Notes</th>
                </tr>
              </thead>
              <tbody>
                {vitals.map(v => (
                  <tr key={v.id}>
                    <td className="small fw-semibold">{v.recordedAt ? new Date(v.recordedAt).toLocaleString() : 'N/A'}</td>
                    <td>{v.recordedBy}</td>
                    <td>{v.temperature ? `${v.temperature} °C` : '-'}</td>
                    <td className="fw-bold text-primary">{v.bloodPressure || '-'}</td>
                    <td>{v.pulse ? `${v.pulse} bpm` : '-'}</td>
                    <td>{v.oxygenSaturation ? `${v.oxygenSaturation}%` : '-'}</td>
                    <td>{v.respiratoryRate || '-'}</td>
                    <td>{v.painScore !== null ? `${v.painScore}/10` : '-'}</td>
                    <td className="small text-muted">{v.notes || '-'}</td>
                  </tr>
                ))}
                {vitals.length === 0 && (
                  <tr><td colSpan="9" className="text-center text-muted py-4">No vitals recorded yet.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {activeTab === 'nursing' && (
        <div className="card border-0 shadow-sm rounded-3 p-4">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <h5 className="fw-bold mb-0">Nursing Care Notes</h5>
            <Link to={`/ipd/admissions/${id}/nursing-notes/new`} className="btn btn-sm btn-primary">
              <i className="bi bi-plus-lg me-1"></i> Add Nursing Note
            </Link>
          </div>
          <div className="list-group list-group-flush">
            {nursingNotes.map(n => (
              <div key={n.id} className="list-group-item px-0 py-3 border-bottom">
                <div className="d-flex justify-content-between align-items-center mb-2">
                  <span className="badge bg-secondary font-monospace">{n.noteId} &bull; {n.noteType}</span>
                  <span className="small text-muted">{new Date(n.noteDatetime).toLocaleString()} &bull; Nurse: {n.nurseName || 'Nursing Staff'}</span>
                </div>
                <p className="mb-0 text-dark">{n.noteText}</p>
              </div>
            ))}
            {nursingNotes.length === 0 && (
              <div className="text-center text-muted py-4">No nursing notes recorded yet.</div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'history' && (
        <div className="row g-4">
          <div className="col-md-6">
            <div className="card border-0 shadow-sm rounded-3 p-4">
              <h5 className="fw-bold mb-3">Bed Allocation History</h5>
              <div className="list-group list-group-flush">
                {bedHistory.map(b => (
                  <div key={b.id} className="list-group-item px-0 py-2.5">
                    <div className="d-flex justify-content-between">
                      <span className="fw-bold font-monospace text-primary">{b.bedCode} ({b.wardName})</span>
                      <span className="badge bg-light text-dark border">{b.allocationType}</span>
                    </div>
                    <div className="small text-muted">Start: {new Date(b.startDatetime).toLocaleString()}</div>
                    {b.endDatetime && <div className="small text-muted">End: {new Date(b.endDatetime).toLocaleString()}</div>}
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card border-0 shadow-sm rounded-3 p-4">
              <h5 className="fw-bold mb-3">Ward Transfers</h5>
              <div className="list-group list-group-flush">
                {transfers.map(t => (
                  <div key={t.id} className="list-group-item px-0 py-2.5">
                    <div className="fw-bold text-dark">{t.fromWardName} ({t.fromBedCode}) &rarr; {t.toWardName} ({t.toBedCode})</div>
                    <div className="small text-muted">{new Date(t.transferDatetime).toLocaleString()} &bull; Reason: {t.reason}</div>
                  </div>
                ))}
                {transfers.length === 0 && <div className="text-muted small py-2">No ward transfers recorded.</div>}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdmissionDetailPage;
