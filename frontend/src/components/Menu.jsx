import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Coffee, Plus } from 'lucide-react';

const Menu = ({ onOrderClick }) => {
    const [drinks, setDrinks] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchMenu = async () => {
            try {
                const response = await axios.get('/api/menu');
                setDrinks(response.data);
            } catch (error) {
                console.error('Error fetching menu:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchMenu();
    }, []);

    if (loading) return <div className="p-8 text-center text-text-body">Loading menu...</div>;

    return (
        <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-3">
            {drinks.map((drink) => (
                <div
                    key={drink.id}
                    className="bg-primary-surface/50 border-l-2 border-accent/30 rounded-lg p-3 hover:bg-primary-surface hover:border-accent transition-all group"
                >
                    <div className="flex items-start justify-between gap-3">
                        <div className="flex items-center gap-3 flex-1 min-w-0">
                            <div className="p-2 bg-primary-bg rounded-md border border-white/5 group-hover:bg-accent group-hover:border-accent transition-colors flex-shrink-0">
                                <Coffee className="w-5 h-5 text-accent group-hover:text-black transition-colors" />
                            </div>
                            <div className="flex-1 min-w-0">
                                <h3 className="text-base font-bold text-white truncate group-hover:text-accent transition-colors">{drink.name}</h3>
                                <p className="text-xs text-text-body">~{drink.prepTimeMinutes} min</p>
                            </div>
                        </div>
                        <div className="flex flex-col items-end gap-2 flex-shrink-0">
                            <span className="font-bold text-sm text-accent">${drink.price.toFixed(2)}</span>
                            <button
                                onClick={() => onOrderClick(drink)}
                                className="p-1.5 bg-accent hover:bg-accent-hover text-black rounded transition-all"
                                title="Order Now"
                            >
                                <Plus className="w-4 h-4" />
                            </button>
                        </div>
                    </div>
                </div>
            ))}
        </div>
    );
};

export default Menu;
