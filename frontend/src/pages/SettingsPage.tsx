import { Chip, Grid, Stack } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getSettings } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';

export function SettingsPage() {
  const settings = useQuery({ queryKey: ['settings'], queryFn: getSettings });
  const integrations = settings.data ? [settings.data.strava, settings.data.googleCalendar, settings.data.weather] : [];

  return (
    <Stack spacing={3}>
      <PageHeader title="Settings" subtitle="Integrations, display, units, and about" />
      <Grid container spacing={2}>
        {integrations.map((integration) => (
          <Grid item xs={12} md={4} key={integration.name}>
            <DashboardCard title={integration.name} value={integration.connected ? 'Connected' : 'Not Connected'}>
              <Chip label={integration.provider} color={integration.connected ? 'success' : 'default'} sx={{ alignSelf: 'flex-start' }} />
            </DashboardCard>
          </Grid>
        ))}
        <Grid item xs={12} md={4}>
          <DashboardCard title="Display" value={settings.data?.displayMode ?? 'kiosk'} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Units" value={settings.data?.units ?? 'imperial'} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="About" value={settings.data?.version ?? '0.1.0'} />
        </Grid>
      </Grid>
    </Stack>
  );
}

