import React from 'react';

const StatCard = ({ label, value, subtitle }) => {
    return (
        <div className="bg-primary-surface/40 rounded-lg p-4 border-l-2 border-accent/20 hover:border-accent/40 transition-colors">
            <p className="text-xs text-text-body/60 uppercase tracking-wide mb-2">{label}</p>
            <p className="text-2xl font-bold text-accent mb-1">{value}</p>
            {subtitle && <p className="text-xs text-text-body/50">{subtitle}</p>}
        </div>
    );
};

export default StatCard;
