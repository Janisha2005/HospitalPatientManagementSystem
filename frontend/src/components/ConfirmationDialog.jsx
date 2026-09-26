import React from 'react';
import Modal from './Modal';

const ConfirmationDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirm Action',
  message = 'Are you sure you want to proceed?',
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  confirmVariant = 'danger'
}) => {
  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      footer={
        <>
          <button type="button" className="btn btn-sm btn-secondary" onClick={onClose}>
            {cancelText}
          </button>
          <button type="button" className={`btn btn-sm btn-${confirmVariant}`} onClick={onConfirm}>
            {confirmText}
          </button>
        </>
      }
    >
      <p className="mb-0 text-secondary">{message}</p>
    </Modal>
  );
};

export default ConfirmationDialog;
