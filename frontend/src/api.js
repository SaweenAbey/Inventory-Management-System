import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests if it exists
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

export const inventoryAPI = {
  getItems: () => api.get('/inventory/items'),
  getItemById: (id) => api.get(`/inventory/items/${id}`),
  addItem: (data) => api.post('/inventory/items/add', data),
  updateItem: (data) => api.put('/inventory/items/update', data),
  deleteItem: (id) => api.delete(`/inventory/items/delete/${id}`),
  getActiveItems: () => api.get('/inventory/items/active/only'),
};

export const purchaseAPI = {
  buyItem: (data) => api.post('/purchase/buy-item', data),
  getMyOrders: () => api.get('/purchase/my-orders'),
  getOrderDetails: (id) => api.get(`/purchase/order/${id}`),
  confirmOrder: (id) => api.put(`/purchase/order/${id}/confirm`),
  cancelOrder: (id) => api.put(`/purchase/order/${id}/cancel`),
};

export default api;
