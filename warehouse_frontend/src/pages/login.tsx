import { useEffect } from "react";
import { useNavigate } from "react-router";
import { useUserStore } from "../state/user_store";
import { requestLogin } from "../services/user_service";

export const LoginPage = () => {
  const navigate = useNavigate();
  const user = useUserStore((state) => state.user);

  useEffect(() => {
    // if user is already authenticated redirect to home page
    if (user) {
      navigate("/", { replace: true });
    }
  }, [user]);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100 gap-5">
      <button
        className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded transition-all duration-300 ease-in-out transform hover:scale-105 cursor-pointer"
        onClick={() => requestLogin("google")}
      >
        Login with Google
      </button>
      <button
        className="bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded transition-all duration-300 ease-in-out transform hover:scale-105 cursor-pointer"
        onClick={() => requestLogin("github")}
      >
        Login with Github
      </button>
    </div>
  );
};
