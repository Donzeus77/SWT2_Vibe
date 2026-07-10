import { useState, useEffect } from "react";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { api, type Order } from "../lib/api";
import { Button } from "../components/ui/button";
import { Trash2, Plus, Minus, CreditCard, CheckCircle, QrCode, ShoppingBag, Package } from "lucide-react";
import { cn } from "../lib/utils";

type Step = "cart" | "payment" | "pickup" | "processing" | "success";
type Tab = "cart" | "history";

export default function Bestellungen() {
  const { items, removeItem, updateQuantity, totalPrice, clearCart, totalItems } = useCart();
  const { user, isLoggedIn } = useAuth();
  const [tab, setTab] = useState<Tab>("cart");
  const [step, setStep] = useState<Step>("cart");
  const [paymentMethod, setPaymentMethod] = useState<"card" | "paypal">("card");
  const [pickupTime, setPickupTime] = useState<string>("");
  const [orders, setOrders] = useState<Order[]>([]);
  const [lastOrder, setLastOrder] = useState<Order | null>(null);

  const loadOrders = () => {
    if (isLoggedIn) {
      api.orders.all(user!.email).then(setOrders).catch(() => {});
    }
  };

  useEffect(() => { loadOrders(); }, [isLoggedIn]);

  const generatePickupSlots = () => {
    const slots: string[] = [];
    const now = new Date();
    const firstSlot = new Date(now);
    firstSlot.setMinutes(firstSlot.getMinutes() + 15);
    firstSlot.setMinutes(Math.ceil(firstSlot.getMinutes() / 5) * 5, 0, 0);
    const opening = new Date(firstSlot);
    opening.setHours(11, 30, 0, 0);
    const closing = new Date(firstSlot);
    closing.setHours(14, 15, 0, 0);

    if (firstSlot > closing) {
      firstSlot.setDate(firstSlot.getDate() + 1);
      firstSlot.setHours(11, 30, 0, 0);
    } else if (firstSlot < opening) {
      firstSlot.setHours(11, 30, 0, 0);
    }

    for (let i = 0; i < 8; i++) {
      const slot = new Date(firstSlot.getTime() + i * 10 * 60000);
      slots.push(`${String(slot.getHours()).padStart(2, "0")}:${String(slot.getMinutes()).padStart(2, "0")}`);
    }
    return slots;
  };

  const handleCheckout = async () => {
    setStep("processing");
    try {
      const order = await api.orders.create({ email: user!.email,
        items: items.map(i => ({ gerichtId: i.item.id, name: i.item.name, anzahl: i.anzahl, preis: i.item.preisStudent })),
        pickupTime,
      });
      setLastOrder(order);
      clearCart();
      setStep("success");
      loadOrders();
    } catch (e) {
      setStep("cart");
      alert("Bestellung fehlgeschlagen: " + (e as Error).message);
    }
  };

  const statusBadge = (status: string) => {
    if (status === "OFFEN") return <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">In Bearbeitung</span>;
    if (status === "BEZAHLT") return <span className="text-xs bg-green-100 text-green-700 px-2 py-0.5 rounded-full">Bereit</span>;
    if (status === "ABGEHOLT") return <span className="text-xs bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full">Abgeholt</span>;
    return <span className="text-xs bg-gray-100 text-gray-700 px-2 py-0.5 rounded-full">{status}</span>;
  };

  const FakeQR = ({ code }: { code: string }) => {
    const cells = [];
    let hash = 0;
    for (let i = 0; i < code.length; i++) hash = (hash * 31 + code.charCodeAt(i)) & 0xffffffff;
    for (let i = 0; i < 49; i++) {
      hash = (hash * 1103515245 + 12345) & 0x7fffffff;
      cells.push((hash >> 16) & 1);
    }
    return (
      <div className="grid grid-cols-7 gap-0.5 w-32 h-32 bg-white p-1 border-2 border-gray-900 rounded">
        {cells.map((c, i) => (
          <div key={i} className={c ? "bg-gray-900" : "bg-white"} />
        ))}
      </div>
    );
  };

  if (!isLoggedIn) {
    return (
      <div>
        <div className="bg-[#003a70] text-white p-6 shadow-lg">
          <h1 className="text-2xl">Bestellen</h1>
        </div>
        <div className="p-4 text-center py-12">
          <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <p className="text-gray-600 mb-4">Bitte melde dich an, um zu bestellen.</p>
          <a href="/profil"><Button>Zum Login</Button></a>
        </div>
      </div>
    );
  }

  if (step === "processing") {
    return (
      <div className="bg-[#003a70] min-h-screen flex flex-col items-center justify-center text-white">
        <div className="w-12 h-12 border-4 border-white border-t-transparent rounded-full animate-spin" />
        <p className="mt-4">Zahlung wird verarbeitet...</p>
      </div>
    );
  }

  if (step === "success" && lastOrder) {
    return (
      <div>
        <div className="bg-[#003a70] text-white p-6 shadow-lg flex items-center gap-2">
          <CheckCircle className="w-6 h-6 text-green-400" />
          <h1 className="text-2xl">Bestellung bestätigt</h1>
        </div>
        <div className="p-4">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 text-center">
            <FakeQR code={lastOrder.code} />
            <div className="text-3xl font-bold text-[#003a70] my-4 tracking-wider">{lastOrder.code}</div>
            <p className="text-gray-600">Abholung um {lastOrder.pickupTime} Uhr</p>
          </div>
          <div className="mt-4 flex gap-2">
            <Button variant="outline" className="flex-1" onClick={() => { setStep("cart"); setTab("history"); }}>
              <Package className="w-4 h-4 mr-1" /> Meine Bestellungen
            </Button>
            <Button className="flex-1" onClick={() => setStep("cart")}>Neue Bestellung</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="bg-[#003a70] text-white p-6 shadow-lg">
        <h1 className="text-2xl">Bestellen</h1>
        <p className="text-sm text-blue-100">{totalItems} Artikel im Warenkorb</p>
      </div>
      <div className="px-4 pt-4 border-b border-gray-200 flex gap-4">
        <button onClick={() => { setTab("cart"); setStep("cart"); }} className={cn("pb-2 text-sm border-b-2", tab === "cart" ? "border-[#003a70] text-[#003a70] font-medium" : "border-transparent text-gray-500")}>Warenkorb</button>
        <button onClick={() => setTab("history")} className={cn("pb-2 text-sm border-b-2", tab === "history" ? "border-[#003a70] text-[#003a70] font-medium" : "border-transparent text-gray-500")}>Meine Bestellungen ({orders.length})</button>
      </div>

      {tab === "cart" && (
        <div className="p-4 space-y-4">
          {items.length === 0 ? (
            <div className="text-center py-12">
              <ShoppingBag className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-600 mb-4">Dein Warenkorb ist leer.</p>
              <a href="/"><Button>Zum Speiseplan</Button></a>
            </div>
          ) : (
            <>
              {step === "cart" && (
                <>
                  {items.map(({ item, anzahl }) => (
                    <div key={item.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
                      <div className="flex justify-between">
                        <div className="flex-1">
                          <h3 className="font-bold">{item.name}</h3>
                          <p className="text-sm text-gray-500">{(item.preisStudent).toFixed(2)} €</p>
                        </div>
                        <button onClick={() => removeItem(item.id)} className="text-gray-400 hover:text-red-500">
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                      <div className="flex items-center gap-3 mt-2">
                        <button onClick={() => updateQuantity(item.id, anzahl - 1)} className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center"><Minus className="w-4 h-4" /></button>
                        <span className="font-medium">{anzahl}</span>
                        <button onClick={() => updateQuantity(item.id, anzahl + 1)} className="w-7 h-7 rounded-full border border-gray-300 flex items-center justify-center"><Plus className="w-4 h-4" /></button>
                        <span className="ml-auto font-bold text-[#003a70]">{(anzahl * item.preisStudent).toFixed(2)} €</span>
                      </div>
                    </div>
                  ))}
                  <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 space-y-2">
                    <div className="flex justify-between text-sm"><span>Zwischensumme</span><span>{totalPrice.toFixed(2)} €</span></div>
                    <div className="flex justify-between text-sm"><span>Abholung</span><span>Gratis</span></div>
                    <div className="flex justify-between font-bold text-[#003a70] border-t pt-2"><span>Gesamt</span><span>{totalPrice.toFixed(2)} €</span></div>
                  </div>
                  <Button className="w-full" onClick={() => setStep("payment")}>Weiter zur Zahlung</Button>
                </>
              )}

              {step === "payment" && (
                <>
                  <h2 className="font-bold">Zahlungsmethode</h2>
                  <div className={cn("bg-white rounded-2xl border-2 p-4 flex items-center gap-3 cursor-pointer", paymentMethod === "card" ? "border-[#003a70]" : "border-gray-100")} onClick={() => setPaymentMethod("card")}>
                    <CreditCard className="w-6 h-6 text-[#003a70]" />
                    <div><div className="font-medium">Kredit-/Debitkarte</div><div className="text-xs text-gray-500">•••• 4242</div></div>
                  </div>
                  <div className={cn("bg-white rounded-2xl border-2 p-4 flex items-center gap-3 cursor-pointer", paymentMethod === "paypal" ? "border-[#003a70]" : "border-gray-100")} onClick={() => setPaymentMethod("paypal")}>
                    <div className="w-6 h-6 bg-[#003087] text-white text-xs font-bold rounded flex items-center justify-center">PP</div>
                    <div className="font-medium">PayPal</div>
                  </div>
                  <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4 flex justify-between font-bold text-[#003a70]"><span>Gesamt</span><span>{totalPrice.toFixed(2)} €</span></div>
                  <div className="flex gap-2">
                    <Button variant="outline" className="flex-1" onClick={() => setStep("cart")}>Zurück</Button>
                    <Button className="flex-1" onClick={() => setStep("pickup")}>Weiter zur Abholzeit</Button>
                  </div>
                </>
              )}

              {step === "pickup" && (
                <>
                  <h2 className="font-bold">Abholzeit wählen</h2>
                  <div className="grid grid-cols-2 gap-2">
                    {generatePickupSlots().map(slot => (
                      <button key={slot} onClick={() => setPickupTime(slot)} className={cn("py-3 rounded-lg border-2 text-sm font-medium", pickupTime === slot ? "border-[#003a70] bg-blue-50 text-[#003a70]" : "border-gray-200 text-gray-700")}>
                        {slot}
                      </button>
                    ))}
                  </div>
                  {pickupTime && <div className="text-center text-2xl font-bold text-[#003a70]">{pickupTime} Uhr</div>}
                  <div className="flex gap-2">
                    <Button variant="outline" className="flex-1" onClick={() => setStep("payment")}>Zurück</Button>
                    <Button className="flex-1" disabled={!pickupTime} onClick={handleCheckout}>{totalPrice.toFixed(2)} € bezahlen</Button>
                  </div>
                </>
              )}
            </>
          )}
        </div>
      )}

      {tab === "history" && (
        <div className="p-4 space-y-3">
          {orders.length === 0 ? (
            <p className="text-gray-500 text-center py-12">Noch keine Bestellungen.</p>
          ) : (
            orders.map(order => (
              <div key={order.id} className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
                <div className="flex justify-between items-start">
                  <div>
                    <div className="font-bold">Code: {order.code}</div>
                    <div className="text-sm text-gray-500">Abholung: {order.pickupTime} Uhr</div>
                  </div>
                  {statusBadge(order.status)}
                </div>
                <div className="mt-2 space-y-1">
                  {order.items?.map((it, i) => (
                    <div key={i} className="text-sm flex justify-between"><span>{it.anzahl}× {it.name}</span><span>{(it.preis * it.anzahl).toFixed(2)} €</span></div>
                  ))}
                </div>
                <div className="mt-2 border-t pt-2 flex justify-between font-bold text-[#003a70]"><span>Gesamt</span><span>{order.total.toFixed(2)} €</span></div>
                {order.status !== "ABGEHOLT" && (
                  <button onClick={() => { api.orders.updateStatus(order.id, "ABGEHOLT").then(loadOrders); }} className="mt-2 text-sm text-[#003a70] underline">Als abgeholt markieren</button>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
