import { Chip, Grid, Stack } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getSettings, getSystemStatus } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';
import { dash, formatInteger } from '../utils/format';

export function SettingsPage() {
  const settings = useQuery({ queryKey: ['settings'], queryFn: getSettings });
  const system = useQuery({
    queryKey: ['system', 'status'],
    queryFn: getSystemStatus,
    refetchInterval: 30_000,
  });
  const integrations = settings.data ? [settings.data.strava, settings.data.googleCalendar, settings.data.weather] : [
    { name: 'Strava', connected: false, provider: dash },
    { name: 'Google Calendar', connected: false, provider: dash },
    { name: 'Weather', connected: false, provider: dash },
  ];
  const status = system.data;

  return (
    <Stack spacing={3}>
      <PageHeader title="Settings" subtitle="Integrations, device health, and system status" />
      <Grid container spacing={2}>
        {integrations.map((integration) => (
          <Grid item xs={12} md={4} key={integration.name}>
            <DashboardCard title={integration.name} value={integration.connected ? 'Connected' : 'Not Connected'}>
              <Chip label={integration.provider} color={integration.connected ? 'success' : 'default'} sx={{ alignSelf: 'flex-start' }} />
            </DashboardCard>
          </Grid>
        ))}
      </Grid>

      <MetricGrid
        metrics={[
          { title: 'CPU Temp', value: temperature(status?.cpuTemperatureC) },
          { title: 'CPU Load', value: load(status?.load1, status?.load5, status?.load15), detail: '1 / 5 / 15 min' },
          { title: 'Memory', value: percent(status?.memoryUsedPercent), detail: memoryDetail(status?.memoryUsedMb, status?.memoryTotalMb) },
          { title: 'Disk', value: percent(status?.diskUsedPercent), detail: diskDetail(status?.diskUsedGb, status?.diskTotalGb) },
          { title: 'Uptime', value: uptime(status?.uptimeSeconds) },
          { title: 'IP Address', value: status?.ipAddress ?? dash },
        ]}
      />

      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Backend" value={service(status?.backendStatus)} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Kiosk" value={service(status?.kioskStatus)} />
        </Grid>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Nginx" value={service(status?.nginxStatus)} />
        </Grid>
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

function temperature(value: number | null | undefined) {
  return value === null || value === undefined ? dash : `${value.toFixed(1)} C`;
}

function load(load1: number | null | undefined, load5: number | null | undefined, load15: number | null | undefined) {
  if (load1 === null || load1 === undefined) {
    return dash;
  }

  return [load1, load5, load15]
    .filter((value) => value !== null && value !== undefined)
    .map((value) => value.toFixed(2))
    .join(' / ');
}

function percent(value: number | null | undefined) {
  return value === null || value === undefined ? dash : `${value.toFixed(1)}%`;
}

function memoryDetail(used: number | null | undefined, total: number | null | undefined) {
  return used === null || used === undefined || total === null || total === undefined
    ? dash
    : `${formatInteger(used)} / ${formatInteger(total)} MB`;
}

function diskDetail(used: number | null | undefined, total: number | null | undefined) {
  return used === null || used === undefined || total === null || total === undefined
    ? dash
    : `${formatInteger(used)} / ${formatInteger(total)} GB`;
}

function uptime(seconds: number | null | undefined) {
  if (seconds === null || seconds === undefined) {
    return dash;
  }

  const totalSeconds = Number(seconds);
  const days = Math.floor(totalSeconds / 86_400);
  const hours = Math.floor((totalSeconds % 86_400) / 3_600);
  const minutes = Math.floor((totalSeconds % 3_600) / 60);

  return days > 0 ? `${days}d ${hours}h` : `${hours}h ${minutes}m`;
}

function service(value: string | null | undefined) {
  return value ? value[0].toUpperCase() + value.slice(1) : dash;
}
