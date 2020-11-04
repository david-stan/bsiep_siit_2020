
const ENVIRONMENTS = {
  localhost: 'dev',
  '188.166.8.206': 'production'
};

const development = {
  pki: 'https://localhost:9091',
  siem: 'http://localhost:9090',
  agent: 'http://localhost:9093'
};

const BACKEND_HOSTNAMES = {
  dev: development,
  production: 'http://188.166.8.206:9091'
};

const AUTH_HOSTNAMES = {
  dev: 'http://localhost:8080/auth',
  production: 'http://188.166.8.206:8080/auth'
};

const APP_HOSTNAMES = {
  dev: 'http://localhost:4200',
  production: 'http://188.166.8.206'
};

const API_ROUTE = '/api';

export {
  ENVIRONMENTS,
  BACKEND_HOSTNAMES,
  APP_HOSTNAMES,
  API_ROUTE,
  AUTH_HOSTNAMES
};
