import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Coffee, Lock, User, Mail, ArrowRight } from 'lucide-react';
import loginBg from '../assets/login-bg.png';

const SignupPage = () => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { signup } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (formData.password !== formData.confirmPassword) {
            setError("Passwords don't match");
            return;
        }

        setLoading(true);
        try {
            await signup(formData.username, formData.password, formData.email);
            navigate('/');
        } catch (err) {
            // Error message already set by AuthContext
            setError(err.response?.data?.message || 'Failed to create account');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen grid grid-cols-1 md:grid-cols-2 bg-primary-bg">
            {/* Left Panel - Visual (Order swapped on mobile via hidden/flex logic usually, but here simple hidden md:block is fine) */}
            <div className="hidden md:block relative bg-primary-surface overflow-hidden">
                <img
                    src={loginBg}
                    alt="Coffee Shop Ambience"
                    className="absolute inset-0 w-full h-full object-cover opacity-60 mix-blend-overlay"
                />
                <div className="absolute inset-0 bg-gradient-to-r from-primary-bg via-primary-bg/80 to-transparent"></div>
                <div className="absolute bottom-20 left-12 text-white max-w-lg z-10">
                    <h1 className="text-5xl font-bold mb-6 leading-tight text-text-main">Join the<br /><span className="text-accent">Community</span></h1>
                    <p className="text-xl text-text-body font-light">Create an account to track your orders, earn loyalty points, and skip the queue.</p>
                </div>
            </div>

            {/* Right Panel - Form */}
            <div className="flex items-center justify-center p-8 bg-primary-bg">
                <div className="w-full max-w-md space-y-8 bg-primary-surface p-10 rounded-2xl border border-white/5 shadow-2xl">
                    <div className="text-center md:text-left">
                        <div className="md:hidden flex justify-center mb-4">
                            <div className="bg-accent p-2 rounded-lg text-black inline-block">
                                <Coffee className="w-8 h-8" />
                            </div>
                        </div>
                        <h2 className="text-3xl font-bold text-text-main">Create Account</h2>
                        <p className="mt-2 text-text-body">Start your smart coffee experience today.</p>
                    </div>

                    <form className="mt-8 space-y-5" onSubmit={handleSubmit}>
                        {error && (
                            <div className="bg-red-500/10 text-red-500 p-4 rounded-lg text-sm flex items-center gap-2 border border-red-500/20">
                                <span className="block w-1.5 h-1.5 rounded-full bg-red-500"></span>
                                {error}
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-text-body mb-1">Username</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <User className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    name="username"
                                    type="text"
                                    required
                                    value={formData.username}
                                    onChange={handleChange}
                                    className="input-field pl-10 block w-full px-4 py-3"
                                    placeholder="Choose a username"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-text-body mb-1">Email Address</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Mail className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    name="email"
                                    type="email"
                                    required
                                    value={formData.email}
                                    onChange={handleChange}
                                    className="input-field pl-10 block w-full px-4 py-3"
                                    placeholder="you@example.com"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-text-body mb-1">Password</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Lock className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    name="password"
                                    type="password"
                                    required
                                    value={formData.password}
                                    onChange={handleChange}
                                    className="input-field pl-10 block w-full px-4 py-3"
                                    placeholder="Create a password"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-text-body mb-1">Confirm Password</label>
                            <div className="relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <Lock className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    name="confirmPassword"
                                    type="password"
                                    required
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    className="input-field pl-10 block w-full px-4 py-3"
                                    placeholder="Confirm your password"
                                />
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className={`w-full flex justify-center py-3 px-4 rounded-lg shadow-lg text-sm font-medium text-black bg-accent hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent transition-all mt-6 transform hover:scale-[1.02] ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                        >
                            {loading ? 'Creating Account...' : 'Create Account'}
                        </button>

                        <div className="text-center mt-4">
                            <span className="text-text-body text-sm">Already have an account? </span>
                            <Link to="/login" className="font-medium text-accent hover:text-accent-hover text-sm">Log in here</Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default SignupPage;
