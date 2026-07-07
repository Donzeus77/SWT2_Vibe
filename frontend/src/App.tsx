import { BrowserRouter, Routes, Route } from "react-router";
import { AuthProvider } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import Home from "./pages/Home";
import Standorte from "./pages/Standorte";
import Bestellungen from "./pages/Bestellungen";
import Abstimmung from "./pages/Abstimmung";
import Profil from "./pages/Profil";
import Navigation from "./components/Navigation";

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <BrowserRouter>
          <div className="flex justify-center bg-gray-200 min-h-screen">
            <div className="bg-white shadow-2xl w-full max-w-[430px] min-h-screen overflow-hidden relative">
              <div className="pb-20">
                <Routes>
                  <Route path="/" element={<Home />} />
                  <Route path="/standorte" element={<Standorte />} />
                  <Route path="/bestellungen" element={<Bestellungen />} />
                  <Route path="/abstimmung" element={<Abstimmung />} />
                  <Route path="/profil" element={<Profil />} />
                </Routes>
              </div>
              <Navigation />
            </div>
          </div>
        </BrowserRouter>
      </CartProvider>
    </AuthProvider>
  );
}
