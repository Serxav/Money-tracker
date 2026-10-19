import * as SecureStore from 'expo-secure-store';

/**
 * El refresh token se guarda en Keychain (iOS) / Keystore (Android), solo en este dispositivo y
 * con el dispositivo desbloqueado. El access token vive únicamente en memoria.
 */
const REFRESH_KEY = 'mt.refreshToken';
const options: SecureStore.SecureStoreOptions = {
  keychainAccessible: SecureStore.WHEN_UNLOCKED_THIS_DEVICE_ONLY,
};

let accessToken: string | null = null;

export const tokenStore = {
  getAccessToken: () => accessToken,
  setAccessToken: (token: string | null) => {
    accessToken = token;
  },
  getRefreshToken: () => SecureStore.getItemAsync(REFRESH_KEY, options),
  setRefreshToken: (token: string) => SecureStore.setItemAsync(REFRESH_KEY, token, options),
  async clear() {
    accessToken = null;
    await SecureStore.deleteItemAsync(REFRESH_KEY, options);
  },
};
