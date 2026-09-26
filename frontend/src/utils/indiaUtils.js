export const INDIAN_STATES_AND_UTS = [
  // States
  'Andhra Pradesh',
  'Arunachal Pradesh',
  'Assam',
  'Bihar',
  'Chhattisgarh',
  'Goa',
  'Gujarat',
  'Haryana',
  'Himachal Pradesh',
  'Jharkhand',
  'Karnataka',
  'Kerala',
  'Madhya Pradesh',
  'Maharashtra',
  'Manipur',
  'Meghalaya',
  'Mizoram',
  'Nagaland',
  'Odisha',
  'Punjab',
  'Rajasthan',
  'Sikkim',
  'Tamil Nadu',
  'Telangana',
  'Tripura',
  'Uttar Pradesh',
  'Uttarakhand',
  'West Bengal',
  // Union Territories
  'Andaman and Nicobar Islands',
  'Chandigarh',
  'Dadra and Nagar Haveli and Daman and Diu',
  'Delhi',
  'Jammu and Kashmir',
  'Ladakh',
  'Lakshadweep',
  'Puducherry'
];

export const formatINR = (amount) => {
  if (amount == null || isNaN(amount)) return '₹0.00';
  const num = Number(amount);
  return num.toLocaleString('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
    minimumFractionDigits: 2
  });
};

export const formatIndianNumber = (number) => {
  if (number == null || isNaN(number)) return '0';
  return Number(number).toLocaleString('en-IN');
};

export const formatIndianDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return dateString;
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  return `${day}-${month}-${year}`;
};

export const isValidIndianPhone = (phone) => {
  if (!phone) return false;
  const clean = phone.replace(/[\s-]/g, '');
  if (clean === '+910000000000' || clean === '0000000000' || clean === '1234567890' || clean === '1111111111') {
    return false;
  }
  const regex = /^(?:\+91[\s-]?)?[6-9]\d{9}$/;
  return regex.test(phone.trim());
};

export const normalizeIndianPhone = (phone) => {
  if (!phone) return '';
  const digits = phone.replace(/[^0-9]/g, '');
  if (digits.length === 10) {
    return `+91${digits}`;
  } else if (digits.length === 12 && digits.startsWith('91')) {
    return `+${digits}`;
  }
  return phone.trim();
};

export const isValidPincode = (pincode) => {
  if (!pincode) return false;
  const regex = /^[1-9][0-9]{5}$/;
  return regex.test(pincode.trim());
};
