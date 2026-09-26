import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { doctorAvailabilityService } from '../../services/doctorAvailabilityService';
import { doctorService } from '../../services/doctorService';
import LoadingSpinner from '../../components/LoadingSpinner';
import ErrorMessage from '../../components/ErrorMessage';

const DoctorAvailabilityPage = () => {
  const { doctorId } = useParams();
  const navigate = useNavigate();

  const [doctor, setDoctor] = useState(null);
  const [availabilities, setAvailabilities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Form State for creating/editing
  const [formData, setFormData] = useState({
    dayOfWeek: 'MONDAY',
    startTime: '09:00',
    endTime: '13:00',
    slotDurationMinutes: 15,
    isActive: true
  });
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState(null);

  useEffect(() => {
    fetchData();
  }, [doctorId]);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [docRes, availRes] = await Promise.all([
        doctorService.getDoctorById(doctorId),
        doctorAvailabilityService.getDoctorAvailability(doctorId)
      ]);
      if (docRes.success) setDoctor(docRes.data);
      if (availRes.success) setAvailabilities(availRes.data || []);
    } catch (e) {
      setError(e.message || 'Failed to fetch availability schedule');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      const payload = {
        dayOfWeek: formData.dayOfWeek,
        startTime: formData.startTime.length === 5 ? formData.startTime + ':00' : formData.startTime,
        endTime: formData.endTime.length === 5 ? formData.endTime + ':00' : formData.endTime,
        slotDurationMinutes: Number(formData.slotDurationMinutes),
        isActive: formData.isActive
      };
      const res = await doctorAvailabilityService.createAvailability(doctorId, payload);
      if (res.success) {
        fetchData();
        setFormData({
          dayOfWeek: 'MONDAY',
          startTime: '09:00',
          endTime: '13:00',
          slotDurationMinutes: 15,
          isActive: true
        });
      } else {
        setFormError(res.message);
      }
    } catch (err) {
      setFormError(err.message || 'Failed to add availability schedule');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (availabilityId) => {
    if (!window.confirm('Are you sure you want to remove this schedule slot?')) return;
    try {
      const res = await doctorAvailabilityService.deleteAvailability(doctorId, availabilityId);
      if (res.success) fetchData();
    } catch (e) {
      alert(e.message || 'Failed to delete schedule');
    }
  };

  if (loading) return <div className="py-5"><LoadingSpinner /></div>;
  if (error) return <div className="container py-4"><ErrorMessage message={error} /></div>;

  return (
    <div className="container py-3" style={{ maxWidth: '900px' }}>
      <div className="d-flex align-items-center mb-4">
        <button className="btn btn-outline-secondary me-3" onClick={() => navigate(`/doctors/${doctorId}`)}>
          <i className="bi bi-arrow-left me-1"></i> Back to Doctor
        </button>
        <div>
          <h2 className="h3 mb-0 text-dark fw-bold">Doctor Availability Schedule</h2>
          <span className="text-muted small">
            Dr. {doctor?.firstName} {doctor?.lastName} ({doctor?.specialization})
          </span>
        </div>
      </div>

      <div className="row g-4">
        {/* Add Availability Form */}
        <div className="col-md-5">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-primary">
              <i className="bi bi-calendar-plus me-2"></i> Add Schedule Slot
            </div>
            <div className="card-body">
              {formError && <div className="alert alert-danger small py-2">{formError}</div>}
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label required">Day of Week</label>
                  <select
                    className="form-select"
                    value={formData.dayOfWeek}
                    onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
                    required
                  >
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                    <option value="SATURDAY">Saturday</option>
                    <option value="SUNDAY">Sunday</option>
                  </select>
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-6">
                    <label className="form-label required">Start Time</label>
                    <input
                      type="time"
                      className="form-control"
                      value={formData.startTime}
                      onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                      required
                    />
                  </div>
                  <div className="col-6">
                    <label className="form-label required">End Time</label>
                    <input
                      type="time"
                      className="form-control"
                      value={formData.endTime}
                      onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                      required
                    />
                  </div>
                </div>

                <div className="mb-3">
                  <label className="form-label required">Slot Duration (Minutes)</label>
                  <select
                    className="form-select"
                    value={formData.slotDurationMinutes}
                    onChange={(e) => setFormData({ ...formData, slotDurationMinutes: e.target.value })}
                    required
                  >
                    <option value="10">10 Minutes</option>
                    <option value="15">15 Minutes</option>
                    <option value="20">20 Minutes</option>
                    <option value="30">30 Minutes</option>
                    <option value="45">45 Minutes</option>
                    <option value="60">60 Minutes</option>
                  </select>
                </div>

                <button type="submit" className="btn btn-primary w-100" disabled={submitting}>
                  {submitting ? 'Saving...' : 'Add Schedule'}
                </button>
              </form>
            </div>
          </div>
        </div>

        {/* Schedule List */}
        <div className="col-md-7">
          <div className="card border-0 shadow-sm">
            <div className="card-header bg-white py-3 border-0 fw-semibold text-dark">
              Active Weekly Schedule
            </div>
            <div className="card-body p-0">
              {availabilities.length === 0 ? (
                <div className="text-center py-4 text-muted">
                  No working schedule configured for this doctor yet.
                </div>
              ) : (
                <div className="table-responsive">
                  <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                      <tr>
                        <th>Day</th>
                        <th>Working Hours</th>
                        <th>Slot Size</th>
                        <th className="text-end">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {availabilities.map((avail) => (
                        <tr key={avail.id}>
                          <td className="fw-semibold">{avail.dayOfWeek}</td>
                          <td>{avail.startTime} - {avail.endTime}</td>
                          <td>{avail.slotDurationMinutes} mins</td>
                          <td className="text-end">
                            <button
                              className="btn btn-sm btn-outline-danger"
                              onClick={() => handleDelete(avail.id)}
                            >
                              <i className="bi bi-trash"></i>
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DoctorAvailabilityPage;
