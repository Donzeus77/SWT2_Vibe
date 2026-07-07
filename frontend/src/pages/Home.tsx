import { useState, useEffect } from "react";
import { api, type MenuItem } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import MenuCard from "../components/MenuCard";
import MenuFilters, { type Filters } from "../components/MenuFilters";
import { Calendar, CalendarDays } from "lucide-react";

export default function Home() {
  const { user } = useAuth();
  const [menu, setMenu] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<Filters>({ dietary: [], allergens: [] });
  const [view, setView] = useState<"day" | "week">("day");

  useEffect(() => {
    api.menu.all()
      .then(data => { setMenu(data); setLoading(false); })
      .catch(e => { setError(e.message); setLoading(false); });
  }, []);

  const filtered = menu.filter(item => {
    if (filters.dietary.length > 0 && !filters.dietary.some(d => item.tags?.includes(d))) return false;
    if (filters.allergens.length > 0 && filters.allergens.some(a => item.allergene?.includes(a))) return false;
    return true;
  });

  const hauptspeisen = filtered.filter(i => i.typ === "Hauptspeise");
  const beilagen = filtered.filter(i => i.typ === "Beilage");

  return (
    <div>
      <div className="bg-[#003a70] text-white p-6 shadow-lg">
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-2xl">Speiseplan</h1>
            <p className="text-sm text-blue-100">Mensa Dortmund</p>
          </div>
          <button
            onClick={() => setView(v => v === "day" ? "week" : "day")}
            className="text-white p-2 rounded-lg hover:bg-[#002a52]"
          >
            {view === "day" ? <Calendar className="w-5 h-5" /> : <CalendarDays className="w-5 h-5" />}
          </button>
        </div>
        <div className="mt-3">
          <MenuFilters filters={filters} setFilters={setFilters} />
        </div>
      </div>
      <div className="p-4 space-y-4">
        {loading && <p className="text-gray-500">Lade Speiseplan...</p>}
        {error && <p className="text-red-500">{error}</p>}
        {!loading && filtered.length === 0 && (
          <p className="text-gray-500 text-center py-8">Keine Gerichte gefunden.</p>
        )}
        {!loading && hauptspeisen.length > 0 && (
          <div>
            <h2 className="text-xs uppercase text-gray-500 font-medium mb-2">Hauptgerichte</h2>
            <div className="space-y-3">
              {hauptspeisen.map(item => <MenuCard key={item.id} item={item} />)}
            </div>
          </div>
        )}
        {!loading && beilagen.length > 0 && (
          <div>
            <h2 className="text-xs uppercase text-gray-500 font-medium mb-2">Beilagen</h2>
            <div className="space-y-3">
              {beilagen.map(item => <MenuCard key={item.id} item={item} />)}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
