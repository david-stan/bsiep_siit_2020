import {
  ENVIRONMENTS,
  BACKEND_HOSTNAMES,
  API_ROUTE,
  AUTH_HOSTNAMES
} from '../constants';

const config = {

  getEnv() {
    return ENVIRONMENTS[window.location.hostname];
  },

  getHostName(msName: string) {
    const env = this.getEnv();
    if (env === 'dev') {
      return BACKEND_HOSTNAMES[env][msName]; // e.g. dev[pki] or dev[siem]
    }
    return BACKEND_HOSTNAMES[env];
  },

  getAuthHostName() {
    return AUTH_HOSTNAMES[this.getEnv()];
  },

  getApiUrl(msName: string) {
    return this.getHostName(msName) + API_ROUTE;
  },

  isProductionEnv() {
    return this.getEnv() === 'production';
  },

};

export { config };
