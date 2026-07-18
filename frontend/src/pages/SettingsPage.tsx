import { Chip, Grid, Stack } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getSettings } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';
import { dash } from '../utils/format';

export function SettingsPage() {
  const settings = useQuery({ queryKey: ['settings'], queryFn: getSettings });
  const integrations = settings.data ? [settings.data.strava, settings.data.googleCalendar, settings.data.weather] : [
    { name: 'Strava', connected: false, provider: dash },
    { name: 'Google Calendar', connected: false, provider: dash },
    { name: 'Weather', connected: false, provider: dash },
  ];

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
          <DashboardCard title="Display" value={settings.data?.displayMode ?? dash} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Units" value={settings.data?.units ?? dash} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="About" value={settings.data?.version ?? dash} />
        </Grid>
      </Grid>
    </Stack>
  );
}
