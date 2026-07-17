import { Box, Button, Grid, Stack, Tab, Tabs } from '@mui/material';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import { useQuery } from '@tanstack/react-query';
import { Link, useSearchParams } from 'react-router-dom';
import { getCyclingSummary } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';
import { ZoneChart } from '../components/ZoneChart';
import { formatDuration, formatFeet, formatMiles } from '../utils/format';

const periods = ['today', 'week'];

export function CyclingPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const period = searchParams.get('period') ?? 'week';
  const selectedPeriod = periods.includes(period) ? period : 'week';
  const summary = useQuery({ queryKey: ['cycling', selectedPeriod], queryFn: () => getCyclingSummary(selectedPeriod) });
  const data = summary.data;

  return (
    <Stack spacing={3}>
      <PageHeader title="Cycling" subtitle="Training load, ride metrics, energy burn, and zones" />
      <Tabs value={selectedPeriod} onChange={(_, value) => setSearchParams({ period: value })} variant="scrollable">
        {periods.map((item) => (
          <Tab key={item} value={item} label={item[0].toUpperCase() + item.slice(1)} />
        ))}
      </Tabs>
      {data && (
        <>
          <MetricGrid
            metrics={[
              { title: 'Distance', value: formatMiles(data.distanceMiles) },
              { title: 'Ride Time', value: formatDuration(data.rideTimeSeconds) },
              { title: 'Moving Time', value: formatDuration(data.movingTimeSeconds) },
              { title: 'Elevation', value: formatFeet(data.elevationFeet) },
              { title: 'Calories', value: data.calories.toLocaleString() },
              { title: 'Avg Speed', value: `${data.averageSpeedMph} mph` },
              { title: 'Avg Power', value: `${data.averagePowerWatts} W` },
              { title: 'Normalized Power', value: `${data.normalizedPowerWatts} W` },
              { title: 'FTP', value: `${data.ftpWatts} W` },
              { title: 'Heart Rate', value: `${data.averageHeartRateBpm} bpm` },
              { title: 'Cadence', value: `${data.averageCadenceRpm} rpm` },
              { title: 'TSS', value: data.tss },
              { title: 'Training Load', value: data.trainingLoad },
              { title: 'Fat Calories', value: data.fatCalories.toLocaleString() },
              { title: 'Carb Calories', value: data.carbCalories.toLocaleString() },
            ]}
          />
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <DashboardCard title="Power Zones">
                <ZoneChart data={data.powerZones} />
              </DashboardCard>
            </Grid>
            <Grid item xs={12} md={6}>
              <DashboardCard title="Heart Rate Zones">
                <ZoneChart data={data.heartRateZones} />
              </DashboardCard>
            </Grid>
          </Grid>
          <DashboardCard title="Recent Rides">
            <Stack spacing={1.5}>
              {data.recentRides.map((ride) => (
                <Box key={ride.id}>
                  <Button
                    component={Link}
                    to={`/cycling/rides/${ride.id}`}
                    startIcon={<DirectionsBikeIcon />}
                    fullWidth
                    variant="outlined"
                    sx={{ justifyContent: 'space-between', px: 2 }}
                  >
                    {ride.name} · {formatMiles(ride.distanceMiles)} · {formatFeet(ride.elevationFeet)}
                  </Button>
                </Box>
              ))}
            </Stack>
          </DashboardCard>
        </>
      )}
    </Stack>
  );
}
