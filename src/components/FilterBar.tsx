import React from 'react';

interface FilterBarProps {
  selectedTab: string;
  onSelectTab: (tab: string) => void;
}

export const FilterBar: React.FC<FilterBarProps> = ({ selectedTab, onSelectTab }) => {
  const tabs = [
    { id: 'ALL', label: 'Toutes les Paires' },
    { id: 'SNIPER', label: '🎯 Sniper (4/4)', badge: '95-100%' },
    { id: 'GOOD_SETUP', label: '⚡ Moyennes (3/4)', badge: '75-90%' },
    { id: 'WATCHLIST', label: '👁️ Watchlist (2/4)', badge: '60-70%' },
    { id: 'CRYPTO', label: 'Crypto' },
    { id: 'FOREX', label: 'Forex' },
    { id: 'COMMODITIES', label: 'Matières 1ères' },
    { id: 'SYNTHETICS', label: 'Deriv Synthetics' }
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 md:px-6 py-2 overflow-x-auto no-scrollbar">
      <div className="flex items-center space-x-2 min-w-max pb-1">
        {tabs.map((tab) => {
          const isSelected = selectedTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onSelectTab(tab.id)}
              className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                isSelected
                  ? 'bg-surface-elevated text-[#F1F5F9] border border-setup shadow-sm'
                  : 'bg-surface text-[#94A3B8] border border-border-subtle hover:border-border hover:text-[#F1F5F9]'
              }`}
            >
              <span>{tab.label}</span>
              {tab.badge && (
                <span
                  className={`text-[9px] px-1.5 py-0.2 rounded font-mono font-bold ${
                    isSelected ? 'bg-setup/20 text-setup' : 'bg-surface-variant text-[#64748B]'
                  }`}
                >
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
};
