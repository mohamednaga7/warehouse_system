export const requestLogin = (provider: "google" | "github") => {
  window.location.href = `http://localhost:8080/oauth2/authorization/${provider}`;
};
