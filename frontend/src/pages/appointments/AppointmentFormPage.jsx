import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { appointmentService } from '../../services/appointmentService';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';
import { patientService } from '../../services/patientService';
import { doctorAvailabilityService } from '../../services/doctorAvailabilityService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';

const AppointmentFormPage = () => {
  const navigate = useNavigate();

  const [patients, setPatients] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [availableSlots, setAvailableSlots] = useState([]);

  const [loading, setLoading] = useState(false);
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [error, setError] = useState(null);

  const [formData, setFormData] = useState({
    patientId: '',
    departmentId: '',
    doctorId: '',
    appointmentDate: new Date().toISOString().split('T')[0],
    startTime: '',
    endTime: '',
    appointmentType: 'NEW_CONSULTATION',
    reasonForVisit: '',
    notes: ''
  });

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (formData.departmentId) {
      loadDoctorsByDepartment(formData.departmentId);
    } else {
      setDoctors([]);
    }
  }, [formData.departmentId]);

  useEffect(() => {
    if (formData.doctorId && formData.appointmentDate) {
      fetchSlots(formData.doctorId, formData.appointmentDate);
    } else {
      setAvailableSlots([]);
    }
  }, [formData.doctorId, formData.appointmentDate]);

  const loadInitialData = async () => {
    try {
      const [patRes, deptRes] = await Promise.all([
        patientService.searchPatients({ size: 100, isActive: true }),
        departmentService.getActiveDepartments()
      ]);
      if (patRes.success) setPatients(patRes.data.content || []);
      if (deptRes.success) setDepartments(deptRes.data || []);
    } catch (e) {
      setError('Failed to load form master data');
    }
  };

  const loadDoctorsByDepartment = async (deptId) => {
    try {
      const res = await doctorService.searchDoctors({ departmentId: deptId, isActive: true });
      if (res.success) setDoctors(res.data.content || []);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchSlots = async (docId, dateStr) => {
    setLoadingSlots(true);
    setAvailableSlots([]);
    try {
      const res = await doctorAvailabilityService.getAvailableSlots(docId, dateStr);
      if (res.success) {
        setAvailableSlots(res.data || []);
      }
    } catch (e) {
      console.error('Failed to load slots', e);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleSlotSelect = (slot) => {
    setFormData({
      ...formData,
      startTime: slot.startTime,
      endTime: slot.endTime
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.patientId || !formData.doctorId || !formData.departmentId || !formData.appointmentDate || !formData.startTime || !formData.endTime) {
      setError('Please fill in all mandatory fields and select a time slot.');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const payload = {
        patientId: Number(formData.patientId),
        doctorId: Number(formData.doctorId),
        departmentId: Number(formData.departmentId),
        appointmentDate: formData.appointmentDate,
        startTime: formData.startTime.length === 5 ? formData.startTime + ':00' : formData.startTime,
        endTime: formData.endTime.length === 5 ? formData.endTime + ':00' : formData.endTime,
        appointmentType: formData.appointmentType,
        reasonForVisit: formData.reasonForVisit,
        notes: formData.notes
      };

      const res = await appointmentService.createAppointment(payload);
      if (res.success) {
        navigate(`/appointments/${res.data.id}`);
      } else {
        setError(res.message);
      }
    } catch (err) {
      setError(err.message || 'Failed to create appointment');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container py-3" style={{ maxWidth: '850px' }}>
      <div className="d-flex align-items-center mb-4">
        <button className="btn btn-outline-secondary me-3" onClick={() => navigate('/appointments')}>
          <i className="bi bi-arrow-left me-1"></i> Back
        </button>
        <h2 className="h3 mb-0 text-dark fw-bold">Book New Appointment</h2>
      </div>

      {error && <ErrorMessage message={error} />}

      <div className="card border-0 shadow-sm">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            {/* Step 1: Patient Selection */}
            <div className="mb-4">
              <h5 className="border-bottom pb-2 text-primary">1. Select Patient</h5>
              <div className="row g-3">
                <div className="col-md-12">
                  <label className="form-label required">Patient *</label>
                  <select
                    className="form-select"
                    value={formData.patientId}
                    onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
                    required
                  >
                    <option value="">Select Patient...</option>
                    {patients.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.firstName} {p.lastName} ({p.patientId}) - Phone: {p.phone}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Step 2: Department & Doctor */}
            <div className="mb-4">
              <h5 className="border-bottom pb-2 text-primary">2. Select Department & Doctor</h5>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label required">Department *</label>
                  <select
                    className="form-select"
                    value={formData.departmentId}
                    onChange={(e) => setFormData({ ...formData, departmentId: e.target.value, doctorId: '', startTime: '', endTime: '' })}
                    required
                  >
                    <option value="">Select Department...</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>{d.departmentName}</option>
                    ))}
                  </select>
                </div>

                <div className="col-md-6">
                  <label className="form-label required">Doctor *</label>
                  <select
                    className="form-select"
                    value={formData.doctorId}
                    onChange={(e) => setFormData({ ...formData, doctorId: e.target.value, startTime: '', endTime: '' })}
                    disabled={!formData.departmentId}
                    required
                  >
                    <option value="">Select Doctor...</option>
                    {doctors.map((doc) => (
                      <option key={doc.id} value={doc.id}>
                        Dr. {doc.firstName} {doc.lastName} ({doc.specialization})
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Step 3: Date & Time Slot */}
            <div className="mb-4">
              <h5 className="border-bottom pb-2 text-primary">3. Date & Available Time Slot</h5>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label required">Appointment Date *</label>
                  <input
                    type="date"
                    className="form-control"
                    min={new Date().toISOString().split('T')[0]}
                    value={formData.appointmentDate}
                    onChange={(e) => setFormData({ ...formData, appointmentDate: e.target.value, startTime: '', endTime: '' })}
                    required
                  />
                </div>

                <div className="col-md-12">
                  <label className="form-label">Available Slots</label>
                  {loadingSlots ? (
                    <div className="py-2"><LoadingSpinner /></div>
                  ) : !formData.doctorId ? (
                    <div className="text-muted small">Please select a doctor to view working hours and slots.</div>
                  ) : availableSlots.length === 0 ? (
                    <div className="alert alert-warning py-2 mb-0 small">
                      No available schedule or slots for the selected doctor on this date.
                    </div>
                  ) : (
                    <div className="d-flex flex-wrap gap-2 mt-1">
                      {availableSlots.map((slot, index) => {
                        const isSelected = formData.startTime === slot.startTime;
                        return (
                          <button
                            type="button"
                            key={index}
                            disabled={!slot.isAvailable}
                            className={`btn btn-sm ${isSelected ? 'btn-primary' : slot.isAvailable ? 'btn-outline-primary' : 'btn-outline-secondary opacity-50'}`}
                            onClick={() => handleSlotSelect(slot)}
                          >
                            {slot.startTime} - {slot.endTime}
                            {!slot.isAvailable && ' (Booked)'}
                          </button>
                        );
                      })}
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Step 4: Appointment Details */}
            <div className="mb-4">
              <h5 className="border-bottom pb-2 text-primary">4. Appointment Details</h5>
              <div className="row g-3">
                <div className="col-md-6">
                  <label className="form-label required">Appointment Type *</label>
                  <select
                    className="form-select"
                    value={formData.appointmentType}
                    onChange={(e) => setFormData({ ...formData, appointmentType: e.target.value })}
                    required
                  >
                    <option value="NEW_CONSULTATION">New Consultation</option>
                    <option value="FOLLOW_UP">Follow Up</option>
                    <option value="EMERGENCY">Emergency</option>
                    <option value="ROUTINE_CHECKUP">Routine Checkup</option>
                    <option value="TELECONSULTATION">Teleconsultation</option>
                  </select>
                </div>

                <div className="col-md-12">
                  <label className="form-label">Reason for Visit</label>
                  <input
                    type="text"
                    className="form-control"
                    placeholder="Brief description of symptoms or consultation reason"
                    value={formData.reasonForVisit}
                    onChange={(e) => setFormData({ ...formData, reasonForVisit: e.target.value })}
                  />
                </div>

                <div className="col-md-12">
                  <label className="form-label">Internal Notes</label>
                  <textarea
                    className="form-control"
                    rows="2"
                    placeholder="Additional clinical or administrative notes"
                    value={formData.notes}
                    onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  ></textarea>
                </div>
              </div>
            </div>

            <div className="d-flex justify-content-end gap-2">
              <button type="button" className="btn btn-light" onClick={() => navigate('/appointments')}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary px-4" disabled={loading}>
                {loading ? 'Booking...' : 'Confirm Appointment'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AppointmentFormPage;
