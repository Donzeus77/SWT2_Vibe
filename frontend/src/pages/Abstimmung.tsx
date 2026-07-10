import { useState, useEffect } from "react";
import { api, type MenuItem } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import { ThumbsUp, Trophy, Lock } from "lucide-react";
import { cn } from "../lib/utils";

export default function Abstimmung() {
  const { user, isLoggedIn } = useAuth();
  const [menu, setMenu] = useState<MenuItem[]>([]);
  const [counts, setCounts] = useState<Record<number, number>>({});
  const [myVotes, setMyVotes] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = () => {
    Promise.all([api.menu.all(), api.votes.counts()])
      .then(([m, c]) => { setMenu(m); setCounts(c); setLoading(false); })
      .catch(() => setLoading(false));
    if (isLoggedIn) {
      api.votes.myVotes(user!.email).then(setMyVotes).catch(() => {});
    }
  };

  useEffect(() => { loadData(); }, [isLoggedIn]);

  const handleVote = async (gerichtId: number) => {
    if (!isLoggedIn || myVotes.includes(gerichtId)) return;
    try {
      await api.votes.cast(gerichtId, user!.email);
      setMyVotes([...myVotes, gerichtId]);
      loadData();
    } catch (e) {
      alert("Abstimmung fehlgeschlagen: " + (e as Error).message);
    }
  };

  const totalVotes = Object.values(counts).reduce((s, c) => s + c, 0);
  const maxCount = Math.max(...Object.values(counts), 0);
  const top3 = [...menu]
    .map(m => ({ item: m, count: counts[m.id] || 0 }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 3)
    .filter(t => t.count > 0);

  const medals = ["🥇", "🥈", "🥉"];

  return (
    <div>
      <div className="bg-[#003a70] text-white p-6 shadow-lg">
        <h1 className="text-2xl">Wunschgericht-Voting</h1>
        <p className="text-sm text-blue-100">Bestimme den Speiseplan der kommenden Woche</p>
      </div>
      <div className="p-4 space-y-4">
        <div className="bg-blue-50 rounded-xl p-3 text-sm text-blue-900">
          {totalVotes} Stimmen insgesamt
        </div>

        {!isLoggedIn && (
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 flex items-center gap-2 text-amber-800">
            <Lock className="w-5 h-5" />
            <div>
              <p className="font-medium">Zum Abstimmen bitte anmelden.</p>
              <p className="text-xs">Die Ergebnisse sind für alle sichtbar.</p>
            </div>
          </div>
        )}

        {top3.length > 0 && (
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
            <div className="flex items-center gap-2 mb-3"><Trophy className="w-5 h-5 text-yellow-500" /><h2 className="font-bold">Top 3</h2></div>
            <div className="space-y-2">
              {top3.map((t, i) => (
                <div key={t.item.id} className="flex items-center gap-3">
                  <span className="text-2xl">{medals[i]}</span>
                  <div className="flex-1"><div className="font-medium">{t.item.name}</div><div className="text-xs text-gray-500">{t.count} Stimmen</div></div>
                </div>
              ))}
            </div>
          </div>
        )}

        {loading ? (
          <p className="text-gray-500">Lade Gerichte...</p>
        ) : (
          <div className="space-y-3">
            {menu.map(item => {
              const count = counts[item.id] || 0;
              const voted = myVotes.includes(item.id);
              const pct = maxCount > 0 ? (count / maxCount) * 100 : 0;
              return (
                <div key={item.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <h3 className="font-bold">{item.name}</h3>
                      <p className="text-sm text-gray-500">{item.beschreibung}</p>
                    </div>
                    <button
                      onClick={() => handleVote(item.id)}
                      disabled={!isLoggedIn || voted}
                      className={cn(
                        "flex items-center gap-1 px-3 py-1.5 rounded-full text-xs font-medium",
                        voted ? "bg-[#003a70] text-white" : isLoggedIn ? "bg-gray-100 text-gray-700 hover:bg-gray-200" : "bg-gray-100 text-gray-400 cursor-not-allowed"
                      )}
                    >
                      <ThumbsUp className="w-3 h-3" /> {voted ? "Abgestimmt" : "Stimmen"}
                    </button>
                  </div>
                  <div className="h-1.5 bg-gray-100 rounded-full">
                    <div className="h-full bg-[#003a70] rounded-full transition-all" style={{ width: `${pct}%` }} />
                  </div>
                  <div className="text-xs text-gray-500 mt-1">{count} Stimmen</div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
