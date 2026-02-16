import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

export default function Home() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      navigate('/login');
    } else {
      setUser(JSON.parse(storedUser));
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    toast.success('Logged out successfully');
    navigate('/login');
  };

  return (
    <div style={styles.container}>
      <div style={styles.navbar}>
        <div style={styles.navContent}>
          <h2 style={styles.logo}>📦 Inventory System</h2>
          <div style={styles.userSection}>
            <span style={styles.username}>Hello, {user?.username || 'User'}!</span>
            <button style={styles.logoutBtn} onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </div>

      <div style={styles.content}>
        <div style={styles.card}>
          <h1>Welcome to Inventory Management System</h1>
          <p style={styles.subtitle}>Your role: <strong>{user?.role}</strong></p>

          {user?.role === 'ADMIN' && (
            <div>
              <p style={styles.description}>
                As an Admin, you can manage all inventory items in the system.
              </p>
              <button
                style={styles.primaryBtn}
                onClick={() => navigate('/admin/dashboard')}
              >
                Go to Admin Dashboard
              </button>
            </div>
          )}

          {user?.role === 'BUYER' && (
            <div>
              <p style={styles.description}>
                As a Buyer, you can browse and purchase items from our inventory.
              </p>
              <button
                style={styles.primaryBtn}
                onClick={() => navigate('/buyer/marketplace')}
              >
                Go to Marketplace
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f5f7fa',
  },
  navbar: {
    backgroundColor: '#2c3e50',
    color: 'white',
    padding: '15px 0',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
  },
  navContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingX: '20px',
  },
  logo: {
    margin: 0,
    fontSize: '24px',
    fontWeight: '700',
  },
  userSection: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
  },
  username: {
    fontSize: '14px',
  },
  logoutBtn: {
    padding: '8px 16px',
    backgroundColor: '#e74c3c',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '600',
    transition: 'background 0.3s',
  },
  content: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '40px 20px',
  },
  card: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: '18px',
    color: '#666',
    marginTop: '10px',
  },
  description: {
    fontSize: '16px',
    color: '#777',
    marginBottom: '30px',
    lineHeight: '1.6',
  },
  primaryBtn: {
    padding: '12px 30px',
    backgroundColor: '#667eea',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '16px',
    fontWeight: '600',
    transition: 'background 0.3s',
  },
};
