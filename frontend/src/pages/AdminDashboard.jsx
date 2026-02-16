import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { inventoryAPI } from '../api';

export default function AdminDashboard() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      navigate('/login');
      return;
    }
    const userData = JSON.parse(storedUser);
    if (userData.role !== 'ADMIN') {
      toast.error('Access denied. Admin only.');
      navigate('/');
      return;
    }
    setUser(userData);
    fetchItems();
  }, [navigate]);

  const fetchItems = async () => {
    try {
      const response = await inventoryAPI.getItems();
      setItems(response.data);
    } catch (error) {
      toast.error('Failed to fetch items');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    toast.success('Logged out successfully');
    navigate('/login');
  };

  if (!user) return <div>Loading...</div>;

  return (
    <div style={styles.container}>
      <div style={styles.navbar}>
        <div style={styles.navContent}>
          <h2 style={styles.logo}>📦 Admin Dashboard</h2>
          <div style={styles.userSection}>
            <span style={styles.username}>{user?.username}</span>
            <button style={styles.logoutBtn} onClick={handleLogout}>
              Logout
            </button>
          </div>
        </div>
      </div>

      <div style={styles.content}>
        <div style={styles.header}>
          <h1>Inventory Management</h1>
          <button
            style={styles.actionBtn}
            onClick={() => navigate('/admin/add-item')}
          >
            + Add New Item
          </button>
        </div>

        {loading ? (
          <p>Loading items...</p>
        ) : items.length === 0 ? (
          <div style={styles.emptyState}>
            <p>No items in inventory yet.</p>
            <button
              style={styles.primaryBtn}
              onClick={() => navigate('/admin/add-item')}
            >
              Add First Item
            </button>
          </div>
        ) : (
          <div style={styles.itemsGrid}>
            {items.map((item) => (
              <div key={item.itemId} style={styles.itemCard}>
                <h3>{item.itemName}</h3>
                <p><strong>SKU:</strong> {item.sku}</p>
                <p><strong>Category:</strong> {item.category}</p>
                <p><strong>Price:</strong> Rs. {item.price}</p>
                <p><strong>Quantity:</strong> {item.quantity}</p>
                <p><strong>Status:</strong> {item.isActive ? '✅ Active' : '❌ Inactive'}</p>
                <div style={styles.actions}>
                  <button style={styles.editBtn}>Edit</button>
                  <button style={styles.deleteBtn}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        )}
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
    padding: '0 20px',
  },
  logo: {
    margin: 0,
    fontSize: '24px',
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
  },
  content: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '40px 20px',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
  },
  actionBtn: {
    padding: '10px 20px',
    backgroundColor: '#667eea',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontWeight: '600',
  },
  emptyState: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    textAlign: 'center',
  },
  primaryBtn: {
    padding: '10px 20px',
    backgroundColor: '#667eea',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontWeight: '600',
    marginTop: '20px',
  },
  itemsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '20px',
  },
  itemCard: {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  actions: {
    display: 'flex',
    gap: '10px',
    marginTop: '15px',
  },
  editBtn: {
    flex: 1,
    padding: '8px',
    backgroundColor: '#3498db',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
  deleteBtn: {
    flex: 1,
    padding: '8px',
    backgroundColor: '#e74c3c',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontSize: '14px',
  },
};
