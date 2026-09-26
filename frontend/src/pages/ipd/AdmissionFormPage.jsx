import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import ipdService from '../../services/ipdService';
import { patientService } from '../../services/patientService';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import wardService from '../../services/wardService';
import bedService from '../../services/bedService';

const AdmissionFormPage = () => {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    patientId: '',
    admittingDoctorId: '',
    departmentId: '',
    wardId: '',
    bedId: '',
    admissionType: 'ELECTIVE',
    admissionDate: new Date().toISOString().split('T')[0],
    admissionTime: '10:00',
    reasonForAdmission: '',
    clinicalSummary: '',
    expectedDischargeDate: ''
  });

  const [patients, setPatients] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [wards, setWards] = useState([]);
  const [availableBeds, setAvailableBeds] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchInitialData();
  }, []);

  useEffect(() => {
    if (formData.departmentId) {
      fetchWards(formData.departmentId);
    }
  }, [formData.departmentId]);

  useEffect(() => {
    if (formData.wardId) {
      fetchAvailableBeds(formData.wardId);
    }
  }, [formData.wardId]);

  const fetchInitialData = async () => {
    try {
      const [pRes, dRes, deptRes] = await Promise.all([
        patientService.getAllPatients(),
        doctorService.getAllDoctors(),
        departmentService.getAllActiveDepartments()
      ]);
      setPatients(pRes.data?.data?.content || pRes.data?.content || pRes.data || []);
      setDoctors(dRes.data?.data?.content || dRes.data?.content || dRes.data || []);
      setDepartments(deptRes.data?.data || deptRes.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchWards = async (deptId) => {
    try {
      const res = await wardService.getAllWards(deptId, true);
      setWards(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const fetchAvailableBeds = async (wardId) => {
    try {
      const res = await bedService.getAvailableBeds(wardId);
      setAvailableBeds(res.data || []);
    } catch (err) {
      console.error(err);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const payload = { ...formData };
      if (!payload.wardId) delete payload.wardId;
      if (!payload.bedId) delete payload.bedId;

      const res = await ipdService.createAdmission(payload);
      navigate(`/ipd/admissions/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create admission request');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container-fluid py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="h3 fw-bold text-dark mb-1">New IPD Admission Request</h2>
          <p className="text-muted small">Initiate patient inpatient admission and optional bed allocation</p>
        </div>
        <Link to="/ipd/admissions" className="btn btn-outline-secondary">
          <i className="bi bi-arrow-left me-1"></i> Back to Registry
        </Link>
      </div>

      {error && <div className="alert alert-danger shadow-sm">{error}</div>}

      <div className="card border-0 shadow-sm rounded-3">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <div className="row g-3">
              <div className="col-md-6">
                <label className="form-label fw-semibold">Patient *</label>
                <select
                  name="patientId"
                  className="form-select"
                  value={formData.patientId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Patient</option>
                  {patients.map(p => (
                    <option key={p.id} value={p.id}>{p.firstName} {p.lastName} ({p.patientId})</option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Admitting Doctor *</label>
                <select
                  name="admittingDoctorId"
                  className="form-select"
                  value={formData.admittingDoctorId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Doctor</option>
                  {doctors.map(d => (
                    <option key={d.id} value={d.id}>Dr. {d.firstName} {d.lastName} ({d.specialization})</option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Department *</label>
                <select
                  name="departmentId"
                  className="form-select"
                  value={formData.departmentId}
                  onChange={handleChange}
                  required
                >
                  <option value="">Select Department</option>
                  {departments.map(dept => (
                    <option key={dept.id} value={dept.id}>{dept.departmentName}</option>
                  ))}
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Admission Type *</label>
                <select
                  name="admissionType"
                  className="form-select"
                  value={formData.admissionType}
                  onChange={handleChange}
                  required
                >
                  <option value="ELECTIVE">ELECTIVE</option>
                  <option value="EMERGENCY">EMERGENCY</option>
                  <option value="TRANSFER">TRANSFER</option>
                  <option value="OBSERVATION">OBSERVATION</option>
                  <option value="DAY_CARE">DAY_CARE</option>
                </select>
              </div>

              <div className="col-md-4">
                <label className="form-label fw-semibold">Admission Date & Time *</label>
                <div className="input-group">
                  <input
                    type="date"
                    name="admissionDate"
                    className="form-control"
                    value={formData.admissionDate}
                    onChange={handleChange}
                    required
                  />
                  <input
                    type="time"
                    name="admissionTime"
                    className="form-control"
                    value={formData.admissionTime}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Ward (Optional Allocation)</label>
                <select
                  name="wardId"
                  className="form-select"
                  value={formData.wardId}
                  onChange={handleChange}
                >
                  <option value="">Select Ward (Or allocate later)</option>
                  {wards.map(w => (
                    <option key={w.id} value={w.id}>{w.wardName} ({w.wardCode})</option>
                  ))}
                </select>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Available Bed (Optional Allocation)</label>
                <select
                  name="bedId"
                  className="form-select"
                  value={formData.bedId}
                  onChange={handleChange}
                  disabled={!formData.wardId}
                >
                  <option value="">Select Bed</option>
                  {availableBeds.map(b => (
                    <option key={b.id} value={b.id}>{b.bedCode} - Bed {b.bedNumber} ({b.bedType})</option>
                  ))}
                </select>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Reason for Admission *</label>
                <textarea
                  name="reasonForAdmission"
                  className="form-control"
                  rows="3"
                  placeholder="Primary clinical chief complaint / diagnosis requiring inpatient admission..."
                  value={formData.reasonForAdmission}
                  onChange={handleChange}
                  required
                ></textarea>
              </div>

              <div className="col-12">
                <label className="form-label fw-semibold">Clinical Summary</label>
                <textarea
                  name="clinicalSummary"
                  className="form-control"
                  rows="3"
                  placeholder="Initial clinical history, examination findings, and initial orders..."
                  value={formData.clinicalSummary}
                  onChange={handleChange}
                ></textarea>
              </div>

              <div className="col-md-6">
                <label className="form-label fw-semibold">Expected Discharge Date</label>
                <input
                  type="date"
                  name="expectedDischargeDate"
                  className="form-control"
                  value={formData.expectedDischargeDate}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="mt-4 pt-3 border-top text-end">
              <Link to="/ipd/admissions" className="btn btn-light me-2">Cancel</Link>
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Submitting...' : 'Submit Admission Request'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AdmissionFormPage;
