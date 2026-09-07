import { create } from "zustand";

type TypeUserStore = {
  user: TypeUser | null;
  clearAuth: () => void;
  setUser: (user: TypeUser) => void;
};

export type TypeUser = {
  id: string;
  name: string;
  email: string;
};

export const useUserStore = create<TypeUserStore>((set) => ({
  user: null,
  setUser: (user: TypeUser) => set({ user }),
  clearAuth: () => {
    localStorage.clear();
    set({ user: null });
  },
}));
