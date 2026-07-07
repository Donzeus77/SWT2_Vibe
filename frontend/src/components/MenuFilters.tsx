import { Sheet, SheetTrigger, SheetContent, SheetHeader, SheetTitle } from "./ui/sheet";
import { Checkbox } from "./ui/checkbox";
import { Label } from "./ui/label";
import { Button } from "./ui/button";
import { Filter } from "lucide-react";
import { Badge } from "./ui/badge";

export interface Filters {
  dietary: string[];
  allergens: string[];
}

const dietaryOptions = ["VEGAN", "VEGETARISCH", "HALAL"];
const allergenOptions = ["GLUTEN", "MILCH", "EI", "FISCH", "SOJA", "NUESSE", "ERDNUSS", "SELLERIE", "SENF", "SESAM"];

export default function MenuFilters({
  filters,
  setFilters,
}: {
  filters: Filters;
  setFilters: (f: Filters) => void;
}) {
  const activeCount = filters.dietary.length + filters.allergens.length;

  const toggleDietary = (d: string) => {
    setFilters({
      ...filters,
      dietary: filters.dietary.includes(d)
        ? filters.dietary.filter(x => x !== d)
        : [...filters.dietary, d],
    });
  };

  const toggleAllergen = (a: string) => {
    setFilters({
      ...filters,
      allergens: filters.allergens.includes(a)
        ? filters.allergens.filter(x => x !== a)
        : [...filters.allergens, a],
    });
  };

  return (
    <Sheet>
      <SheetTrigger asChild>
        <Button variant="outline" size="sm" className="relative">
          <Filter className="w-4 h-4 mr-1" /> Filter
          {activeCount > 0 && (
            <span className="absolute -top-1 -right-1 bg-yellow-400 text-yellow-900 text-[10px] rounded-full h-5 w-5 flex items-center justify-center font-bold">
              {activeCount}
            </span>
          )}
        </Button>
      </SheetTrigger>
      <SheetContent side="bottom">
        <SheetHeader>
          <SheetTitle>Filter</SheetTitle>
        </SheetHeader>
        <div className="p-6 space-y-6">
          <div>
            <h4 className="font-medium mb-3">Ernährungsform</h4>
            <div className="space-y-2">
              {dietaryOptions.map(d => (
                <div key={d} className="flex items-center gap-2">
                  <Checkbox id={`d-${d}`} checked={filters.dietary.includes(d)} onCheckedChange={() => toggleDietary(d)} />
                  <Label htmlFor={`d-${d}`}>{d}</Label>
                </div>
              ))}
            </div>
          </div>
          <div>
            <h4 className="font-medium mb-3">Allergene ausschließen</h4>
            <div className="flex flex-wrap gap-2">
              {allergenOptions.map(a => (
                <Badge
                  key={a}
                  variant={filters.allergens.includes(a) ? "red" : "outline"}
                  className="cursor-pointer"
                  onClick={() => toggleAllergen(a)}
                >
                  {filters.allergens.includes(a) ? `Ohne ${a}` : a}
                </Badge>
              ))}
            </div>
          </div>
          <Button
            variant="outline"
            className="w-full"
            onClick={() => setFilters({ dietary: [], allergens: [] })}
          >
            Alle Filter zurücksetzen
          </Button>
        </div>
      </SheetContent>
    </Sheet>
  );
}
