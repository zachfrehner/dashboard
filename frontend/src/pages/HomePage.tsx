import { Chip, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';

import { getCalendarEvents, getCurrentWeather, getCyclingSummary, getSettings } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';
import { formatDateTime, formatDuration, formatFeet, formatMiles } from '../utils/format';

export function HomePage() {
  const weather = useQuery({ queryKey: ['weather', 'current'], queryFn: getCurrentWeather });
  const events = useQuery({ queryKey: ['calendar', 'events'], queryFn: getCalendarEvents });
  const cycling = useQuery({ queryKey: ['cycling', 'week'], queryFn: () => getCyclingSummary('week') });
  const settings = useQuery({ queryKey: ['settings'], queryFn: getSettings });
  const currentTime = new Intl.DateTimeFormat(undefined, { hour: 'numeric', minute: '2-digit' }).format(new Date());
  const nextEvent = events.data?.[0];
  const ride = cycling.data?.recentRides[0];

  return (
    <Stack spacing={3}>
      <PageHeader title="Home" subtitle="Kiosk overview" action={<Chip color="success" label={currentTime} sx={{ fontSize: '1rem', height: 44 }} />} />
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <DashboardCard
            title="Current Weather"
            value={weather.data ? `${Math.round(weather.data.temperatureF)}°F` : '...'}
            detail={weather.data ? `${weather.data.condition} · Wind ${weather.data.windMph} mph` : undefined}
          />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Next Event" value={nextEvent?.title ?? 'None'} detail={nextEvent ? formatDateTime(nextEvent.startsAt) : 'Open calendar'} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Today Ride" value={ride ? formatMiles(ride.distanceMiles) : 'Rest'} detail={ride?.name ?? 'No ride logged'} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <DashboardCard title="Weekly Mileage" value={cycling.data ? formatMiles(cycling.data.distanceMiles) : '...'} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <DashboardCard title="Weekly Elevation" value={cycling.data ? formatFeet(cycling.data.elevationFeet) : '...'} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <DashboardCard title="Current Streak" value="6 days" detail="Sample data" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <DashboardCard title="System Status" value="Online" detail={settings.data ? `Mode: ${settings.data.displayMode}` : 'Checking'} />
        </Grid>
      </Grid>
      <DashboardCard title="Recent Rides">
        <Stack spacing={1.5}>
          {cycling.data?.recentRides.map((recentRide) => (
            <Stack key={recentRide.id} direction="row" justifyContent="space-between" alignItems="center">
              <Stack>
                <Typography fontWeight={700}>{recentRide.name}</Typography>
                <Typography color="text.secondary">{formatDateTime(recentRide.startedAt)}</Typography>
              </Stack>
              <Typography fontWeight={800}>{formatMiles(recentRide.distanceMiles)}</Typography>
            </Stack>
          ))}
          {cycling.data && <Typography color="text.secondary">Moving time {formatDuration(cycling.data.movingTimeSeconds)}</Typography>}
        </Stack>
      </DashboardCard>
    </Stack>
  );
}

