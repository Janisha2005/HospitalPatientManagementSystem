import React from 'react';

const Modal = ({ isOpen, onClose, title, children, footer }) => {
  if (!isOpen) return null;

  return (
    <>
      <div className="modal-backdrop fade show"></div>
      <div className="modal d-block fade show" tabIndex="-1" role="dialog">
        <div className="modal-dialog modal-dialog-centered modal-lg" role="document">
          <div className="modal-content shadow-lg border-0">
            <div className="modal-header bg-light">
              <h5 className="modal-header-title mb-0 h6 text-primary fw-bold">{title}</h5>
              <button type="button" className="btn-close" onClick={onClose} aria-label="Close"></button>
            </div>
            <div className="modal-body">{children}</div>
            {footer && <div className="modal-footer bg-light">{footer}</div>}
          </div>
        </div>
      </div>
    </>
  );
};

export default Modal;
