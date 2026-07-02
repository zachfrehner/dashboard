import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'dark',
    background: {
      default: '#080b0f',
      paper: '#111820',
    },
    primary: {
      main: '#44d19d',
      contrastText: '#06100c',
    },
    secondary: {
      main: '#5fb7ff',
    },
    warning: {
      main: '#f6bd60',
    },
    error: {
      main: '#ff6b6b',
    },
    success: {
      main: '#44d19d',
    },
    divider: 'rgba(255,255,255,0.08)',
    text: {
      primary: '#f5f7fb',
      secondary: '#a9b4c2',
    },
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif',
    h1: { fontSize: '2.4rem', fontWeight: 700, letterSpacing: 0 },
    h2: { fontSize: '1.7rem', fontWeight: 700, letterSpacing: 0 },
    h3: { fontSize: '1.25rem', fontWeight: 700, letterSpacing: 0 },
    button: { textTransform: 'none', fontWeight: 700, letterSpacing: 0 },
  },
  shape: {
    borderRadius: 8,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid rgba(255,255,255,0.08)',
          backgroundImage: 'none',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          minHeight: 48,
        },
      },
    },
    MuiIconButton: {
      styleOverrides: {
        root: {
          minHeight: 48,
          minWidth: 48,
        },
      },
    },
  },
});

