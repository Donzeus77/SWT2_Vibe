import { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import { api } from "../lib/api";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Badge } from "../components/ui/badge";
import { Mail, Lock, Eye, EyeOff, User, LogOut, Leaf, Star } from "lucide-react";
import { cn } from "../lib/utils";

type View = "landing" | "login" | "register" | "verify";

export default function Profil() {
  const { user, isLoggedIn, login, logout } = useAuth();
  const [view, setView] = useState<View>("landing");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPw, setShowPw] = useState(false);
  const [vorname, setVorname] = useState("");
  const [nachname, setNachname] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [prefs, setPrefs] = useState<{ dietary: string[]; allergens: string[] }>({ dietary: [], allergens: [] });

  useEffect(() => {
    if (isLoggedIn) {
      api.profil.preferences(user!.email).then(setPrefs).catch(() => {});
    }
  }, [isLoggedIn]);

  const validDomains = ["stud.tu-dortmund.de", "stud.fh-dortmund.de", "tu-dortmund.de", "fh-dortmund.de"];
  const validateEmail = (e: string) => validDomains.some(d => e.endsWith("@" + d) || e.endsWith("." + d));

  const handleLogin = async () => {
    setError(null); setLoading(true);
    try {
      const res = await api.auth.login(email, password);
      login(res);
    } catch { setError("Falsche E-Mail oder Passwort"); }
    setLoading(false);
  };

  const handleRegister = async () => {
    setError(null); setLoading(true);
    try {
      const res = await api.auth.register(email, password, vorname, nachname);
      login(res);
    } catch { setError("Registrierung fehlgeschlagen (E-Mail schon vorhanden?)"); }
    setLoading(false);
  };

  const togglePref = (type: "dietary" | "allergens", value: string) => {
    const list = prefs[type];
    const newList = list.includes(value) ? list.filter(x => x !== value) : [...list, value];
    const newPrefs = { ...prefs, [type]: newList };
    setPrefs(newPrefs);
    if (user) api.profil.updatePreferences(user.email, newPrefs).catch(() => {});
  };

  if (isLoggedIn && user) {
    const priceLabel = user.type === "student" ? "Studierendenpreis aktiv" : user.type === "mitarbeiter" ? "Bediensteten-Preis aktiv" : "Gastpreis aktiv";
    return (
      <div>
        <div className="bg-[#003a70] text-white p-6 shadow-lg flex items-center gap-3">
          <div className="w-12 h-12 bg-white text-[#003a70] rounded-full flex items-center justify-center font-bold text-lg">
            {user.vorname[0]}{user.nachname[0]}
          </div>
          <div>
            <h1 className="text-xl">{user.vorname} {user.nachname}</h1>
            <p className="text-sm text-blue-100">{user.email}</p>
            <Badge variant={user.type === "student" ? "blue" : user.type === "mitarbeiter" ? "yellow" : "secondary"} className="mt-1">{priceLabel}</Badge>
          </div>
        </div>
        <div className="p-4 space-y-4">
          <div className="bg-white rounded-2xl border border-gray-100 shadow-sm p-4">
            <div className="flex items-center gap-2 mb-3"><Leaf className="w-5 h-5 text-green-600" /><h2 className="font-bold">Ernährungspräferenzen</h2></div>
            <p className="text-xs text-gray-500 mb-3">Wird automatisch im Speiseplan angewendet.</p>
            <div className="flex flex-wrap gap-2 mb-3">
              {["VEGAN", "VEGETARISCH", "HALAL"].map(d => (
                <Badge key={d} variant={prefs.dietary.includes(d) ? "blue" : "outline"} className="cursor-pointer" onClick={() => togglePref("dietary", d)}>{d}</Badge>
              ))}
            </div>
            <h3 className="text-sm font-medium mb-2">Allergene ausschließen</h3>
            <div className="flex flex-wrap gap-2">
              {["GLUTEN", "MILCH", "EI", "FISCH", "SOJA", "NUESSE", "ERDNUSS", "SELLERIE", "SENF", "SESAM"].map(a => (
                <Badge key={a} variant={prefs.allergens.includes(a) ? "red" : "outline"} className="cursor-pointer" onClick={() => togglePref("allergens", a)}>
                  {prefs.allergens.includes(a) ? `Ohne ${a}` : a}
                </Badge>
              ))}
            </div>
          </div>
          <Button variant="outline" className="w-full text-red-600 border-red-300 hover:bg-red-50" onClick={logout}>
            <LogOut className="w-4 h-4 mr-1" /> Abmelden
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div>
      <div className="bg-[#003a70] text-white p-6 shadow-lg">
        <h1 className="text-2xl">Profil</h1>
        <p className="text-sm text-blue-100">Dein persönlicher Bereich</p>
      </div>
      <div className="p-4">
        {view === "landing" && (
          <div className="text-center py-8">
            <User className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-600 mb-2">Nicht angemeldet</p>
            <p className="text-sm text-gray-500 mb-6">Anmelden für Studierendenrabatt und personalisierte Ansicht.</p>
            <div className="space-y-2">
              <Button className="w-full" onClick={() => setView("login")}>Anmelden</Button>
              <Button variant="outline" className="w-full" onClick={() => setView("register")}>Registrieren</Button>
            </div>
          </div>
        )}

        {view === "login" && (
          <div className="space-y-4">
            <h2 className="font-bold text-lg">Anmelden</h2>
            {error && <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{error}</div>}
            <div>
              <Label>E-Mail</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                <Input className="pl-10" placeholder="vorname@stud.fh-dortmund.de" value={email} onChange={e => setEmail(e.target.value)} />
              </div>
            </div>
            <div>
              <Label>Passwort</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                <Input className="pl-10" type={showPw ? "text" : "password"} value={password} onChange={e => setPassword(e.target.value)} />
                <button onClick={() => setShowPw(!showPw)} className="absolute right-3 top-3 text-gray-400">{showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}</button>
              </div>
            </div>
            <Button className="w-full" disabled={loading} onClick={handleLogin}>{loading ? "Anmelden..." : "Anmelden"}</Button>
            <button onClick={() => setView("register")} className="w-full text-sm text-gray-500 underline">Noch kein Konto? Registrieren</button>
          </div>
        )}

        {view === "register" && (
          <div className="space-y-4">
            <h2 className="font-bold text-lg">Registrieren</h2>
            {error && <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg">{error}</div>}
            <div className="grid grid-cols-2 gap-2">
              <div><Label>Vorname</Label><Input value={vorname} onChange={e => setVorname(e.target.value)} /></div>
              <div><Label>Nachname</Label><Input value={nachname} onChange={e => setNachname(e.target.value)} /></div>
            </div>
            <div>
              <Label>Hochschule-E-Mail</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                <Input className="pl-10" placeholder="vorname.nachname000@stud.fh-dortmund.de" value={email} onChange={e => setEmail(e.target.value)} />
              </div>
            </div>
            <div>
              <Label>Passwort</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 w-4 h-4 text-gray-400" />
                <Input className="pl-10" type={showPw ? "text" : "password"} value={password} onChange={e => setPassword(e.target.value)} />
                <button onClick={() => setShowPw(!showPw)} className="absolute right-3 top-3 text-gray-400">{showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}</button>
              </div>
            </div>
            <Button className="w-full" disabled={loading} onClick={handleRegister}>{loading ? "Registrieren..." : "Registrieren"}</Button>
            <button onClick={() => setView("login")} className="w-full text-sm text-gray-500 underline">Schon Konto? Anmelden</button>
          </div>
        )}
      </div>
    </div>
  );
}
