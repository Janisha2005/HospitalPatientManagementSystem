import React from 'react';
import FormInput from './FormInput';
import SelectInput from './SelectInput';
import { INDIAN_STATES_AND_UTS } from '../utils/indiaUtils';

const AddressInputGroup = ({ values, onChange, fieldErrors = {} }) => {
  const stateOptions = INDIAN_STATES_AND_UTS.map((stateName) => ({
    label: stateName,
    value: stateName
  }));

  return (
    <div>
      <h6 className="text-primary fw-bold mb-3 border-bottom pb-2">
        <i className="bi bi-geo-alt me-2"></i>Address Details (India)
      </h6>
      <div className="row g-3">
        <div className="col-md-6">
          <FormInput
            label="Address Line 1"
            name="addressLine1"
            value={values.addressLine1 || ''}
            onChange={onChange}
            error={fieldErrors.addressLine1}
            placeholder="Door No., Street Name, Area"
          />
        </div>
        <div className="col-md-6">
          <FormInput
            label="Address Line 2"
            name="addressLine2"
            value={values.addressLine2 || ''}
            onChange={onChange}
            error={fieldErrors.addressLine2}
            placeholder="Landmark / Locality"
          />
        </div>

        <div className="col-md-4">
          <FormInput
            label="City"
            name="city"
            value={values.city || ''}
            onChange={onChange}
            error={fieldErrors.city}
            placeholder="e.g. Chennai"
          />
        </div>
        <div className="col-md-4">
          <FormInput
            label="District"
            name="district"
            value={values.district || ''}
            onChange={onChange}
            error={fieldErrors.district}
            placeholder="e.g. Chennai"
          />
        </div>
        <div className="col-md-4">
          <SelectInput
            label="State / Union Territory"
            name="state"
            value={values.state || ''}
            onChange={onChange}
            options={stateOptions}
            error={fieldErrors.state}
          />
        </div>

        <div className="col-md-6">
          <FormInput
            label="PIN Code"
            name="pincode"
            value={values.pincode || ''}
            onChange={onChange}
            error={fieldErrors.pincode}
            placeholder="6-digit PIN code (e.g. 600040)"
          />
        </div>
        <div className="col-md-6">
          <FormInput
            label="Country"
            name="country"
            value={values.country || 'India'}
            onChange={onChange}
            disabled
          />
        </div>
      </div>
    </div>
  );
};

export default AddressInputGroup;
