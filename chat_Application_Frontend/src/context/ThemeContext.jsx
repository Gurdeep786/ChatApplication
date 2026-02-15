import { createContext, useEffect, useState } from "react";

export const ThemeContext = createContext();

export const ThemeProvider = ({ children }) => {
  const [theme, setTheme] = useState(() => {
    try {
      const t = localStorage.getItem("theme");
      return t === "light" || t === "dark" ? t : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    try {
      if (theme) {
        localStorage.setItem("theme", theme);
        document.documentElement.setAttribute("data-theme", theme);
      } else {
        localStorage.removeItem("theme");
        document.documentElement.removeAttribute("data-theme");
      }
    } catch (e) {
      // ignore
    }
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, setTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};
