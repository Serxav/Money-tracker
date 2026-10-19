import { render, screen } from '@testing-library/react-native';

import { TextField } from '../TextField';

it('expone la etiqueta y anuncia el error como alerta', async () => {
  await render(<TextField label="Importe (EUR)" value="" onChangeText={() => {}} error="Importe no válido" />);
  expect(screen.getByLabelText('Importe (EUR)')).toBeTruthy();
  expect(screen.getByRole('alert')).toHaveTextContent('Importe no válido');
});
