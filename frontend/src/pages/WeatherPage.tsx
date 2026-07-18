import { Box, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getCurrentWeather } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';
import { dash, formatNumberUnit } from '../utils/format';

export function WeatherPage() {
  const weather = useQuery({ queryKey: ['weather', 'current'], queryFn: getCurrentWeather });
  const current = weather.data;
  const temperature = current?.temperatureF !== null && current?.temperatureF !== undefined ? `${Math.round(current.temperatureF)} F` : dash;
  const feelsLike = current?.feelsLikeF !== null && current?.feelsLikeF !== undefined ? `Feels like ${Math.round(current.feelsLikeF)} F - ${current.provider}` : dash;

  return (
    <Stack spacing={3}>
      <PageHeader title="Weather" subtitle="Current conditions and environment data" />
      <Grid container spacing={2}>
        <Grid item xs={12} md={5}>
          <DashboardCard title="Current Conditions" value={temperature} detail={current?.condition ?? dash}>
            <Typography color="text.secondary">{feelsLike}</Typography>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Forecast" value={dash} />
        </Grid>
      </Grid>
      <MetricGrid
        metrics={[
          { title: 'Wind', value: formatNumberUnit(current?.windMph, 'mph') },
          { title: 'Humidity', value: current?.humidityPercent !== null && current?.humidityPercent !== undefined ? `${current.humidityPercent}%` : dash },
          { title: 'UV Index', value: current?.uvIndex ?? dash },
          { title: 'Air Quality', value: dash },
          { title: 'Sunrise', value: current?.sunrise ?? dash },
          { title: 'Sunset', value: current?.sunset ?? dash },
        ]}
      />
      <DashboardCard title="Radar">
        <Box sx={{ minHeight: 220, border: '1px dashed rgba(255,255,255,0.18)', borderRadius: 2, display: 'grid', placeItems: 'center' }}>
          <Typography color="text.secondary">{dash}</Typography>
        </Box>
      </DashboardCard>
    </Stack>
  );
}
