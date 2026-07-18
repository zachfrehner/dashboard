import AccessTimeIcon from '@mui/icons-material/AccessTime';
import ArticleIcon from '@mui/icons-material/Article';
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import { Box, Chip, Divider, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import type { ReactNode } from 'react';

import { getCalendarEvents, getCurrentWeather, getCyclingSummary } from '../api/dashboardApi';
import { dash, formatDateTime, formatDuration, formatFeet, formatInteger, formatMiles, formatNumberUnit } from '../utils/format';

export function HomePage() {
  const weather = useQuery({ queryKey: ['weather', 'current'], queryFn: getCurrentWeather });
  const events = useQuery({ queryKey: ['calendar', 'events'], queryFn: getCalendarEvents });
  const cycling = useQuery({ queryKey: ['cycling', 'week'], queryFn: () => getCyclingSummary('week') });
  const now = new Date();
  const currentTime = new Intl.DateTimeFormat(undefined, { hour: 'numeric', minute: '2-digit' }).format(now);
  const currentDate = new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'short', day: 'numeric' }).format(now);
  const upcomingEvents = events.data?.slice(0, 4) ?? [];

  return (
    <Box
      sx={{
        height: { xs: 'auto', md: 'calc(100vh - 88px)' },
        minHeight: 0,
        overflow: 'hidden',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' },
        gridTemplateRows: { xs: 'repeat(4, minmax(220px, auto))', md: '1fr 1fr' },
        gap: { xs: 1.25, md: 2 },
        '@media (min-width:700px)': {
          height: 'calc(100vh - 32px)',
          gridTemplateColumns: '1fr 1fr',
          gridTemplateRows: '1fr 1fr',
        },
        '@media (min-width:900px)': {
          height: 'calc(100vh - 88px)',
          gap: 2,
        },
      }}
    >
      <HomePanel title="Weather" icon={<WbSunnyIcon />} accent="#f6bd60">
        <Stack spacing={2} sx={{ height: '100%', justifyContent: 'space-between' }}>
          <Box>
            <Stack direction="row" spacing={1.5} alignItems="center">
              <AccessTimeIcon color="primary" />
              <Typography sx={{ fontSize: { xs: '2.35rem', md: '3.8rem' }, lineHeight: 1, fontWeight: 800 }}>
                {currentTime}
              </Typography>
            </Stack>
            <Typography color="text.secondary" sx={{ mt: 0.75 }}>
              {currentDate}
            </Typography>
          </Box>
          <Stack direction="row" spacing={2} alignItems="flex-end" justifyContent="space-between">
            <Box>
              <Typography sx={{ fontSize: { xs: '2rem', md: '3.2rem' }, lineHeight: 1, fontWeight: 800 }}>
                {weather.data?.temperatureF !== null && weather.data?.temperatureF !== undefined ? `${Math.round(weather.data.temperatureF)} F` : dash}
              </Typography>
              <Typography color="text.secondary">{weather.data?.condition ?? dash}</Typography>
            </Box>
            <Stack spacing={0.75} sx={{ minWidth: 138 }}>
              <MetricLine label="Wind" value={formatNumberUnit(weather.data?.windMph, 'mph')} />
              <MetricLine label="Sunrise" value={weather.data?.sunrise ?? dash} />
              <MetricLine label="Sunset" value={weather.data?.sunset ?? dash} />
            </Stack>
          </Stack>
        </Stack>
      </HomePanel>

      <HomePanel title="Cycling" icon={<DirectionsBikeIcon />} accent="#44d19d">
        <Stack spacing={2.5} sx={{ height: '100%', justifyContent: 'center' }}>
          <Stack direction="row" spacing={2} alignItems="stretch">
            <MetricBlock label="Week Miles" value={formatMiles(cycling.data?.distanceMiles)} />
            <MetricBlock label="Week Hours" value={formatDuration(cycling.data?.movingTimeSeconds)} />
          </Stack>
          <Stack direction="row" spacing={1.25} flexWrap="wrap" useFlexGap>
            <Chip label={`Elevation ${formatFeet(cycling.data?.elevationFeet)}`} />
            <Chip label={`Calories ${formatInteger(cycling.data?.calories)}`} />
            <Chip label={`HR ${formatNumberUnit(cycling.data?.averageHeartRateBpm, 'bpm')}`} />
          </Stack>
        </Stack>
      </HomePanel>

      <HomePanel title="Calendar" icon={<CalendarMonthIcon />} accent="#5fb7ff">
        <Stack spacing={1.5} sx={{ height: '100%', overflow: 'hidden' }}>
          {upcomingEvents.length === 0 && <Typography color="text.secondary">{dash}</Typography>}
          {upcomingEvents.map((event) => (
            <Stack key={event.id} spacing={0.25}>
              <Typography fontWeight={800} noWrap>
                {event.title || dash}
              </Typography>
              <Typography color="text.secondary" noWrap>
                {formatDateTime(event.startsAt)}{event.location ? ` - ${event.location}` : ''}
              </Typography>
            </Stack>
          ))}
        </Stack>
      </HomePanel>

      <HomePanel title="Top Stories" icon={<ArticleIcon />} accent="#ff6b6b">
        <Stack spacing={1.5} divider={<Divider flexItem />}>
          {[0, 1, 2].map((item) => (
            <Typography key={item} fontWeight={700}>
              {dash}
            </Typography>
          ))}
        </Stack>
      </HomePanel>
    </Box>
  );
}

function HomePanel({ title, icon, accent, children }: { title: string; icon: ReactNode; accent: string; children: ReactNode }) {
  return (
    <Box
      sx={{
        minHeight: 0,
        border: '1px solid rgba(255,255,255,0.08)',
        borderRadius: 2,
        bgcolor: 'background.paper',
        p: { xs: 1.5, md: 2.5 },
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Stack direction="row" alignItems="center" spacing={1} sx={{ color: accent, mb: 1.5 }}>
        {icon}
        <Typography fontWeight={800}>{title}</Typography>
      </Stack>
      <Box sx={{ minHeight: 0, flex: 1 }}>{children}</Box>
    </Box>
  );
}

function MetricLine({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" justifyContent="space-between" spacing={2}>
      <Typography color="text.secondary">{label}</Typography>
      <Typography fontWeight={800}>{value}</Typography>
    </Stack>
  );
}

function MetricBlock({ label, value }: { label: string; value: string }) {
  return (
    <Box sx={{ flex: 1, minWidth: 0 }}>
      <Typography color="text.secondary" fontWeight={700} sx={{ mb: 0.75 }}>
        {label}
      </Typography>
      <Typography sx={{ fontSize: { xs: '1.7rem', md: '2.6rem' }, lineHeight: 1, fontWeight: 800 }}>
        {value}
      </Typography>
    </Box>
  );
}
