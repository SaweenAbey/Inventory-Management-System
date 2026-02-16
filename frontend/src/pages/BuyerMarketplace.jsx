import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { inventoryAPI, purchaseAPI } from '../api';

export default function BuyerMarketplace() {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [items, setItems] = useState([]);
  const [cartItems, setCartItems] = useState({});
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
    fetchItems();
  }, [navigate]);

  const fetchItems = async () => {
    try {
      const response = await inventoryAPI.getActiveItems();
      setItems(response.data);
    } catch (error) {
      toast.error('Failed to fetch items');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleQuantityChange = (itemId, quantity) => {
    const item = items.find(i => i.itemId === itemId);
    if (quantity > item.quantity) {
      toast.error(`Only ${item.quantity} items available`);
      return;
    }
    if (quantity > 0) {
      setCartItems(prev => ({
        ...prev,
        [itemId]: quantity
      }));
    } else {
      const newCart = { ...cartItems };
      delete newCart[itemId];
      setCartItems(newCart);
    }
  };

  const handleBuyItem = async (itemId) => {
    const quantity = cartItems[itemId];
    if (!quantity || quantity <= 0) {
      toast.error('Please select a quantity');
      return;
    }

    try {
      await purchaseAPI.buyItem({
        itemId,
        quantityToPurchase: quantity
      });
      toast.success(`Successfully purchased! Quantity: ${quantity}`);
      setCartItems(prev => {
        const newCart = { ...prev };
        delete newCart[itemId];
        return newCart;
      });
      fetchItems();
    } catch (error) {
      const errorMsg = error.response?.data?.message || 'Purchase failed';
      toast.error(errorMsg);
      console.error(error);
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
          <h2 style={styles.logo}>🛍️ Marketplace</h2>
          <div style={styles.navButtons}>
            <button
              style={styles.ordersBtn}
              onClick={() => navigate('/buyer/my-orders')}
            >
              📋 My Orders
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
        <h1>Available Items</h1>

        {loading ? (
          <p>Loading items...</p>
        ) : items.length === 0 ? (
          <div style={styles.emptyState}>
            <p>No items available at the moment.</p>
          </div>
        ) : (
          <div style={styles.itemsGrid}>
            {items.map((item) => (
              <div key={item.itemId} style={styles.itemCard}>
                <div style={styles.itemHeader}>
                  <h3>{item.itemName}</h3>
                  <span style={styles.categoryTag}>{item.category}</span>
                </div>
                <p style={styles.sku}><strong>SKU:</strong> {item.sku}</p>
                <p style={styles.description}>{item.description}</p>
                <div style={styles.priceStock}>
                  <div style={styles.price}>Rs. {item.price}</div>
                  <div style={styles.stock}>
                    {item.quantity > 0 ? (
                      <span style={styles.inStock}>✅ In Stock ({item.quantity})</span>
                    ) : (
                      <span style={styles.outOfStock}>❌ Out of Stock</span>
                    )}
                  </div>
                </div>

                {item.quantity > 0 && (
                  <div style={styles.buySection}>
                    <input
                      type="number"
                      min="1"
                      max={item.quantity}
                      value={cartItems[item.itemId] || 1}
                      onChange={(e) => handleQuantityChange(item.itemId, parseInt(e.target.value))}
                      style={styles.quantityInput}
                    />
                    <button
                      style={styles.buyBtn}
                      onClick={() => handleBuyItem(item.itemId)}
                    >
                      Buy
                    </button>
                  </div>
                )}
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
  ordersBtn: {
    padding: '8px 16px',
    backgroundColor: '#3498db',
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
  itemsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
    gap: '20px',
  },
  itemCard: {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '12px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    transition: 'transform 0.3s, box-shadow 0.3s',
  },
  itemHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'start',
    marginBottom: '10px',
  },
  categoryTag: {
    backgroundColor: '#667eea',
    color: 'white',
    padding: '4px 12px',
    borderRadius: '20px',
    fontSize: '12px',
    fontWeight: '600',
  },
  sku: {
    fontSize: '12px',
    color: '#999',
    margin: '5px 0',
  },
  description: {
    fontSize: '14px',
    color: '#666',
    margin: '10px 0',
    minHeight: '40px',
  },
  priceStock: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    margin: '15px 0',
    paddingBottom: '15px',
    borderBottom: '1px solid #eee',
  },
  price: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#667eea',
  },
  stock: {
    fontSize: '12px',
  },
  inStock: {
    color: '#27ae60',
  },
  outOfStock: {
    color: '#e74c3c',
  },
  buySection: {
    display: 'flex',
    gap: '10px',
    alignItems: 'center',
  },
  quantityInput: {
    flex: 1,
    padding: '8px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    fontSize: '14px',
  },
  buyBtn: {
    flex: 1,
    padding: '10px',
    backgroundColor: '#27ae60',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    fontWeight: '600',
    fontSize: '14px',
  },
};
