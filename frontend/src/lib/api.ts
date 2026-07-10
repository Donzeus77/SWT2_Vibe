const BASE = "/api";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  };
  const res = await fetch(`${BASE}${path}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Serverfehler" }));
    throw new Error(err.error || "Serverfehler");
  }
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}

export interface MenuItem {
  id: number;
  name: string;
  beschreibung: string;
  preisStudent: number;
  preisGast: number;
  allergene: string[];
  tags: string[];
  typ: string;
}

export interface Mensa {
  id: number;
  name: string;
  campus: string;
  adresse: string;
  oeffnungszeiten: string;
  auslastung: "low" | "medium" | "high";
}

export interface Order {
  id: number;
  items: { gerichtId: number; name: string; anzahl: number; preis: number }[];
  total: number;
  status: string;
  pickupTime: string;
  code: string;
}

export interface AuthUser {
  id: number;
  email: string;
  vorname: string;
  nachname: string;
  type: "student" | "mitarbeiter" | "gast";
}

export const api = {
  auth: {
    login: (email: string, password: string) =>
      request<AuthUser>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      }),
    register: (email: string, password: string) =>
      request<AuthUser>("/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      }),
  },
  menu: {
    all: () => request<MenuItem[]>("/menu"),
    byId: (id: number) => request<MenuItem>(`/menu/${id}`),
  },
  mensen: {
    all: () => request<Mensa[]>("/mensen"),
  },
  orders: {
    all: (email: string) => request<Order[]>(`/orders?email=${encodeURIComponent(email)}`),
    create: (data: { email: string; items: { gerichtId: number; name: string; anzahl: number; preis: number }[]; pickupTime: string }) =>
      request<Order>("/orders", { method: "POST", body: JSON.stringify(data) }),
    updateStatus: (id: number, status: string) =>
      request<Order>(`/orders/${id}`, { method: "PATCH", body: JSON.stringify({ status }) }),
  },
  votes: {
    counts: () => request<Record<number, number>>("/votes"),
    myVotes: (email: string) => request<number[]>(`/votes/my?email=${encodeURIComponent(email)}`),
    cast: (gerichtId: number, email: string) =>
      request<void>(`/votes/${gerichtId}?email=${encodeURIComponent(email)}`, { method: "POST" }),
  },
  profil: {
    preferences: (email: string) => request<{ dietary: string[]; allergens: string[] }>(`/profil/preferences?email=${encodeURIComponent(email)}`),
    updatePreferences: (email: string, data: { dietary: string[]; allergens: string[] }) =>
      request<void>(`/profil/preferences?email=${encodeURIComponent(email)}`, { method: "PUT", body: JSON.stringify(data) }),
  },
};
