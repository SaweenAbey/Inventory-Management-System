import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { authAPI } from '../api';
import './Auth.css';

export default function Login() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const handleChange = (e) => {
    if (!e || !e.target) return;
    const { name, value } = e.target;
    if (name) {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Defensive checks
    if (!formData || typeof formData !== 'object') {
      toast.error('Form data error. Please refresh page.');
      console.error('FormData is invalid:', formData);
      return;
    }

    const username = formData.username?.trim() || '';
    const password = formData.password?.trim() || '';

    if (!username || !password) {
      toast.error('Please fill in all fields');
      return;
    }

    setLoading(true);
    try {
      const loginData = { username, password };
      const response = await authAPI.login(loginData);
      
      if (!response || !response.data) {
        throw new Error('Invalid response from server');
      }

      const responseData = response.data;
      const token = responseData.token;
      
      // New response structure from backend
      if (!token) {
        throw new Error('Token or user data missing from response');
      }

      // Build user object from response
      const user = {
        userId: responseData.userId,
        username: responseData.username,
        email: responseData.email,
        role: responseData.role
      };

      if (!user.username || !user.role) {
        throw new Error('User data incomplete in response');
      }

      // Store token and user info
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));

      toast.success(`✅ Welcome back, ${user.username}!`);
      console.log('✅ Login successful for user:', user.username, 'Role:', user.role);
      
      // Redirect based on role
      setTimeout(() => {
        if (user.role === 'ADMIN') {
          navigate('/admin/dashboard');
        } else if (user.role === 'BUYER') {
          navigate('/buyer/marketplace');
        } else {
          navigate('/');
        }
      }, 800);
    } catch (error) {
      console.error('Login error details:', error);
      const errorMsg = error.response?.data?.message || error.message || 'Login failed. Please check credentials.';
      toast.error(errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-box">
        <h1>Sign In</h1>
        <p className="auth-subtitle">Welcome to Inventory Management System</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder="Enter your username"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Enter your password"
              disabled={loading}
            />
          </div>

          <button
            type="submit"
            className="auth-button"
            disabled={loading}
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          <p>Don't have an account?</p>
          <button
            type="button"
            className="link-button"
            onClick={() => navigate('/signup')}
            disabled={loading}
          >
            Sign up here
          </button>
        </div>
      </div>
    </div>
  );
}
