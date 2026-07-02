import { Grid } from '@mui/material';
import { DashboardCard } from './DashboardCard';

export interface Metric {
  title: string;
  value: string | number;
  detail?: string;
}

interface MetricGridProps {
  metrics: Metric[];
}

export function MetricGrid({ metrics }: MetricGridProps) {
  return (
    <Grid container spacing={2}>
      {metrics.map((metric) => (
        <Grid key={metric.title} item xs={12} sm={6} md={4} lg={3}>
          <DashboardCard title={metric.title} value={metric.value} detail={metric.detail} />
        </Grid>
      ))}
    </Grid>
  );
}

