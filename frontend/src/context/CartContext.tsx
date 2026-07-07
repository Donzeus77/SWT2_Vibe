import { createContext, useContext, useState, type ReactNode } from "react";
import type { MenuItem } from "../lib/api";

interface CartItem {
  item: MenuItem;
  anzahl: number;
}

interface CartContextType {
  items: CartItem[];
  addItem: (item: MenuItem) => void;
  removeItem: (gerichtId: number) => void;
  updateQuantity: (gerichtId: number, anzahl: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

const CartContext = createContext<CartContextType | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>([]);

  const addItem = (item: MenuItem) => {
    setItems(prev => {
      const existing = prev.find(i => i.item.id === item.id);
      if (existing) {
        return prev.map(i => i.item.id === item.id ? { ...i, anzahl: i.anzahl + 1 } : i);
      }
      return [...prev, { item, anzahl: 1 }];
    });
  };

  const removeItem = (gerichtId: number) => {
    setItems(prev => prev.filter(i => i.item.id !== gerichtId));
  };

  const updateQuantity = (gerichtId: number, anzahl: number) => {
    if (anzahl <= 0) { removeItem(gerichtId); return; }
    setItems(prev => prev.map(i => i.item.id === gerichtId ? { ...i, anzahl } : i));
  };

  const clearCart = () => setItems([]);

  const totalItems = items.reduce((s, i) => s + i.anzahl, 0);
  const totalPrice = items.reduce((s, i) => s + i.anzahl * i.item.preisStudent, 0);

  return (
    <CartContext.Provider value={{ items, addItem, removeItem, updateQuantity, clearCart, totalItems, totalPrice }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}
