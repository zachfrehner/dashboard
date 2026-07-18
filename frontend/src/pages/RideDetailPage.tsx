import { Box, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useParams } from 'react-router-dom';
import { Area, AreaChart, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getRideDetail } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';
import { dash, formatDateTime, formatFeet, formatMiles, formatNumberUnit } from '../utils/format';

export function RideDetailPage() {
  const { rideId = 'ride-1' } = useParams();
  const ride = useQuery({ queryKey: ['ride', rideId], queryFn: () => getRideDetail(rideId) });
  const data = ride.data;

  return (
    <Stack spacing={3}>
      <PageHeader title={data?.name ?? 'Ride Detail'} subtitle={formatDateTime(data?.startedAt)} />
      <MetricGrid
        metrics={[
          { title: 'Distance', value: formatMiles(data?.summary.distanceMiles) },
          { title: 'Elevation', value: formatFeet(data?.summary.elevationFeet) },
          { title: 'Avg Power', value: formatNumberUnit(data?.summary.averagePowerWatts, 'W') },
          { title: 'Heart Rate', value: formatNumberUnit(data?.summary.averageHeartRateBpm, 'bpm') },
        ]}
      />
      <Grid container spacing={2}>
        <Grid item xs={12} md={5}>
          <DashboardCard title="Map">
            <Box sx={{ minHeight: 300, border: '1px dashed rgba(255,255,255,0.18)', borderRadius: 2, display: 'grid', placeItems: 'center' }}>
              <Typography color="text.secondary">{dash}</Typography>
            </Box>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Power">
            <Chart data={data?.power ?? []} color="#44d19d" />
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={6}>
          <DashboardCard title="Heart Rate">
            <Chart data={data?.heartRate ?? []} color="#ff6b6b" />
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={6}>
          <DashboardCard title="Elevation Profile">
            <ElevationChart data={data?.elevation ?? []} />
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={6}>
          <DashboardCard title="BurnMetrix Calculations" detail={data?.burnMetrixSummary ?? dash} />
        </Grid>
        <Grid item xs={12} md={6}>
          <DashboardCard title="Notes" detail={data?.notes ?? dash} />
        </Grid>
      </Grid>
    </Stack>
  );
}

function Chart({ data, color }: { data: { label: string; value: number }[]; color: string }) {
  return (
    <Box sx={{ height: 260 }}>
      {data.length === 0 ? (
        <Box sx={{ height: '100%', display: 'grid', placeItems: 'center' }}>
          <Typography color="text.secondary">{dash}</Typography>
        </Box>
      ) : (
        <ResponsiveContainer>
          <LineChart data={data}>
            <XAxis dataKey="label" stroke="#a9b4c2" tickLine={false} axisLine={false} />
            <YAxis stroke="#a9b4c2" tickLine={false} axisLine={false} />
            <Tooltip contentStyle={{ background: '#111820', border: '1px solid rgba(255,255,255,0.12)' }} />
            <Line type="monotone" dataKey="value" stroke={color} strokeWidth={3} dot={{ r: 4 }} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </Box>
  );
}

function ElevationChart({ data }: { data: { label: string; value: number }[] }) {
  return (
    <Box sx={{ height: 260 }}>
      {data.length === 0 ? (
        <Box sx={{ height: '100%', display: 'grid', placeItems: 'center' }}>
          <Typography color="text.secondary">{dash}</Typography>
        </Box>
      ) : (
        <ResponsiveContainer>
          <AreaChart data={data}>
            <XAxis dataKey="label" stroke="#a9b4c2" tickLine={false} axisLine={false} />
            <YAxis stroke="#a9b4c2" tickLine={false} axisLine={false} />
            <Tooltip contentStyle={{ background: '#111820', border: '1px solid rgba(255,255,255,0.12)' }} />
            <Area type="monotone" dataKey="value" stroke="#f6bd60" fill="rgba(246,189,96,0.22)" strokeWidth={3} />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </Box>
  );
}
