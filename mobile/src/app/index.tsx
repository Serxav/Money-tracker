import { Redirect } from 'expo-router';

import { useSession } from '@/core/session/SessionProvider';
import { Loading } from '@/shared/ui/Feedback';

/**
 * Ruta inicial pública: `/` existe con y sin sesión y redirige a donde toca. Sin ella, al abrir
 * la app sin sesión la ruta inicial quedaba protegida y Expo Router mostraba "Unmatched Route".
 */
export default function Index() {
  const { status } = useSession();
  if (status === 'loading') {
    return <Loading />;
  }
  return <Redirect href={status === 'signedIn' ? '/home' : '/login'} />;
}
