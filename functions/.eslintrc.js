// Configuración mínima de ESLint (opcional). Evita que el lint bloquee el deploy.
module.exports = {
  env: {
    es2021: true,
    node: true,
  },
  parserOptions: {
    ecmaVersion: 2021,
  },
  rules: {},
};
