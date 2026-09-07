import { useEffect, useRef } from "react";
import { useNavigate } from "react-router";
import { useUserStore } from "../state/user_store";

export const HomePage = () => {
  // if not authenticated redirect to login page
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);
  const clearAuth = useUserStore((state) => state.clearAuth);
  const isFetchingUser = useRef(false);

  useEffect(() => {
    const storedToken = localStorage.getItem("auth_token");
    const storedRefreshToken = localStorage.getItem("refresh_token");
    const hash = window.location.hash;

    if (
      !storedToken &&
      !storedRefreshToken &&
      !user &&
      !isFetchingUser.current &&
      !hash
    ) {
      navigate("/login", { replace: true });
    }

    if (
      (storedToken || storedRefreshToken) &&
      !user &&
      !isFetchingUser.current
    ) {
      // fetch user info and if token is not working use refresh token to get new access token and refresh token
      fetch("http://localhost:8080/api/users/info", {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${storedToken}`,
        },
      })
        .then((res) => {
          if (res.status === 401) {
            // access token is not valid, use refresh token to get new access token and refresh token
            return fetch("http://localhost:8080/api//refresh", {
              method: "POST",
              credentials: "include",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${storedRefreshToken}`,
              },
            });
          }
          return res;
        })
        .then((res) => res.json())
        .then((data) => {
          if (data && data.id && data.name && data.email) {
            useUserStore.setState({ user: data });
          } else {
            navigate("/login", { replace: true });
          }
        })
        .catch((err) => {
          console.error(err);
          navigate("/login", { replace: true });
        });
    } else {
      // if user is already authenticated redirect to home page
      if (user) {
        navigate("/", { replace: true });
      }
    }

    if (!user && !hash) return;

    const params = new URLSearchParams(hash.substring(1));
    const token = params.get("access_token");
    if (token) {
      localStorage.setItem("auth_token", token);
    }

    const refreshToken = params.get("refresh_token");
    if (refreshToken) {
      localStorage.setItem("refresh_token", refreshToken);
    }

    if (user || isFetchingUser.current) return;

    isFetchingUser.current = true;

    fetch("http://localhost:8080/api/users/info", {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${localStorage.getItem("auth_token")}`,
      },
    })
      .then((res) => res.json())
      .then((data) => {
        if (data && data.id && data.name && data.email) {
          useUserStore.setState({ user: data });
        } else {
          navigate("/login", { replace: true });
        }
      })
      .catch((err) => {
        console.error(err);
        navigate("/login", { replace: true });
      });
  }, [user]);

  // I need to chagne the content of this page to include a welcome message with the name of the user and a button to logout
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <h1 className="text-4xl font-bold mb-4">Welcome, {user?.name}!</h1>
      <p className="text-lg text-gray-700">
        This is the home page of our application.
      </p>
      <button
        className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded transition-colors duration-300 ease-in-out transform hover:scale-105 cursor-pointer"
        onClick={() => {
          clearAuth();
          navigate("/login", { replace: true });
        }}
      >
        Logout
      </button>
    </div>
  );
};
