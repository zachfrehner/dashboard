import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { MemoryRouter } from 'react-router-dom';
import { ThemeProvider } from '@mui/material';
import { describe, expect, it } from 'vitest';
import { HomePage } from './pages/HomePage';
import { theme } from './theme';

describe('HomePage', () => {
  it('renders the dashboard overview', async () => {
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });

    render(
      <ThemeProvider theme={theme}>
        <QueryClientProvider client={queryClient}>
          <MemoryRouter>
            <HomePage />
          </MemoryRouter>
        </QueryClientProvider>
      </ThemeProvider>,
    );

    expect(await screen.findByText('Home')).toBeInTheDocument();
    expect(await screen.findByText('Current Weather')).toBeInTheDocument();
  });
});

