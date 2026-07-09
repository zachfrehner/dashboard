import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import RefreshIcon from '@mui/icons-material/Refresh';
import UploadIcon from '@mui/icons-material/Upload';
import { Alert, Box, Button, Chip, Grid, Stack, Typography } from '@mui/material';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

import { analyzeMetabolicActivity, getMetabolicActivities, getMetabolicStatus, updateMetabolicDescription } from '../api/dashboardApi';
import type { MetabolicActivity, MetabolicAnalysis } from '../api/types';
import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';

export function CaloriesPage() {
  const status = useQuery({ queryKey: ['metabolic', 'status'], queryFn: getMetabolicStatus });
  const activities = useQuery({
    queryKey: ['metabolic', 'activities'],
    queryFn: getMetabolicActivities,
    enabled: Boolean(status.data?.configured && status.data.connected),
    retry: false,
  });
  const analysis = useMutation({ mutationFn: (activityId: number) => analyzeMetabolicActivity(activityId) });
  const updateDescription = useMutation({
    mutationFn: ({ activityId, report }: { activityId: number; report: string }) => updateMetabolicDescription(activityId, report),
  });

  const configured = Boolean(status.data?.configured);
  const connected = Boolean(status.data?.connected);

  return (
    <Stack spacing={3}>
      <PageHeader
        title="Calories"
        subtitle="Strava metabolic analysis from heart-rate streams and lab data"
        action={
          <Button href="/api/metabolic/auth/start" variant="contained" size="large">
            {connected ? 'Reconnect Strava' : 'Connect Strava'}
          </Button>
        }
      />

      {!configured && (
        <Alert severity="warning">
          Add `STRAVA_CLIENT_ID` and `STRAVA_CLIENT_SECRET` before connecting Strava. The Pi installer can run without them; add them later as environment values.
        </Alert>
      )}
      {configured && !connected && <Alert severity="info">Connect Strava to load your latest activities.</Alert>}
      {connected && status.data?.athlete && <Alert severity="success">Connected as {status.data.athlete.firstname} {status.data.athlete.lastname}</Alert>}

      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Latest Strava Activities">
            <Stack spacing={1.5}>
              <Button startIcon={<RefreshIcon />} variant="outlined" onClick={() => activities.refetch()} disabled={!connected}>
                Refresh
              </Button>
              {activities.isError && <Alert severity="error">{readError(activities.error)}</Alert>}
              {(activities.data ?? []).map((activity) => (
                <ActivityButton
                  key={activity.id}
                  activity={activity}
                  selected={analysis.data?.activity.id === activity.id}
                  onAnalyze={() => analysis.mutate(activity.id)}
                />
              ))}
              {connected && activities.data?.length === 0 && <Typography color="text.secondary">No activities returned by Strava.</Typography>}
            </Stack>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={8}>
          <AnalysisPanel
            analysis={analysis.data}
            loading={analysis.isPending}
            error={analysis.isError ? readError(analysis.error) : null}
            updating={updateDescription.isPending}
            updated={updateDescription.isSuccess}
            onUpdate={(result) => updateDescription.mutate({ activityId: result.activity.id, report: result.report })}
          />
        </Grid>
      </Grid>
    </Stack>
  );
}

function ActivityButton({ activity, selected, onAnalyze }: { activity: MetabolicActivity; selected: boolean; onAnalyze: () => void }) {
  return (
    <Button
      fullWidth
      variant={selected ? 'contained' : 'outlined'}
      startIcon={<DirectionsBikeIcon />}
      onClick={onAnalyze}
      sx={{ minHeight: 76, justifyContent: 'flex-start', textAlign: 'left' }}
    >
      <Stack sx={{ width: '100%' }} spacing={0.5}>
        <Typography fontWeight={800}>{activity.name}</Typography>
        <Typography color={selected ? 'inherit' : 'text.secondary'} fontSize="0.85rem">
          {activity.sportType} · {formatDate(activity.startDateLocal)} · {formatDistance(activity.distance)}
        </Typography>
        <Chip
          size="small"
          label={activity.hasHeartrate ? `${Math.round(activity.averageHeartrate ?? 0)} bpm avg` : 'No HR'}
          sx={{ alignSelf: 'flex-start' }}
        />
      </Stack>
    </Button>
  );
}

function AnalysisPanel({
  analysis,
  loading,
  error,
  updating,
  updated,
  onUpdate,
}: {
  analysis?: MetabolicAnalysis;
  loading: boolean;
  error: string | null;
  updating: boolean;
  updated: boolean;
  onUpdate: (analysis: MetabolicAnalysis) => void;
}) {
  if (loading) {
    return <DashboardCard title="Analysis" value="Analyzing..." detail="Comparing heart-rate stream against the lab CSV." />;
  }

  if (error) {
    return <DashboardCard title="Analysis Error" detail={error} />;
  }

  if (!analysis) {
    return <DashboardCard title="Analysis" detail="Select a Strava activity with heart-rate data to calculate fat calories, carb calories, fueling risk, drift, and Zone 2." />;
  }

  const metrics = analysis.metrics;

  return (
    <Stack spacing={2}>
      <MetricGrid
        metrics={[
          { title: 'Total Calories', value: `${round(metrics.totalCalories)} kcal` },
          { title: 'Fat Calories', value: `${round(metrics.fatCalories)} kcal` },
          { title: 'Carb Calories', value: `${round(metrics.carbohydrateCalories)} kcal` },
          { title: 'Max Power', value: metrics.maxPower ? `${round(metrics.maxPower)} W` : 'No data' },
          { title: 'Fat Burned', value: `${round(metrics.gramsOfFatBurned, 1)} g` },
          { title: 'Carbs Burned', value: `${round(metrics.gramsOfCarbohydrateBurned, 1)} g` },
          { title: 'Flex Score', value: `${metrics.metabolicFlexibilityScore}/100` },
          { title: 'Zone 2', value: metrics.personalizedZone2Range },
          { title: 'Fueling', value: metrics.fuelingRecommendation },
          { title: 'Glycogen Left', value: `${round(metrics.remainingGlycogen)} g` },
          { title: 'Time Until Bonk', value: metrics.timeUntilBonk },
          { title: 'Aerobic Efficiency', value: metrics.aerobicEfficiency },
        ]}
      />
      <DashboardCard title="Metabolic Stream">
        <Box sx={{ height: 300 }}>
          <ResponsiveContainer>
            <LineChart data={analysis.chartSamples}>
              <XAxis dataKey="second" stroke="#a9b4c2" tickFormatter={formatDuration} tickLine={false} axisLine={false} />
              <YAxis yAxisId="burn" stroke="#a9b4c2" tickLine={false} axisLine={false} />
              <YAxis yAxisId="hr" orientation="right" stroke="#a9b4c2" tickLine={false} axisLine={false} />
              <Tooltip
                contentStyle={{ background: '#111820', border: '1px solid rgba(255,255,255,0.12)' }}
                labelFormatter={(value) => formatDuration(Number(value))}
              />
              <Line yAxisId="hr" type="monotone" dataKey="heartRate" name="Heart rate" stroke="#ff6b6b" strokeWidth={2.5} dot={false} />
              <Line yAxisId="burn" type="monotone" dataKey="fatGPerMin" name="Fat g/min" stroke="#44d19d" strokeWidth={2.5} dot={false} />
              <Line yAxisId="burn" type="monotone" dataKey="carbGPerMin" name="Carb g/min" stroke="#5fb7ff" strokeWidth={2.5} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </Box>
      </DashboardCard>
      <DashboardCard title="Strava Report">
        <Stack spacing={2}>
          <Box component="pre" sx={{ m: 0, whiteSpace: 'pre-wrap', color: 'text.secondary', fontFamily: 'inherit' }}>
            {analysis.report}
          </Box>
          <Button startIcon={<UploadIcon />} variant="contained" onClick={() => onUpdate(analysis)} disabled={updating}>
            {updated ? 'Updated' : updating ? 'Updating...' : 'Update ride description'}
          </Button>
        </Stack>
      </DashboardCard>
    </Stack>
  );
}

function formatDate(value: string) {
  if (!value) return 'Unknown date';
  return new Date(value).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDistance(meters: number) {
  return `${round((meters || 0) / 1609.344, 1)} mi`;
}

function formatDuration(seconds: number) {
  const minutes = Math.round(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const remainder = minutes % 60;
  return hours > 0 ? `${hours}:${String(remainder).padStart(2, '0')}` : `${minutes}:00`;
}

function round(value: number, digits = 0) {
  return Number(value || 0).toLocaleString(undefined, {
    maximumFractionDigits: digits,
    minimumFractionDigits: digits,
  });
}

function readError(error: unknown) {
  if (error instanceof Error) return error.message;
  return 'Request failed.';
}
