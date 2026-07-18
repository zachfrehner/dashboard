import { Box, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { getCalendarEvents } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';
import { dash, formatDateTime } from '../utils/format';

export function CalendarPage() {
  const events = useQuery({ queryKey: ['calendar', 'events'], queryFn: getCalendarEvents });

  return (
    <Stack spacing={3}>
      <PageHeader title="Calendar" subtitle="Agenda and provider-ready calendar views" />
      <Grid container spacing={2}>
        <Grid item xs={12} md={5}>
          <DashboardCard title="Today">
            <Stack spacing={2}>
              {(events.data ?? []).length === 0 && (
                <Typography color="text.secondary">{dash}</Typography>
              )}
              {events.data?.map((event) => (
                <Box key={event.id} sx={{ borderLeft: '4px solid', borderColor: 'primary.main', pl: 2 }}>
                  <Typography fontWeight={800}>{event.title || dash}</Typography>
                  <Typography color="text.secondary">{formatDateTime(event.startsAt)}</Typography>
                  <Typography color="text.secondary">{event.location || dash}</Typography>
                </Box>
              ))}
            </Stack>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Month View">
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(7, minmax(0, 1fr))',
                gap: 1,
                minHeight: 360,
              }}
            >
              {Array.from({ length: 35 }, (_, index) => (
                <Box
                  key={index}
                  sx={{
                    border: '1px solid rgba(255,255,255,0.08)',
                    borderRadius: 1,
                    minHeight: 54,
                    p: 1,
                    color: index < 2 ? 'text.disabled' : 'text.secondary',
                  }}
                >
                  {dash}
                </Box>
              ))}
            </Box>
          </DashboardCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
