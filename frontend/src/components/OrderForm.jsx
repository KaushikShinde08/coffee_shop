import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { X, Award, Coffee, ArrowRight, Loader2, AlertCircle, User } from 'lucide-react';

const OrderForm = ({ drink, onClose, onOrderSuccess }) => {
    const { user } = useAuth();
    const [isLoyal, setIsLoyal] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState('');
    const [orderData, setOrderData] = useState({ size: 'Medium', sugar: '50%' });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        setError('');
        try {
            await axios.post('/api/orders', {
                customerName: user.username, // Use authenticated user's name
                drinkId: drink.id,
                isLoyal,
                size: orderData.size,
                sugar: orderData.sugar
            });
            onOrderSuccess();
            onClose();
        } catch (error) {
            console.error('Error placing order:', error);
            setError('Failed to place order. Please try again.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
            <div className="bg-primary-surface rounded-2xl w-full max-w-lg p-6 shadow-2xl shadow-accent/20 border border-white/10 transform transition-all relative">
                <button
                    onClick={onClose}
                    className="absolute top-4 right-4 text-text-body hover:text-white transition-colors"
                >
                    <X className="w-6 h-6" />
                </button>

                <div className="mb-6">
                    <h2 className="text-2xl font-bold text-white flex items-center gap-2">
                        <Coffee className="w-6 h-6 text-accent" />
                        Place Order
                    </h2>
                    <p className="text-text-body mt-1">Adjust your {drink.name} to perfection</p>
                </div>

                {error && (
                    <div className="mb-6 bg-red-500/10 border border-red-500/20 text-red-500 p-3 rounded-lg text-sm flex items-center gap-2">
                        <AlertCircle className="w-4 h-4" />
                        {error}
                    </div>
                )}

                <div className="mb-6 p-4 bg-primary-bg rounded-xl border border-white/5">
                    <p className="text-sm text-text-body mb-1">You selected</p>
                    <p className="text-lg font-bold text-accent">{drink.name}</p>
                    <p className="text-sm text-white mt-1">${drink.price.toFixed(2)}</p>
                </div>

                <div className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium text-text-body mb-3">Size</label>
                        <div className="grid grid-cols-3 gap-3">
                            {['Small', 'Medium', 'Large'].map((size) => (
                                <button
                                    key={size}
                                    type="button"
                                    onClick={() => setOrderData({ ...orderData, size })}
                                    className={`py-2 px-4 rounded-lg text-sm font-medium transition-all ${orderData.size === size
                                            ? 'bg-accent text-black shadow-lg shadow-accent/20 scale-105'
                                            : 'bg-primary-bg text-text-body border border-white/10 hover:border-accent/50 hover:text-white'
                                        }`}
                                >
                                    {size}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-text-body mb-3">Sugar Level</label>
                        <div className="grid grid-cols-4 gap-2">
                            {['0%', '25%', '50%', '100%'].map((level) => (
                                <button
                                    key={level}
                                    type="button"
                                    onClick={() => setOrderData({ ...orderData, sugar: level })}
                                    className={`py-2 px-2 rounded-lg text-sm font-medium transition-all ${orderData.sugar === level
                                            ? 'bg-accent text-black shadow-lg shadow-accent/20 scale-105'
                                            : 'bg-primary-bg text-text-body border border-white/10 hover:border-accent/50 hover:text-white'
                                        }`}
                                >
                                    {level}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div
                        onClick={() => setIsLoyal(!isLoyal)}
                        className={`cursor-pointer p-4 rounded-xl border transition-all flex items-center gap-3 ${isLoyal ? 'border-accent bg-accent/10' : 'border-white/10 bg-primary-bg hover:border-accent/50'
                            }`}
                    >
                        <div className={`p-2 rounded-full ${isLoyal ? 'bg-accent text-black' : 'bg-white/10 text-gray-400'}`}>
                            <Award className="w-5 h-5" />
                        </div>
                        <div>
                            <p className="font-medium text-white">Loyalty Member</p>
                            <p className="text-xs text-text-body">Get priority service (Demo)</p>
                        </div>
                    </div>

                    <div className="flex items-center gap-2 mt-4 px-1 p-4 bg-primary-bg rounded-lg border border-white/5">
                        <User className="w-5 h-5 text-accent" />
                        <span className="text-text-body text-sm">Ordering as:</span>
                        <span className="font-semibold text-white">{user?.username}</span>
                    </div>

                    <div className="pt-4 border-t border-white/10 flex justify-end gap-3">
                        <button
                            onClick={onClose}
                            className="px-4 py-2 text-text-body hover:text-white font-medium transition-colors"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSubmit}
                            disabled={submitting}
                            className="btn-primary min-w-[120px] flex items-center justify-center"
                        >
                            {submitting ? (
                                <Loader2 className="w-5 h-5 animate-spin" />
                            ) : (
                                <>
                                    Confirm <ArrowRight className="w-4 h-4 ml-2" />
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default OrderForm;
