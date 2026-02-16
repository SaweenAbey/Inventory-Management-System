import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { purchaseAPI } from '../api';

export default function MyOrders() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      navigate('/login');
      return;
    }
    const userData = JSON.parse(storedUser);
    if (userData.role !== 'BUYER') {
      toast.error('Access denied. Buyers only.');
      navigate('/');
      return;
    }
    setUser(userData);
    fetchOrders();
  }, [navigate]);

  const fetchOrders = async () => {
    try {
      const response = await purchaseAPI.getMyOrders();
      setOrders(response.data);
    } catch (error) {
      toast.error('Failed to fetch orders');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async (orderId) => {
    if (window.confirm('Are you sure you want to cancel this order?')) {
      try {
        await purchaseAPI.cancelOrder(orderId);
        toast.success('Order cancelled successfully');
        fetchOrders();
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Failed to cancel order';
        toast.error(errorMsg);
      }
    }
  };

  const handleConfirmOrder = async (orderId) => {
    try {
      await purchaseAPI.confirmOrder(orderId);
      toast.success('Order confirmed');
      fetchOrders();
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Failed to confirm order';
      toast.error(errorMsg);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return '#f39c12';
      case 'CONFIRMED':
        return '#3498db';
      case 'SHIPPED':
        return '#9b59b6';
      case 'DELIVERED':
        return '#27ae60';
      case 'CANCELLED':
        return '#e74c3c';
      default:
        return '#95a5a6';
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
          <h2 style={styles.logo}>📋 My Orders</h2>
          <div style={styles.navButtons}>
            <button
              style={styles.marketplaceBtn}
              onClick={() => navigate('/buyer/marketplace')}
            >
              🛍️ Back to Marketplace
            </button>
            <div style={styles.userSection}>
              <span style={styles.username}>{user?.username}</span>
              <button style={styles.logoutBtn} onClick={handleLogout}>
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>

      <div style={styles.content}>
        <h1>Your Orders</h1>

        {loading ? (
          <p>Loading orders...</p>
        ) : orders.length === 0 ? (
          <div style={styles.emptyState}>
            <p>You haven't placed any orders yet.</p>
            <button
              style={styles.primaryBtn}
              onClick={() => navigate('/buyer/marketplace')}
            >
              Start Shopping
            </button>
          </div>
        ) : (
          <div style={styles.ordersList}>
            {orders.map((order) => (
              <div key={order.orderId} style={styles.orderCard}>
                <div style={styles.orderHeader}>
                  <div>
                    <h3 style={styles.orderTitle}>Order #{order.orderId}</h3>
                    <p style={styles.orderDate}>
                      {new Date(order.orderDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div
                    style={{
                      ...styles.statusBadge,
                      backgroundColor: getStatusColor(order.orderStatus)
                    }}
                  >
                    {order.orderStatus}
                  </div>
                </div>

                <div style={styles.orderDetails}>
                  <div style={styles.detailRow}>
                    <span style={styles.label}>Item:</span>
                    <span style={styles.value}>{order.itemName}</span>
                  </div>
                  <div style={styles.detailRow}>
                    <span style={styles.label}>Quantity:</span>
                    <span style={styles.value}>{order.quantity}</span>
                  </div>
                  <div style={styles.detailRow}>
                    <span style={styles.label}>Unit Price:</span>
                    <span style={styles.value}>Rs. {order.unitPrice}</span>
                  </div>
                  <div style={styles.detailRow}>
                    <span style={styles.label}>Total Amount:</span>
                    <span style={styles.totalPrice}>Rs. {order.totalAmount}</span>
                  </div>
                </div>

                <div style={styles.actions}>
                  {order.orderStatus === 'PENDING' && (
                    <>
                      <button
                        style={styles.confirmBtn}
                        onClick={() => handleConfirmOrder(order.orderId)}
                      >
                        ✓ Confirm
                      </button>
                      <button
                        style={styles.cancelBtn}
                        onClick={() => handleCancelOrder(order.orderId)}
                      >
                        ✗ Cancel
                      </button>
                    </>
                  )}
                  {(order.orderStatus === 'CONFIRMED' || order.orderStatus === 'SHIPPED') && (
                    <button
                      style={styles.cancelBtn}
                      onClick={() => handleCancelOrder(order.orderId)}
                    >
                      ✗ Cancel Order
                    </button>
                  )}
                  {order.orderStatus !== 'CANCELLED' && (
                    <button style={styles.viewBtn}>View Details</button>
                  )}
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
  navButtons: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
  },
  marketplaceBtn: {
    padding: '8px 16px',
    backgroundColor: '#27ae60',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '600',
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
  ordersList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  orderCard: {
    backgroundColor: 'white',
    padding: '25px',
    borderRadius: '12px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  orderHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    marginBottom: '20px',
    paddingBottom: '15px',
    borderBottom: '1px solid #eee',
  },
  orderTitle: {
    margin: 0,
    fontSize: '18px',
    fontWeight: '600',
    color: '#2c3e50',
  },
  orderDate: {
    margin: '5px 0 0 0',
    fontSize: '12px',
    color: '#999',
  },
  statusBadge: {
    padding: '8px 16px',
    borderRadius: '20px',
    color: 'white',
    fontWeight: '600',
    fontSize: '12px',
  },
  orderDetails: {
    backgroundColor: '#f5f7fa',
    padding: '15px',
    borderRadius: '8px',
    marginBottom: '20px',
  },
  detailRow: {
    display: 'flex',
    justifyContent: 'space-between',
    marginBottom: '10px',
    fontSize: '14px',
  },
  label: {
    fontWeight: '600',
    color: '#2c3e50',
    minWidth: '100px',
  },
  value: {
    color: '#666',
  },
  totalPrice: {
    fontWeight: '600',
    color: '#667eea',
    fontSize: '16px',
  },
  actions: {
    display: 'flex',
    gap: '10px',
    flexWrap: 'wrap',
  },
  confirmBtn: {
    padding: '10px 16px',
    backgroundColor: '#27ae60',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '600',
    flex: 1,
    minWidth: '100px',
  },
  cancelBtn: {
    padding: '10px 16px',
    backgroundColor: '#e74c3c',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '600',
    flex: 1,
    minWidth: '100px',
  },
  viewBtn: {
    padding: '10px 16px',
    backgroundColor: '#3498db',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontSize: '14px',
    fontWeight: '600',
    flex: 1,
    minWidth: '100px',
  },
};
