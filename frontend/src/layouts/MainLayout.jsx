import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import Sidebar from '../components/Sidebar';

const MainLayout = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="layout-wrapper">
      <Navbar onToggleSidebar={toggleSidebar} />
      <div className="d-flex" style={{ paddingTop: '56px' }}>
        <Sidebar isOpen={sidebarOpen} />
        <main className="main-content flex-grow-1 p-4 bg-light min-vh-100">
          {children}
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
