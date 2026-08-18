import { Box, Typography } from '@mui/material';
import { useEffect, useState } from 'react';

const sleepStartMinutes = 21 * 60 + 30;
const morningStartMinutes = 6 * 60;
const dismissedKeyPrefix = 'burnmetrix-morning-dismissed';

type RestMode = 'sleep' | 'morning' | null;

interface RestState {
  mode: RestMode;
  dateKey: string;
}

export function RestScreen() {
  const [state, setState] = useState(() => getRestState());

  useEffect(() => {
    const timer = window.setInterval(() => setState(getRestState()), 30_000);
    return () => window.clearInterval(timer);
  }, []);

  if (!state.mode) {
    return null;
  }

  const isMorning = state.mode === 'morning';

  const handleTap = () => {
    if (!isMorning) {
      return;
    }

    window.localStorage.setItem(morningDismissedKey(state.dateKey), '1');
    setState(getRestState());
  };

  return (
    <Box
      role={isMorning ? 'button' : undefined}
      tabIndex={isMorning ? 0 : undefined}
      onClick={handleTap}
      onKeyDown={(event) => {
        if (isMorning && (event.key === 'Enter' || event.key === ' ')) {
          handleTap();
        }
      }}
      sx={{
        position: 'fixed',
        inset: 0,
        zIndex: 2000,
        display: 'grid',
        placeItems: 'center',
        cursor: isMorning ? 'pointer' : 'default',
        background: isMorning
          ? 'linear-gradient(180deg, #10221d 0%, #060a0d 100%)'
          : 'linear-gradient(180deg, #030405 0%, #000 100%)',
        color: isMorning ? '#e9fff5' : 'rgba(255,255,255,0.28)',
      }}
    >
      <Box sx={{ textAlign: 'center', px: 3 }}>
        <Typography
          component="div"
          sx={{
            fontSize: { xs: '3rem', md: '5.5rem' },
            fontWeight: 800,
            lineHeight: 1,
          }}
        >
          {isMorning ? 'Good morning' : 'Sleep tight'}
        </Typography>
        {isMorning && (
          <Typography sx={{ mt: 2, color: 'rgba(233,255,245,0.68)', fontSize: { xs: '1rem', md: '1.35rem' } }}>
            Tap to open Frehner Home
          </Typography>
        )}
      </Box>
    </Box>
  );
}

function getRestState(): RestState {
  const now = new Date();
  const minutes = now.getHours() * 60 + now.getMinutes();
  const dateKey = formatDateKey(now);

  if (minutes >= sleepStartMinutes || minutes < morningStartMinutes) {
    return { mode: 'sleep', dateKey };
  }

  if (window.localStorage.getItem(morningDismissedKey(dateKey))) {
    return { mode: null, dateKey };
  }

  return { mode: 'morning', dateKey };
}

function morningDismissedKey(dateKey: string) {
  return `${dismissedKeyPrefix}-${dateKey}`;
}

function formatDateKey(date: Date) {
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`;
}
