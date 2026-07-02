import { Box, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { getCurrentWeather } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';

const hourly = [
  { label: 'Now', temp: 72 },
  { label: '1p', temp: 75 },
  { label: '2p', temp: 77 },
  { label: '3p', temp: 76 },
  { label: '4p', temp: 73 },
  { label: '5p', temp: 70 },
];

export function WeatherPage() {
  const weather = useQuery({ queryKey: ['weather', 'current'], queryFn: getCurrentWeather });
  const current = weather.data;

  return (
    <Stack spacing={3}>
      <PageHeader title="Weather" subtitle="Provider-ready conditions, forecast, and environment data" />
      <Grid container spacing={2}>
        <Grid item xs={12} md={5}>
          <DashboardCard title="Current Conditions" value={current ? `${Math.round(current.temperatureF)}°F` : '...'} detail={current?.condition}>
            {current && (
              <Typography color="text.secondary">
                Feels like {Math.round(current.feelsLikeF)}°F · {current.provider}
              </Typography>
            )}
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Hourly Forecast">
            <Box sx={{ height: 220 }}>
              <ResponsiveContainer>
                <LineChart data={hourly}>
                  <XAxis dataKey="label" stroke="#a9b4c2" tickLine={false} axisLine={false} />
                  <YAxis stroke="#a9b4c2" tickLine={false} axisLine={false} />
                  <Tooltip contentStyle={{ background: '#111820', border: '1px solid rgba(255,255,255,0.12)' }} />
                  <Line type="monotone" dataKey="temp" stroke="#5fb7ff" strokeWidth={3} dot={{ r: 5 }} />
                </LineChart>
              </ResponsiveContainer>
            </Box>
          </DashboardCard>
        </Grid>
      </Grid>
      <MetricGrid
        metrics={[
          { title: 'Wind', value: current ? `${current.windMph} mph` : '...' },
          { title: 'Humidity', value: current ? `${current.humidityPercent}%` : '...' },
          { title: 'UV Index', value: current?.uvIndex ?? '...' },
          { title: 'Air Quality', value: '42', detail: 'Mock AQI' },
          { title: 'Sunrise', value: '5:42 AM' },
          { title: 'Sunset', value: '8:31 PM' },
        ]}
      />
      <DashboardCard title="Radar">
        <Box sx={{ minHeight: 220, border: '1px dashed rgba(255,255,255,0.18)', borderRadius: 2, display: 'grid', placeItems: 'center' }}>
          <Typography color="text.secondary">Radar image placeholder</Typography>
        </Box>
      </DashboardCard>
    </Stack>
  );
}

