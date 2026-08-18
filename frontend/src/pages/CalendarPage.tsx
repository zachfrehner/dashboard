import { Box, Chip, Grid, Stack, Typography } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useMemo } from 'react';
import type { CalendarEvent } from '../api/types';
import { getCalendarEvents } from '../api/dashboardApi';
import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';
import { dash, formatDateTime } from '../utils/format';

const weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

export function CalendarPage() {
  const events = useQuery({ queryKey: ['calendar', 'events'], queryFn: getCalendarEvents });
  const today = new Date();
  const monthStart = new Date(today.getFullYear(), today.getMonth(), 1);
  const monthLabel = new Intl.DateTimeFormat(undefined, { month: 'long', year: 'numeric' }).format(monthStart);
  const calendarDays = useMemo(() => monthDays(today), [today.getFullYear(), today.getMonth()]);
  const eventsByDay = useMemo(() => groupEventsByDay(events.data ?? []), [events.data]);
  const upcomingEvents = [...(events.data ?? [])].sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime()).slice(0, 8);

  return (
    <Stack spacing={3}>
      <PageHeader title="Calendar" subtitle="Current month and upcoming activities" />
      <Grid container spacing={2}>
        <Grid item xs={12} md={4}>
          <DashboardCard title="Upcoming">
            <Stack spacing={2}>
              {upcomingEvents.length === 0 && (
                <Typography color="text.secondary">{dash}</Typography>
              )}
              {upcomingEvents.map((event) => (
                <Box key={event.id} sx={{ borderLeft: '4px solid', borderColor: 'primary.main', pl: 2 }}>
                  <Typography fontWeight={800}>{event.title || dash}</Typography>
                  <Typography color="text.secondary">{formatDateTime(event.startsAt)}</Typography>
                  <Typography color="text.secondary">{event.location || dash}</Typography>
                </Box>
              ))}
            </Stack>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={8}>
          <DashboardCard title={monthLabel}>
            <Box
              sx={{
                display: 'grid',
                gridTemplateColumns: 'repeat(7, minmax(0, 1fr))',
                gap: 0.75,
              }}
            >
              {weekdays.map((day) => (
                <Typography key={day} color="text.secondary" fontSize="0.78rem" fontWeight={800} textAlign="center">
                  {day}
                </Typography>
              ))}
              {calendarDays.map((day) => (
                <MonthDay
                  key={day.key}
                  day={day}
                  events={eventsByDay.get(day.key) ?? []}
                  isToday={day.key === dateKey(today)}
                />
              ))}
            </Box>
          </DashboardCard>
        </Grid>
      </Grid>
    </Stack>
  );
}

interface CalendarDay {
  date: Date;
  key: string;
  inMonth: boolean;
}

function MonthDay({ day, events, isToday }: { day: CalendarDay; events: CalendarEvent[]; isToday: boolean }) {
  const visibleEvents = events.slice(0, 2);
  const extraEvents = events.length - visibleEvents.length;

  return (
    <Box
      sx={{
        minHeight: { xs: 86, md: 104 },
        border: '1px solid',
        borderColor: isToday ? 'primary.main' : 'rgba(255,255,255,0.08)',
        borderRadius: 1,
        p: 0.75,
        bgcolor: isToday ? 'rgba(68, 209, 157, 0.12)' : 'rgba(255,255,255,0.02)',
        color: day.inMonth ? 'text.primary' : 'text.disabled',
        overflow: 'hidden',
      }}
    >
      <Stack spacing={0.5} sx={{ minWidth: 0 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Typography fontWeight={800} fontSize="0.88rem">
            {day.date.getDate()}
          </Typography>
          {isToday && <Chip label="Today" color="primary" size="small" sx={{ height: 20, fontSize: '0.68rem' }} />}
        </Stack>
        {visibleEvents.map((event) => (
          <Box
            key={event.id}
            sx={{
              minWidth: 0,
              borderRadius: 0.75,
              px: 0.75,
              py: 0.35,
              bgcolor: 'rgba(95, 183, 255, 0.16)',
              color: day.inMonth ? 'text.primary' : 'text.secondary',
            }}
          >
            <Typography noWrap fontSize="0.72rem" fontWeight={800}>
              {event.title || dash}
            </Typography>
          </Box>
        ))}
        {extraEvents > 0 && (
          <Typography color="text.secondary" fontSize="0.72rem" fontWeight={800}>
            +{extraEvents} more
          </Typography>
        )}
      </Stack>
    </Box>
  );
}

function monthDays(anchor: Date) {
  const firstOfMonth = new Date(anchor.getFullYear(), anchor.getMonth(), 1);
  const gridStart = new Date(firstOfMonth);
  gridStart.setDate(firstOfMonth.getDate() - firstOfMonth.getDay());
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(gridStart);
    date.setDate(gridStart.getDate() + index);
    return {
      date,
      key: dateKey(date),
      inMonth: date.getMonth() === anchor.getMonth(),
    };
  });
}

function groupEventsByDay(events: CalendarEvent[]) {
  const grouped = new Map<string, CalendarEvent[]>();
  for (const event of events) {
    const key = dateKey(new Date(event.startsAt));
    grouped.set(key, [...(grouped.get(key) ?? []), event]);
  }
  for (const [key, value] of grouped) {
    grouped.set(key, value.sort((a, b) => new Date(a.startsAt).getTime() - new Date(b.startsAt).getTime()));
  }
  return grouped;
}

function dateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
