import { Plus, Check, Leaf, Flame } from "lucide-react";
import { useState } from "react";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import type { MenuItem } from "../lib/api";

const allergenColors: Record<string, string> = {
  GLUTEN: "amber", MILCH: "blue", EI: "yellow", FISCH: "cyan",
  SOJA: "lime", NUESSE: "orange", ERDNUSS: "red", SELLERIE: "green",
  SENF: "purple", SESAM: "blue", SULFITE: "purple", KREBSTIERE: "red",
  LUPINEN: "lime", WEICHTIERE: "cyan",
};

export default function MenuCard({ item }: { item: MenuItem }) {
  const { addItem } = useCart();
  const { user } = useAuth();
  const [added, setAdded] = useState(false);
  const price = user?.type === "gast" ? item.preisGast : item.preisStudent;
  const priceLabel = user?.type === "gast" ? "Gäste" : "Studierende";

  const handleAdd = () => {
    addItem(item);
    setAdded(true);
    setTimeout(() => setAdded(false), 1500);
  };

  return (
    <div className="rounded-2xl border border-gray-100 bg-white shadow-sm p-4">
      <div className="flex justify-between items-start mb-1">
        <h3 className="font-bold text-gray-900">{item.name}</h3>
        <div className="text-right">
          <div className="text-[#003a70] font-bold">{price.toFixed(2)} €</div>
          <div className="text-[10px] text-gray-500">{priceLabel}</div>
        </div>
      </div>
      <p className="text-sm text-gray-600 mb-2">{item.beschreibung}</p>
      <div className="flex flex-wrap gap-1.5 mb-3">
        {item.tags?.map((tag) => (
          <Badge key={tag} variant="green" className="flex items-center gap-1">
            <Leaf className="w-3 h-3" /> {tag}
          </Badge>
        ))}
        {item.allergene?.map((a) => (
          <Badge key={a} variant={(allergenColors[a] as any) || "secondary"}>
            {a}
          </Badge>
        ))}
      </div>
      <Button
        onClick={handleAdd}
        className="w-full"
        variant={added ? "green" : "default"}
        style={added ? { backgroundColor: "#16a34a" } : undefined}
      >
        {added ? <><Check className="w-4 h-4 mr-1" /> Hinzugefügt</> : <><Plus className="w-4 h-4 mr-1" /> Zum Warenkorb</>}
      </Button>
    </div>
  );
}
