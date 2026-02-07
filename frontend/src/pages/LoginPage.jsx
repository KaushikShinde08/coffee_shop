import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Coffee, Lock, User } from 'lucide-react';
import loginBg from '../assets/login-bg-modern.png';

const LoginPage = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await login(username, password);
            navigate('/');
        } catch (err) {
            setError('Invalid credentials. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen grid grid-cols-1 md:grid-cols-2 bg-primary-bg">
            {/* Left Panel - Visual */}
            <div className="hidden md:block relative bg-primary-surface overflow-hidden">
                <img
                    src={loginBg}
                    alt="Coffee Shop Ambience"
                    className="absolute inset-0 w-full h-full object-cover opacity-60 mix-blend-overlay"
                />
                <div className="absolute inset-0 bg-gradient-to-r from-primary-bg via-primary-bg/80 to-transparent"></div>
                <div className="absolute bottom-20 left-12 text-white max-w-lg z-10">
                    <div className="flex items-center gap-3 mb-6">
                        <div className="bg-white/10 p-3 rounded-xl backdrop-blur-md border border-white/10">
                            <Coffee className="w-8 h-8 text-accent" />
                        </div>
                    </div>
                    <h1 className="text-5xl font-bold mb-6 leading-tight text-text-main">Brewed for you.<br /><span className="text-accent">Served smart.</span></h1>
                    <p className="text-xl text-text-body font-light">Experience the seamless way to order your favorite coffee. Skip the line, not the taste.</p>
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
                        <h2 className="text-3xl font-bold text-text-main">Welcome back</h2>
                        <p className="mt-2 text-text-body">Please enter your details to sign in.</p>
                    </div>

                    <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
                        {error && (
                            <div className="bg-red-500/10 text-red-500 p-4 rounded-lg text-sm flex items-center gap-2 border border-red-500/20">
                                <span className="block w-1.5 h-1.5 rounded-full bg-red-500"></span>
                                {error}
                            </div>
                        )}

                        <div className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-text-body mb-1">Username</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <User className="h-5 w-5 text-gray-500" />
                                    </div>
                                    <input
                                        type="text"
                                        required
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                        className="input-field pl-10 block w-full px-4 py-3"
                                        placeholder="Enter your username"
                                    />
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-text-body mb-1">Password</label>
                                <div className="relative">
                                    <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                        <Lock className="h-5 w-5 text-gray-500" />
                                    </div>
                                    <input
                                        type="password"
                                        required
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="input-field pl-10 block w-full px-4 py-3"
                                        placeholder="••••••••"
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="flex items-center justify-between">
                            <div className="flex items-center">
                                <input id="remember-me" name="remember-me" type="checkbox" className="h-4 w-4 text-accent focus:ring-accent border-gray-600 rounded bg-primary-bg" />
                                <label htmlFor="remember-me" className="ml-2 block text-sm text-text-body">Remember me</label>
                            </div>
                            <div className="text-sm">
                                <a href="#" className="font-medium text-accent hover:text-accent-hover">Forgot password?</a>
                            </div>
                        </div>

                        <button
                            type="submit"
                            disabled={loading}
                            className={`w-full flex justify-center py-3 px-4 rounded-lg shadow-lg text-sm font-medium text-black bg-accent hover:bg-accent-hover focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-accent transition-all transform hover:scale-[1.02] ${loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                        >
                            {loading ? (
                                <span className="flex items-center gap-2">
                                    <svg className="animate-spin h-4 w-4 text-black" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Signing in...
                                </span>
                            ) : 'Sign in'}
                        </button>

                        <div className="text-center mt-4">
                            <span className="text-text-body text-sm">Don't have an account? </span>
                            <Link to="/signup" className="font-medium text-accent hover:text-accent-hover text-sm">Sign up for free</Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
