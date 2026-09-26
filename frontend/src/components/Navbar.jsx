import React from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';

const Navbar = ({ onToggleSidebar }) => {
  const navigate = useNavigate();
  const user = authService.getStoredUser();

  const handleLogout = async () => {
    await authService.logout();
    navigate('/login');
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm fixed-top">
      <div className="container-fluid px-3">
        <button
          className="btn btn-link text-white me-2 d-lg-none p-0 border-0"
          onClick={onToggleSidebar}
          aria-label="Toggle Navigation"
        >
          <i className="bi bi-list fs-3"></i>
        </button>

        <a className="navbar-brand d-flex align-items-center fw-bold" href="/dashboard">
          <i className="bi bi-hospital fs-4 me-2"></i>
          <span>PulseCare HMS</span>
        </a>

        <div className="d-flex align-items-center ms-auto">
          {user && (
            <div className="dropdown">
              <button
                className="btn btn-primary dropdown-toggle d-flex align-items-center border-0 shadow-none"
                type="button"
                id="userMenu"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <div className="rounded-circle bg-white text-primary fw-bold me-2 d-flex align-items-center justify-content-center" style={{ width: '32px', height: '32px' }}>
                  {user.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
                </div>
                <div className="text-start me-2 d-none d-sm-block">
                  <div className="fw-semibold lh-1 small">{user.fullName}</div>
                  <span className="badge bg-light text-dark font-monospace" style={{ fontSize: '0.65rem' }}>
                    {user.role}
                  </span>
                </div>
              </button>
              <ul className="dropdown-menu dropdown-menu-end shadow-sm" aria-labelledby="userMenu">
                <li>
                  <div className="dropdown-header">
                    Logged in as <strong>{user.username}</strong>
                  </div>
                </li>
                <li><hr className="dropdown-divider" /></li>
                <li>
                  <button className="dropdown-item text-danger" onClick={handleLogout}>
                    <i className="bi bi-box-arrow-right me-2"></i>Logout
                  </button>
                </li>
              </ul>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
