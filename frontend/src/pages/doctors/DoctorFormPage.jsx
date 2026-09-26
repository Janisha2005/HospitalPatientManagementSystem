import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import FormInput from '../../components/FormInput';
import SelectInput from '../../components/SelectInput';
import DateInput from '../../components/DateInput';
import AddressInputGroup from '../../components/AddressInputGroup';
import ErrorMessage from '../../components/ErrorMessage';
import LoadingSpinner from '../../components/LoadingSpinner';
import { doctorService } from '../../services/doctorService';
import { departmentService } from '../../services/departmentService';

const DoctorFormPage = () => {
  const { id } = useParams();
  const isEditMode = !!id;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [departments, setDepartments] = useState([]);

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    specialization: '',
    qualification: '',
    licenseNumber: '',
    departmentId: '',
    consultationFee: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    district: '',
    state: 'Tamil Nadu',
    pincode: '',
    country: 'India',
    joiningDate: new Date().toISOString().split('T')[0],
    isActive: true
  });

  useEffect(() => {
    const fetchDepartments = async () => {
      try {
        const response = await departmentService.getAllActiveDepartments();
        if (response.success && response.data) {
          setDepartments(response.data);
          if (!isEditMode && response.data.length > 0) {
            setFormData((prev) => ({ ...prev, departmentId: response.data[0].id }));
          }
        }
      } catch (err) {
        setError('Failed to load active departments');
      }
    };
    fetchDepartments();

    if (isEditMode) {
      const fetchDoctor = async () => {
        setLoading(true);
        try {
          const response = await doctorService.getDoctorById(id);
          if (response.success && response.data) {
            setFormData(response.data);
          }
        } catch (err) {
          setError(err.message || 'Failed to fetch doctor details');
        } finally {
          setLoading(false);
        }
      };
      fetchDoctor();
    }
  }, [id, isEditMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFieldErrors({});
    setError('');
    setSaving(true);

    try {
      if (isEditMode) {
        const response = await doctorService.updateDoctor(id, formData);
        if (response.success) {
          navigate(`/doctors/${id}`);
        }
      } else {
        const response = await doctorService.createDoctor(formData);
        if (response.success) {
          navigate('/doctors');
        }
      }
    } catch (err) {
      if (err.errors && typeof err.errors === 'object') {
        setFieldErrors(err.errors);
      } else {
        setError(err.message || 'Validation error saving doctor');
      }
    } finally {
      setSaving(false);
    }
  };

  const departmentOptions = departments.map((d) => ({
    label: `${d.departmentName} (${d.departmentCode})`,
    value: d.id
  }));

  if (loading) return <LoadingSpinner message="Loading doctor profile..." />;

  return (
    <div style={{ maxWidth: '850px' }} className="mx-auto">
      <div className="d-flex align-items-center mb-4">
        <button className="btn btn-outline-secondary btn-sm me-3" onClick={() => navigate(-1)}>
          <i className="bi bi-arrow-left me-1"></i> Back
        </button>
        <div>
          <h3 className="fw-bold mb-0">{isEditMode ? 'Edit Doctor Profile' : 'Register New Doctor (India)'}</h3>
          <p className="text-muted small mb-0">Medical credentials, state registration number, consultation fee, and address</p>
        </div>
      </div>

      <ErrorMessage message={error} onClose={() => setError('')} />

      <div className="card border-0 shadow-sm rounded-3 mb-4">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <h6 className="text-primary fw-bold mb-3 border-bottom pb-2">
              <i className="bi bi-person-badge me-2"></i>Doctor Identification & Qualifications
            </h6>

            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <FormInput
                  label="First Name"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  error={fieldErrors.firstName}
                  placeholder="e.g. Arjun"
                  required
                />
              </div>
              <div className="col-md-6">
                <FormInput
                  label="Last Name"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  error={fieldErrors.lastName}
                  placeholder="e.g. Krishnan"
                  required
                />
              </div>

              <div className="col-md-6">
                <FormInput
                  label="Specialization"
                  name="specialization"
                  value={formData.specialization}
                  onChange={handleChange}
                  error={fieldErrors.specialization}
                  placeholder="e.g. Cardiology, Orthopedics"
                  required
                />
              </div>
              <div className="col-md-6">
                <FormInput
                  label="Qualification"
                  name="qualification"
                  value={formData.qualification}
                  onChange={handleChange}
                  error={fieldErrors.qualification}
                  placeholder="e.g. MBBS, MD, DM"
                  required
                />
              </div>

              <div className="col-md-6">
                <FormInput
                  label="Medical Registration / License Number"
                  name="licenseNumber"
                  value={formData.licenseNumber}
                  onChange={handleChange}
                  error={fieldErrors.licenseNumber}
                  placeholder="e.g. MCI-TN-2015-8849"
                  required
                />
              </div>
              <div className="col-md-6">
                <SelectInput
                  label="Assigned Department"
                  name="departmentId"
                  value={formData.departmentId}
                  onChange={handleChange}
                  options={departmentOptions}
                  error={fieldErrors.departmentId}
                  required
                />
              </div>
            </div>

            <h6 className="text-primary fw-bold mb-3 border-bottom pb-2">
              <i className="bi bi-currency-rupee me-2"></i>Contact & Fee Schedule
            </h6>

            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <FormInput
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  error={fieldErrors.email}
                  placeholder="e.g. arjun.krishnan@pulse-hospital.in"
                  required
                />
              </div>
              <div className="col-md-6">
                <FormInput
                  label="Mobile Number (India)"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  error={fieldErrors.phone}
                  placeholder="e.g. 9876543211 or +919876543211"
                  required
                />
              </div>

              <div className="col-md-6">
                <FormInput
                  label="Consultation Fee (₹ INR)"
                  name="consultationFee"
                  type="number"
                  step="0.01"
                  value={formData.consultationFee}
                  onChange={handleChange}
                  error={fieldErrors.consultationFee}
                  placeholder="e.g. 1000.00"
                  required
                />
              </div>
              <div className="col-md-6">
                <DateInput
                  label="Joining Date"
                  name="joiningDate"
                  value={formData.joiningDate}
                  onChange={handleChange}
                  error={fieldErrors.joiningDate}
                  required
                />
              </div>
            </div>

            <AddressInputGroup
              values={formData}
              onChange={handleChange}
              fieldErrors={fieldErrors}
            />

            <div className="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/doctors')}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>Saving...
                  </>
                ) : (
                  isEditMode ? 'Update Doctor' : 'Register Doctor'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default DoctorFormPage;
