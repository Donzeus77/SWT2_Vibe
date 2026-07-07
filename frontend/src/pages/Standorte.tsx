import { useState, useEffect } from "react";
import { api, type Mensa } from "../lib/api";
import { MapPin, Clock, ChevronDown, Star, Navigation as NavIcon } from "lucide-react";
import { cn } from "../lib/utils";

const auslastungConfig = {
  low: { label: "Wenig besucht", color: "green", bar: "25%" },
  medium: { label: "Mäßig besucht", color: "yellow", bar: "60%" },
  high: { label: "Stark besucht", color: "red", bar: "90%" },
};

export default function Standorte() {
  const [mensen, setMensen] = useState<Mensa[]>([]);
  const [loading, setLoading] = useState(true);
  const [campus, setCampus] = useState<"Alle" | "TU Dortmund" | "FH Dortmund">("Alle");
  const [expanded, setExpanded] = useState<number | null>(null);

  useEffect(() => {
    api.mensen.all()
      .then(data => { setMensen(data); setLoading(false); })
      .catch(() => setLoading(false));
  }, []);

  const filtered = campus === "Alle" ? mensen : mensen.filter(m => m.campus === campus);

  return (
    <div>
      <div className="bg-[#003a70] text-white p-6 shadow-lg">
        <h1 className="text-2xl">Standorte</h1>
        <p className="text-sm text-blue-100">Mensen & Öffnungszeiten</p>
      </div>
      <div className="px-4 pt-4 border-b border-gray-200 flex gap-4">
        {["Alle", "TU Dortmund", "FH Dortmund"].map(c => (
          <button
            key={c}
            onClick={() => setCampus(c as any)}
            className={cn(
              "pb-2 text-sm border-b-2",
              campus === c ? "border-[#003a70] text-[#003a70] font-medium" : "border-transparent text-gray-500"
            )}
          >
            {c}
          </button>
        ))}
      </div>
      <div className="p-4 space-y-3">
        {loading && <p className="text-gray-500">Lade Standorte...</p>}
        {filtered.map(mensa => {
          const cfg = auslastungConfig[mensa.auslastung as keyof typeof auslastungConfig] || auslastungConfig.low;
          const isOpen = expanded === mensa.id;
          return (
            <div key={mensa.id} className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
              <button
                onClick={() => setExpanded(isOpen ? null : mensa.id)}
                className="w-full p-4 text-left"
              >
                <div className="flex justify-between items-start">
                  <h3 className="font-bold text-gray-900">{mensa.name}</h3>
                  <ChevronDown className={cn("w-5 h-5 text-gray-400 transition", isOpen && "rotate-180")} />
                </div>
                <div className="mt-2 flex items-center gap-2">
                  <span className={cn(
                    "text-xs px-2 py-0.5 rounded-full",
                    cfg.color === "green" && "bg-green-100 text-green-700",
                    cfg.color === "yellow" && "bg-yellow-100 text-yellow-700",
                    cfg.color === "red" && "bg-red-100 text-red-700",
                  )}>
                    {cfg.label}
                  </span>
                  <div className="flex-1 h-1.5 bg-gray-100 rounded-full">
                    <div className={cn(
                      "h-full rounded-full",
                      cfg.color === "green" && "bg-green-500",
                      cfg.color === "yellow" && "bg-yellow-500",
                      cfg.color === "red" && "bg-red-500",
                    )} style={{ width: cfg.bar }} />
                  </div>
                </div>
                <div className="mt-2 flex items-center gap-2 text-sm text-gray-600">
                  <Clock className="w-4 h-4" /> {mensa.oeffnungszeiten}
                </div>
                <div className="mt-1 flex items-center gap-2 text-sm text-gray-600">
                  <MapPin className="w-4 h-4" /> {mensa.adresse}
                </div>
              </button>
              {isOpen && (
                <div className="px-4 pb-4 border-t border-gray-100 pt-3">
                  <div className="flex gap-2">
                    <a
                      href={`https://maps.google.com/?q=${encodeURIComponent(mensa.adresse)}`}
                      target="_blank"
                      rel="noreferrer"
                      className="flex-1 bg-[#003a70] text-white text-sm py-2 rounded-lg flex items-center justify-center gap-1 hover:bg-[#002a52]"
                    >
                      <NavIcon className="w-4 h-4" /> Route
                    </a>
                    <a
                      href="/"
                      className="flex-1 border border-gray-300 text-gray-900 text-sm py-2 rounded-lg flex items-center justify-center hover:bg-gray-50"
                    >
                      Speiseplan
                    </a>
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
