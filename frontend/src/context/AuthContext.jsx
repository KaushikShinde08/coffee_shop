import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        // Check local storage for session restoration
        const storedUser = localStorage.getItem('coffee_user');
        // We use 'coffee_auth' to store the Basic Auth token (base64 of username:password)
        const authToken = localStorage.getItem('coffee_auth');

        if (storedUser && authToken) {
            setUser(JSON.parse(storedUser));
            axios.defaults.headers.common['Authorization'] = `Basic ${authToken}`;
        }
        setLoading(false);
    }, []);

    const login = async (username, password) => {
        try {
            // First verify credentials via the custom auth endpoint (optional, but good for custom logic)
            const response = await axios.post('/api/auth/login', { username, password });

            // If successful, setup Basic Auth for future requests
            const basicAuthToken = btoa(`${username}:${password}`);
            axios.defaults.headers.common['Authorization'] = `Basic ${basicAuthToken}`;

            const { token, username: returnedUsername, role } = response.data;
            const userData = { username: returnedUsername, role };

            setUser(userData);
            localStorage.setItem('coffee_user', JSON.stringify(userData));
            localStorage.setItem('coffee_auth', basicAuthToken);

            return true;
        } catch (error) {
            console.error("Login failed", error);
            throw error;
        }
    };

    const signup = async (username, password, email) => {
        setLoading(true);
        setError('');
        try {
            await axios.post('/api/auth/signup', { username, password, email });
            await login(username, password);
        } catch (err) {
            console.error('Signup error:', err);
            setError(err.response?.data?.message || 'Failed to create account');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    const logout = () => {
        setUser(null);
        localStorage.removeItem('coffee_user');
        localStorage.removeItem('coffee_auth');
        delete axios.defaults.headers.common['Authorization'];
    };

    return (
        <AuthContext.Provider value={{ user, login, signup, logout, loading, error }}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
