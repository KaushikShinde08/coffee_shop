import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation, Link } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import Menu from './components/Menu';
import OrderForm from './components/OrderForm';
import QueueBoard from './components/QueueBoard';
import StatsPage from './pages/StatsPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import { Coffee, LogOut, User as UserIcon, BarChart3 } from 'lucide-react';

const ProtectedRoute = ({ children }) => {
    const { user, loading } = useAuth();
    const location = useLocation();

    if (loading) return <div className="min-h-screen flex items-center justify-center">Loading...</div>;

    if (!user) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return children;
};

const MainLayout = () => {
    const [selectedDrink, setSelectedDrink] = useState(null);
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const { user, logout } = useAuth();
    const location = useLocation();

    const handleOrderSuccess = () => {
        setRefreshTrigger(prev => prev + 1);
    };

    return (
        <div className="min-h-screen bg-primary-bg text-text-main">
            {/* Header */}
            <header className="bg-primary-surface/80 border-b border-white/5 sticky top-0 z-40 backdrop-blur-md">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
                    <div className="flex items-center gap-6">
                        <div className="flex items-center gap-3">
                            <div className="bg-accent p-2 rounded-lg text-black">
                                <Coffee className="w-6 h-6" />
                            </div>
                            <h1 className="text-xl font-bold text-white">Bean & Brew <span className="text-accent font-normal">Smart Queue</span></h1>
                        </div>

                        {/* Navigation Tabs */}
                        <nav className="hidden md:flex items-center gap-2">
                            <Link
                                to="/"
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${location.pathname === '/'
                                    ? 'bg-accent text-black'
                                    : 'text-text-body hover:bg-primary-bg hover:text-white'
                                    }`}
                            >
                                Overview
                            </Link>
                            <Link
                                to="/stats"
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center gap-2 ${location.pathname === '/stats'
                                    ? 'bg-accent text-black'
                                    : 'text-text-body hover:bg-primary-bg hover:text-white'
                                    }`}
                            >
                                <BarChart3 className="w-4 h-4" />
                                Stats
                            </Link>
                        </nav>
                    </div>
                    <div className="flex items-center gap-4">
                        <div className="hidden md:flex items-center gap-2 text-sm text-text-body">
                            <UserIcon className="w-4 h-4" />
                            <span className="font-medium text-white">{user?.username}</span>
                        </div>
                        <button
                            onClick={logout}
                            className="flex items-center gap-2 text-sm font-medium text-text-body hover:text-accent transition-colors"
                        >
                            <LogOut className="w-4 h-4" />
                            <span className="hidden sm:inline">Logout</span>
                        </button>
                    </div>
                </div>
            </header>

            <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="mb-8">
                    <h2 className="text-xl font-semibold text-white mb-4 border-l-2 border-accent pl-3">Menu</h2>
                    <Menu onOrderClick={setSelectedDrink} />
                </div>

                <div className="pt-6 border-t border-white/10">
                    <h2 className="text-xl font-semibold text-white mb-4 border-l-2 border-accent pl-3">Live Status</h2>
                    <QueueBoard refreshTrigger={refreshTrigger} />
                </div>
            </main>

            {selectedDrink && (
                <OrderForm
                    drink={selectedDrink}
                    onClose={() => setSelectedDrink(null)}
                    onOrderSuccess={handleOrderSuccess}
                />
            )}
        </div>
    );
};

function App() {
    return (
        <AuthProvider>
            <Router>
                <Routes>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route
                        path="/"
                        element={
                            <ProtectedRoute>
                                <MainLayout />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/stats"
                        element={
                            <ProtectedRoute>
                                <StatsPage />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </Router>
        </AuthProvider>
    );
}

export default App;
