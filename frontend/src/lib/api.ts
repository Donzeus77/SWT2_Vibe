const BASE = "/api";

function getToken(): string | null {
  return localStorage.getItem("authToken");
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) || {}),
  };
  const token = getToken();
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  const res = await fetch(`${BASE}${path}`, { ...options, headers });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: "Serverfehler" }));
    throw new Error(err.error || "Serverfehler");
  }
  return res.json() as Promise<T>;
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
      request<{ token: string; user: AuthUser }>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      }),
    register: (email: string, password: string, vorname: string, nachname: string) =>
      request<{ token: string; user: AuthUser }>("/auth/register", {
        method: "POST",
        body: JSON.stringify({ email, password, vorname, nachname }),
      }),
    me: () => request<AuthUser>("/auth/me"),
  },
  menu: {
    all: () => request<MenuItem[]>("/menu"),
    byId: (id: number) => request<MenuItem>(`/menu/${id}`),
  },
  mensen: {
    all: () => request<Mensa[]>("/mensen"),
  },
  orders: {
    all: () => request<Order[]>("/orders"),
    create: (data: { items: { gerichtId: number; anzahl: number }[]; pickupTime: string }) =>
      request<Order>("/orders", { method: "POST", body: JSON.stringify(data) }),
    updateStatus: (id: number, status: string) =>
      request<Order>(`/orders/${id}`, { method: "PATCH", body: JSON.stringify({ status }) }),
  },
  votes: {
    counts: () => request<Record<number, number>>("/votes"),
    myVotes: () => request<number[]>("/votes/my"),
    cast: (gerichtId: number) =>
      request<void>(`/votes/${gerichtId}`, { method: "POST" }),
  },
  profil: {
    preferences: () => request<{ dietary: string[]; allergens: string[] }>("/profil/preferences"),
    updatePreferences: (data: { dietary: string[]; allergens: string[] }) =>
      request<void>("/profil/preferences", { method: "PUT", body: JSON.stringify(data) }),
  },
};
