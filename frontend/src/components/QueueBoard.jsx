import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Clock, CheckCircle2, ListFilter, AlertCircle } from 'lucide-react';

const QueueBoard = ({ refreshTrigger }) => {
    const [orders, setOrders] = useState([]);

    const fetchOrders = async () => {
        try {
            const response = await axios.get('/api/orders');
            setOrders(response.data);
        } catch (error) {
            console.error('Error fetching queue:', error);
        }
    };

    useEffect(() => {
        fetchOrders();
        const interval = setInterval(fetchOrders, 3000); // Poll every 3s
        return () => clearInterval(interval);
    }, [refreshTrigger]);

    const handlePickup = async (orderId) => {
        try {
            await axios.put(`/api/orders/${orderId}/pickup`);
            fetchOrders();
        } catch (error) {
            console.error('Error picking up order:', error);
            alert('Could not pick up order');
        }
    };

    const waitingOrders = orders
        .filter(o => o.status === 'WAITING' || o.status === 'PLACED')
        .sort((a, b) => b.priorityScore - a.priorityScore);

    const preparingOrders = orders.filter(o => o.status === 'PREPARING')
        .sort((a, b) => a.estimatedCompletionTime?.localeCompare(b.estimatedCompletionTime));

    const readyOrders = orders.filter(o => o.status === 'READY_TO_PICKUP');

    return (
        <div className="flex flex-col lg:flex-row gap-4 h-[calc(100vh-200px)]">

            {/* WAITING COLUMN - Narrow Left */}
            <div className="lg:w-[280px] bg-primary-surface/30 rounded-lg p-3 border-l-2 border-white/20 flex flex-col">
                <div className="flex items-center gap-2 mb-3 pb-2 border-b border-white/10">
                    <ListFilter className="w-4 h-4 text-text-body" />
                    <h2 className="font-semibold text-sm text-white">Waiting ({waitingOrders.length})</h2>
                </div>
                <div className="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
                    {waitingOrders.map(order => (
                        <div key={order.id} className="bg-primary-bg/50 p-2.5 rounded border-l-2 border-white/20 hover:border-white/40 transition-colors">
                            <div className="flex justify-between items-start mb-1">
                                <p className="font-semibold text-sm text-white truncate">{order.customerName}</p>
                                {order.loyal && <span className="text-[9px] bg-accent/20 text-accent px-1.5 py-0.5 rounded-full font-medium">VIP</span>}
                            </div>
                            <p className="text-xs text-text-body truncate">{order.drink.name}</p>
                            <div className="flex justify-between items-center mt-1.5">
                                <span className="text-[10px] text-text-body/60">Priority</span>
                                <span className="text-xs font-bold text-accent">{order.priorityScore.toFixed(0)}</span>
                            </div>
                        </div>
                    ))}
                    {waitingOrders.length === 0 && (
                        <div className="text-center text-text-body/40 text-xs py-6">Queue is empty</div>
                    )}
                </div>
            </div>

            {/* PREPARING COLUMN - Dominant Center */}
            <div className="flex-1 bg-primary-surface/50 rounded-lg p-4 border-l-2 border-accent/40 flex flex-col relative">
                <div className="absolute top-0 left-0 w-full h-0.5 bg-accent/30"></div>
                <div className="flex items-center gap-2 mb-4 pb-2 border-b border-accent/20">
                    <Clock className="w-5 h-5 text-accent animate-pulse" />
                    <h2 className="font-bold text-base text-accent">Preparing ({preparingOrders.length}/3)</h2>
                </div>
                <div className="flex-1 overflow-y-auto space-y-3 pr-2 custom-scrollbar">
                    {preparingOrders.map(order => (
                        <div key={order.id} className="bg-primary-bg/60 p-3 rounded-lg border-l-3 border-accent shadow-sm relative group hover:bg-primary-bg transition-colors">
                            <div className="flex justify-between items-start mb-2">
                                <div>
                                    <p className="font-bold text-base text-white">{order.customerName}</p>
                                    <p className="text-sm text-text-body">{order.drink.name}</p>
                                    {order.loyal && <span className="text-[10px] bg-accent/20 text-accent px-2 py-0.5 rounded-full font-medium inline-block mt-1">VIP</span>}
                                </div>
                                {order.assignedBarista && (
                                    <div className="text-right">
                                        <p className="text-[10px] text-text-body/60">Barista</p>
                                        <p className="text-xs font-semibold text-accent">{order.assignedBarista.name}</p>
                                    </div>
                                )}
                            </div>
                            {order.estimatedCompletionTime && (
                                <div className="flex items-center gap-1.5 mt-2 pt-2 border-t border-white/5">
                                    <Clock className="w-3.5 h-3.5 text-accent/60" />
                                    <span className="text-xs text-text-body">Ready: {new Date(order.estimatedCompletionTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                                </div>
                            )}
                        </div>
                    ))}
                    {preparingOrders.length === 0 && (
                        <div className="text-center text-text-body/40 text-sm py-12">No orders in preparation</div>
                    )}
                </div>
            </div>

            {/* READY COLUMN - Narrow Right */}
            <div className="lg:w-[300px] bg-primary-surface/30 rounded-lg p-3 border-l-2 border-green-500/30 flex flex-col">
                <div className="flex items-center gap-2 mb-3 pb-2 border-b border-green-500/20">
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    <h2 className="font-semibold text-sm text-green-400">Ready ({readyOrders.length})</h2>
                </div>
                <div className="flex-1 overflow-y-auto space-y-2 pr-1 custom-scrollbar">
                    {readyOrders.map(order => (
                        <div key={order.id} className="bg-primary-bg/50 p-2.5 rounded border-l-2 border-green-500/40 hover:border-green-500/60 transition-colors">
                            <div className="flex justify-between items-start mb-1.5">
                                <div className="flex-1 min-w-0">
                                    <p className="font-semibold text-sm text-white truncate">{order.customerName}</p>
                                    <p className="text-xs text-text-body truncate">{order.drink.name}</p>
                                    {order.loyal && <span className="text-[9px] bg-accent/20 text-accent px-1.5 py-0.5 rounded-full font-medium inline-block mt-1">VIP</span>}
                                </div>
                            </div>
                            <button
                                onClick={() => handlePickup(order.id)}
                                className="w-full mt-2 px-3 py-1.5 bg-green-600 hover:bg-green-500 text-white text-xs font-medium rounded transition-colors flex items-center justify-center gap-1.5"
                            >
                                <CheckCircle2 className="w-3.5 h-3.5" />
                                Pickup
                            </button>
                        </div>
                    ))}
                    {readyOrders.length === 0 && (
                        <div className="text-center text-text-body/40 text-xs py-6">No orders ready</div>
                    )}
                </div>
            </div>

        </div>
    );
};

export default QueueBoard;
