import { Link, useLocation } from "react-router";
import { Home, MapPin, ShoppingBag, ThumbsUp, User } from "lucide-react";
import { cn } from "../lib/utils";
import { useCart } from "../context/CartContext";

const tabs = [
  { path: "/", label: "Speiseplan", icon: Home },
  { path: "/standorte", label: "Standorte", icon: MapPin },
  { path: "/bestellungen", label: "Bestellen", icon: ShoppingBag },
  { path: "/abstimmung", label: "Voting", icon: ThumbsUp },
  { path: "/profil", label: "Profil", icon: User },
];

export default function Navigation() {
  const location = useLocation();
  const { totalItems } = useCart();
  return (
    <nav className="absolute bottom-0 left-0 right-0 h-16 bg-white border-t border-gray-200 flex items-center justify-around">
      {tabs.map(({ path, label, icon: Icon }) => {
        const active = location.pathname === path;
        return (
          <Link
            key={path}
            to={path}
            className={cn(
              "flex flex-col items-center gap-1 text-xs relative",
              active ? "text-[#003a70] font-medium" : "text-gray-500"
            )}
          >
            <div className="relative">
              <Icon className="w-5 h-5" />
              {path === "/bestellungen" && totalItems > 0 && (
                <span className="absolute -top-1 -right-2 bg-[#003a70] text-white text-[10px] rounded-full h-4 w-4 flex items-center justify-center">
                  {totalItems}
                </span>
              )}
            </div>
            <span>{label}</span>
          </Link>
        );
      })}
    </nav>
  );
}
