import React, { useState, useEffect } from 'react';
import axios from 'axios';
import StatCard from '../components/StatCard';
import { CheckCircle2, AlertTriangle } from 'lucide-react';

const StatsPage = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            const response = await axios.get('/api/stats');
            setStats(response.data);
        } catch (error) {
            console.error('Error fetching stats:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-primary-bg text-text-main flex items-center justify-center">
                <p className="text-text-body">Loading statistics...</p>
            </div>
        );
    }

    if (!stats || stats.totalOrders === 0) {
        return (
            <div className="min-h-screen bg-primary-bg text-text-main flex items-center justify-center">
                <div className="text-center">
                    <p className="text-xl text-text-body mb-2">No data available</p>
                    <p className="text-sm text-text-body/60">Place some orders to see analytics</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-primary-bg text-text-main pb-12">
            {/* Header */}
            <div className="bg-primary-surface/50 border-b border-white/5 py-6">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center">
                        <div>
                            <h1 className="text-2xl font-bold text-white">Analytics Dashboard</h1>
                            <p className="text-sm text-text-body mt-1">Real-time queue performance metrics</p>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                {/* KPI Summary Cards */}
                <div className="mb-8">
                    <h2 className="text-lg font-semibold text-white mb-4 border-l-2 border-accent pl-3">Key Metrics</h2>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3 mb-3">
                        <StatCard
                            label="Total Orders"
                            value={stats.totalOrders}
                        />
                        <StatCard
                            label="Avg Wait Time"
                            value={`${stats.avgWaitTime.toFixed(1)} min`}
                        />
                        <StatCard
                            label="Weighted Avg Wait"
                            value={`${stats.weightedAvgWait.toFixed(1)} min`}
                        />
                        <StatCard
                            label="Max Wait Time"
                            value={`${stats.maxWaitTime.toFixed(1)} min`}
                        />
                        <StatCard
                            label="Timeout Rate"
                            value={`${stats.timeoutRate.toFixed(1)}%`}
                            subtitle={`${stats.timeoutCount} orders`}
                        />
                    </div>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-3">
                        <StatCard
                            label="Barista Utilization"
                            value={`${stats.baristaUtilization.toFixed(1)}%`}
                        />
                        <StatCard
                            label="Fairness Issues"
                            value={stats.fairnessIssues}
                        />
                        <StatCard
                            label="Starvation Count"
                            value={stats.starvationCount}
                        />
                        <StatCard
                            label="FIFO Skips"
                            value={stats.fifoSkips}
                        />
                        <StatCard
                            label="Completion Inversions"
                            value={stats.completionInversions}
                        />
                    </div>
                </div>

                {/* Validation Status Banner */}
                <div className={`mb-8 rounded-lg p-4 border-l-4 flex items-center gap-3 ${stats.validationStatus === 'passed'
                        ? 'bg-green-900/20 border-green-500'
                        : 'bg-yellow-900/20 border-yellow-500'
                    }`}>
                    {stats.validationStatus === 'passed' ? (
                        <>
                            <CheckCircle2 className="w-6 h-6 text-green-500 flex-shrink-0" />
                            <div>
                                <p className="font-semibold text-green-400">All validation checks passed</p>
                                <p className="text-xs text-text-body/70 mt-1">Max 3 baristas, concurrency respected</p>
                            </div>
                        </>
                    ) : (
                        <>
                            <AlertTriangle className="w-6 h-6 text-yellow-500 flex-shrink-0" />
                            <div>
                                <p className="font-semibold text-yellow-400">Constraint violations detected</p>
                                <p className="text-xs text-text-body/70 mt-1">{stats.violationsCount} customers exceeded 10-minute wait time</p>
                            </div>
                        </>
                    )}
                </div>

                {/* Order Distribution Table */}
                {stats.drinkDistribution && stats.drinkDistribution.length > 0 && (
                    <div className="mb-8">
                        <h2 className="text-lg font-semibold text-white mb-4 border-l-2 border-accent pl-3">Order Distribution by Drink Type</h2>
                        <div className="bg-primary-surface/30 rounded-lg overflow-hidden border border-white/5">
                            <table className="w-full">
                                <thead className="bg-primary-surface/50 border-b border-white/10">
                                    <tr>
                                        <th className="text-left py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Drink Type</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Count</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Percentage</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Prep Time</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stats.drinkDistribution.map((drink, index) => (
                                        <tr key={index} className="border-b border-white/5 hover:bg-primary-surface/20 transition-colors">
                                            <td className="py-3 px-4 text-sm text-white">{drink.drinkType}</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{drink.orderCount}</td>
                                            <td className="py-3 px-4 text-sm text-right text-accent font-medium">{drink.percentage.toFixed(1)}%</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{drink.prepTime} min</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {/* Barista Performance Table */}
                {stats.baristaPerformance && stats.baristaPerformance.length > 0 && (
                    <div className="mb-8">
                        <h2 className="text-lg font-semibold text-white mb-4 border-l-2 border-accent pl-3">Barista Performance Comparison</h2>
                        <div className="bg-primary-surface/30 rounded-lg overflow-hidden border border-white/5">
                            <table className="w-full">
                                <thead className="bg-primary-surface/50 border-b border-white/10">
                                    <tr>
                                        <th className="text-left py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Test</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Overall Avg</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">B1 Avg</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">B2 Avg</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">B3 Avg</th>
                                        <th className="text-right py-3 px-4 text-xs font-semibold text-accent uppercase tracking-wide">Complaints</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stats.baristaPerformance.map((perf, index) => (
                                        <tr key={index} className="border-b border-white/5 hover:bg-primary-surface/20 transition-colors">
                                            <td className="py-3 px-4 text-sm text-white">{perf.testName}</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{perf.overallAvgWait.toFixed(1)}m</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{perf.barista1Avg > 0 ? perf.barista1Avg.toFixed(1) : '—'}m</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{perf.barista2Avg > 0 ? perf.barista2Avg.toFixed(1) : '—'}m</td>
                                            <td className="py-3 px-4 text-sm text-right text-text-body">{perf.barista3Avg > 0 ? perf.barista3Avg.toFixed(1) : '—'}m</td>
                                            <td className="py-3 px-4 text-sm text-right text-accent font-medium">{perf.complaints}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {/* Time Slot Performance Table */}
                {stats.timeSlotPerformance && stats.timeSlotPerformance.length > 0 && (
                    <div className="mb-8">
                        <h2 className="text-lg font-semibold text-white mb-4 border-l-2 border-accent pl-3">Performance by Time Slot</h2>
                        <div className="bg-primary-surface/30 rounded-lg overflow-hidden border border-white/5">
                            <table className="w-full text-sm">
                                <thead className="bg-primary-surface/50 border-b border-white/10">
                                    <tr>
                                        <th className="text-left py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Time Slot</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Arrived</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Completed</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Avg Wait</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Max Wait</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Timeout %</th>
                                        <th className="text-right py-3 px-3 text-xs font-semibold text-accent uppercase tracking-wide">Fair Viol.</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {stats.timeSlotPerformance.map((slot, index) => (
                                        <tr key={index} className="border-b border-white/5 hover:bg-primary-surface/20 transition-colors">
                                            <td className="py-2.5 px-3 text-white font-medium">{slot.timeSlot}</td>
                                            <td className="py-2.5 px-3 text-right text-text-body">{slot.customersArrived}</td>
                                            <td className="py-2.5 px-3 text-right text-text-body">{slot.ordersCompleted}</td>
                                            <td className="py-2.5 px-3 text-right text-text-body">{slot.avgWait.toFixed(1)}m</td>
                                            <td className="py-2.5 px-3 text-right text-text-body">{slot.maxWait.toFixed(1)}m</td>
                                            <td className="py-2.5 px-3 text-right text-accent font-medium">{slot.timeoutPercent.toFixed(1)}%</td>
                                            <td className="py-2.5 px-3 text-right text-text-body">{slot.fairnessViolations}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default StatsPage;
