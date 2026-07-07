import { useState, useEffect, useMemo } from "react";
import { api, type MenuItem } from "../lib/api";
import { useAuth } from "../context/AuthContext";
import MenuCard from "../components/MenuCard";
import MenuFilters, { type Filters } from "../components/MenuFilters";
import { Calendar, CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";
import { format, addDays, startOfWeek, addWeeks, isSameDay, isToday, isTomorrow, getISOWeek, getISOWeekYear } from "date-fns";
import { de } from "date-fns/locale";
import { cn } from "../lib/utils";

export default function Home() {
  const { user } = useAuth();
  const [menu, setMenu] = useState<MenuItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filters, setFilters] = useState<Filters>({ dietary: [], allergens: [] });
  const [view, setView] = useState<"day" | "week">("day");
  const [refDate, setRefDate] = useState(new Date());

  useEffect(() => {
    api.menu.all()
      .then(data => { setMenu(data); setLoading(false); })
      .catch(e => { setError(e.message); setLoading(false); });
  }, []);

  const filtered = useMemo(() => menu.filter(item => {
    if (filters.dietary.length > 0 && !filters.dietary.some(d => item.tags?.includes(d))) return false;
    if (filters.allergens.length > 0 && filters.allergens.some(a => item.allergene?.includes(a))) return false;
    return true;
  }), [menu, filters]);

  const hauptspeisen = filtered.filter(i => i.typ === "Hauptspeise");
  const beilagen = filtered.filter(i => i.typ === "Beilage");

  const isWeekend = (d: Date) => d.getDay() === 0 || d.getDay() === 6;

  const dayLabel = (d: Date) => {
    if (isToday(d)) return "Heute";
    if (isTomorrow(d)) return "Morgen";
    return format(d, "EEEE", { locale: de });
  };

  const weekStart = startOfWeek(refDate, { weekStartsOn: 1 });
  const weekDays = Array.from({ length: 5 }, (_, i) => addDays(weekStart, i));

  const goPrev = () => {
    if (view === "day") setRefDate(d => {
      let n = addDays(d, -1);
      while (isWeekend(n)) n = addDays(n, -1);
      return n;
    });
    else setRefDate(d => addWeeks(d, -1));
  };

  const goNext = () => {
    if (view === "day") setRefDate(d => {
      let n = addDays(d, 1);
      while (isWeekend(n)) n = addDays(n, 1);
      return n;
    });
    else setRefDate(d => addWeeks(d, 1));
  };

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
            title={view === "day" ? "Wochenansicht" : "Tagesansicht"}
          >
            {view === "day" ? <Calendar className="w-5 h-5" /> : <CalendarDays className="w-5 h-5" />}
          </button>
        </div>
        <div className="mt-3">
          <MenuFilters filters={filters} setFilters={setFilters} />
        </div>
      </div>

      <div className="px-4 py-3 flex items-center justify-between border-b border-gray-100">
        <button onClick={goPrev} className="p-2 rounded-lg hover:bg-gray-100">
          <ChevronLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div className="text-center">
          {view === "day" ? (
            <>
              <div className="font-bold text-[#003a70]">{dayLabel(refDate)}</div>
              <div className="text-xs text-gray-500">{format(refDate, "dd. MMMM yyyy", { locale: de })}</div>
            </>
          ) : (
            <>
              <div className="font-bold text-[#003a70]">KW {getISOWeek(refDate)}</div>
              <div className="text-xs text-gray-500">{format(weekStart, "dd.MM.", { locale: de })} – {format(addDays(weekStart, 4), "dd.MM.yyyy", { locale: de })}</div>
            </>
          )}
        </div>
        <button onClick={goNext} className="p-2 rounded-lg hover:bg-gray-100">
          <ChevronRight className="w-5 h-5 text-gray-600" />
        </button>
      </div>

      <div className="p-4 space-y-4">
        {loading && <p className="text-gray-500">Lade Speiseplan...</p>}
        {error && <p className="text-red-500">{error}</p>}

        {view === "day" ? (
          isWeekend(refDate) ? (
            <div className="text-center py-12">
              <p className="text-gray-500 font-medium">Wochenende</p>
              <p className="text-sm text-gray-400 mt-1">Die Mensa hat am Wochenende geschlossen.</p>
            </div>
          ) : !loading && (
            <>
              {filtered.length === 0 && (
                <p className="text-gray-500 text-center py-8">Keine Gerichte gefunden.</p>
              )}
              {hauptspeisen.length > 0 && (
                <div>
                  <h2 className="text-xs uppercase text-gray-500 font-medium mb-2">Hauptgerichte</h2>
                  <div className="space-y-3">
                    {hauptspeisen.map(item => <MenuCard key={item.id} item={item} />)}
                  </div>
                </div>
              )}
              {beilagen.length > 0 && (
                <div>
                  <h2 className="text-xs uppercase text-gray-500 font-medium mb-2">Beilagen</h2>
                  <div className="space-y-3">
                    {beilagen.map(item => <MenuCard key={item.id} item={item} />)}
                  </div>
                </div>
              )}
            </>
          )
        ) : (
          !loading && weekDays.map((day) => {
            const today = isToday(day);
            return (
              <div key={day.toISOString()} className={cn("rounded-2xl border bg-white shadow-sm overflow-hidden", today ? "border-[#003a70]" : "border-gray-100")}>
                <div className={cn("px-4 py-2 flex justify-between items-center", today ? "bg-[#003a70] text-white" : "bg-gray-50")}>
                  <div className="font-medium">{format(day, "EEEE", { locale: de })}</div>
                  <div className={cn("text-xs", today ? "text-blue-100" : "text-gray-500")}>{format(day, "dd.MM.", { locale: de })}</div>
                </div>
                <div className="p-3 space-y-2">
                  {hauptspeisen.slice(0, 3).map(item => (
                    <div key={item.id} className="text-sm">
                      <div className="font-medium text-gray-900">{item.name}</div>
                      <div className={cn("text-xs", today ? "text-[#003a70]" : "text-gray-500")}>{item.preisStudent.toFixed(2)} €</div>
                    </div>
                  ))}
                  {beilagen.length > 0 && (
                    <div className="text-xs text-gray-500 pt-1 border-t border-gray-100">
                      Beilagen: {beilagen.map(b => b.name).join(", ")}
                    </div>
                  )}
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
