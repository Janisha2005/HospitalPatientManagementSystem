import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import FormInput from '../../components/FormInput';
import SelectInput from '../../components/SelectInput';
import DateInput from '../../components/DateInput';
import AddressInputGroup from '../../components/AddressInputGroup';
import ErrorMessage from '../../components/ErrorMessage';
import LoadingSpinner from '../../components/LoadingSpinner';
import { patientService } from '../../services/patientService';

const PatientFormPage = () => {
  const { id } = useParams();
  const isEditMode = !!id;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    gender: 'Male',
    bloodGroup: 'A+',
    phone: '',
    email: '',
    addressLine1: '',
    addressLine2: '',
    city: '',
    district: '',
    state: 'Tamil Nadu',
    pincode: '',
    country: 'India',
    emergencyContactName: '',
    emergencyContactPhone: '',
    emergencyContactRelationship: 'Spouse',
    isActive: true
  });

  const genderOptions = [
    { label: 'Male', value: 'Male' },
    { label: 'Female', value: 'Female' },
    { label: 'Other', value: 'Other' }
  ];

  const bloodGroupOptions = [
    { label: 'A+', value: 'A+' },
    { label: 'A-', value: 'A-' },
    { label: 'B+', value: 'B+' },
    { label: 'B-', value: 'B-' },
    { label: 'AB+', value: 'AB+' },
    { label: 'AB-', value: 'AB-' },
    { label: 'O+', value: 'O+' },
    { label: 'O-', value: 'O-' }
  ];

  const relationshipOptions = [
    { label: 'Spouse', value: 'Spouse' },
    { label: 'Parent', value: 'Parent' },
    { label: 'Child', value: 'Child' },
    { label: 'Sibling', value: 'Sibling' },
    { label: 'Relative', value: 'Relative' },
    { label: 'Friend', value: 'Friend' },
    { label: 'Other', value: 'Other' }
  ];

  useEffect(() => {
    if (isEditMode) {
      const fetchPatient = async () => {
        setLoading(true);
        try {
          const response = await patientService.getPatientById(id);
          if (response.success && response.data) {
            setFormData(response.data);
          }
        } catch (err) {
          setError(err.message || 'Failed to fetch patient details');
        } finally {
          setLoading(false);
        }
      };
      fetchPatient();
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
        const response = await patientService.updatePatient(id, formData);
        if (response.success) {
          navigate(`/patients/${id}`);
        }
      } else {
        const response = await patientService.createPatient(formData);
        if (response.success) {
          navigate('/patients');
        }
      }
    } catch (err) {
      if (err.errors && typeof err.errors === 'object') {
        setFieldErrors(err.errors);
      } else {
        setError(err.message || 'Validation error saving patient');
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner message="Loading patient details..." />;

  return (
    <div style={{ maxWidth: '850px' }} className="mx-auto">
      <div className="d-flex align-items-center mb-4">
        <button className="btn btn-outline-secondary btn-sm me-3" onClick={() => navigate(-1)}>
          <i className="bi bi-arrow-left me-1"></i> Back
        </button>
        <div>
          <h3 className="fw-bold mb-0">{isEditMode ? 'Edit Patient Profile' : 'Register Patient (India)'}</h3>
          <p className="text-muted small mb-0">Demographics, Indian address, and emergency contacts</p>
        </div>
      </div>

      <ErrorMessage message={error} onClose={() => setError('')} />

      <div className="card border-0 shadow-sm rounded-3 mb-4">
        <div className="card-body p-4">
          <form onSubmit={handleSubmit}>
            <h6 className="text-primary fw-bold mb-3 border-bottom pb-2">
              <i className="bi bi-person me-2"></i>Personal & Demographic Information
            </h6>

            <div className="row g-3 mb-4">
              <div className="col-md-6">
                <FormInput
                  label="First Name"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  error={fieldErrors.firstName}
                  placeholder="e.g. Arun"
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
                  placeholder="e.g. Kumar"
                  required
                />
              </div>

              <div className="col-md-4">
                <DateInput
                  label="Date of Birth"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  error={fieldErrors.dateOfBirth}
                  max={new Date().toISOString().split('T')[0]}
                  required
                />
              </div>
              <div className="col-md-4">
                <SelectInput
                  label="Gender"
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  options={genderOptions}
                  error={fieldErrors.gender}
                  required
                />
              </div>
              <div className="col-md-4">
                <SelectInput
                  label="Blood Group"
                  name="bloodGroup"
                  value={formData.bloodGroup}
                  onChange={handleChange}
                  options={bloodGroupOptions}
                  error={fieldErrors.bloodGroup}
                />
              </div>

              <div className="col-md-6">
                <FormInput
                  label="Mobile Number (India)"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  error={fieldErrors.phone}
                  placeholder="e.g. 9876543210 or +919876543210"
                  required
                />
              </div>
              <div className="col-md-6">
                <FormInput
                  label="Email Address"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  error={fieldErrors.email}
                  placeholder="e.g. arun.kumar@example.com"
                />
              </div>
            </div>

            <AddressInputGroup
              values={formData}
              onChange={handleChange}
              fieldErrors={fieldErrors}
            />

            <h6 className="text-primary fw-bold mt-4 mb-3 border-bottom pb-2">
              <i className="bi bi-telephone-plus me-2"></i>Emergency Contact (India)
            </h6>

            <div className="row g-3">
              <div className="col-md-5">
                <FormInput
                  label="Emergency Contact Person"
                  name="emergencyContactName"
                  value={formData.emergencyContactName}
                  onChange={handleChange}
                  error={fieldErrors.emergencyContactName}
                  placeholder="e.g. Priya Sharma"
                  required
                />
              </div>
              <div className="col-md-4">
                <FormInput
                  label="Contact Phone Number"
                  name="emergencyContactPhone"
                  value={formData.emergencyContactPhone}
                  onChange={handleChange}
                  error={fieldErrors.emergencyContactPhone}
                  placeholder="e.g. 9876543219"
                  required
                />
              </div>
              <div className="col-md-3">
                <SelectInput
                  label="Relationship"
                  name="emergencyContactRelationship"
                  value={formData.emergencyContactRelationship}
                  onChange={handleChange}
                  options={relationshipOptions}
                  error={fieldErrors.emergencyContactRelationship}
                  required
                />
              </div>
            </div>

            <div className="d-flex justify-content-end gap-2 mt-4 pt-3 border-top">
              <button type="button" className="btn btn-secondary" onClick={() => navigate('/patients')}>
                Cancel
              </button>
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? (
                  <>
                    <span className="spinner-border spinner-border-sm me-2"></span>Saving...
                  </>
                ) : (
                  isEditMode ? 'Update Patient' : 'Register Patient'
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default PatientFormPage;
