import ArticleIcon from '@mui/icons-material/Article';
import PublicIcon from '@mui/icons-material/Public';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { Box, Chip, Grid, Stack, Typography } from '@mui/material';

import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';

const leadStory = {
  title: 'Top news feed ready for API integration',
  source: 'BurnMetrix News',
  time: 'Live soon',
  summary:
    'This screen is structured for a proper headline feed with source, recency, category, and short summaries once a news API or webhook is connected.',
};

const stories = [
  { title: 'Cycling and endurance headlines', source: 'Sports', time: 'Placeholder', category: 'Cycling' },
  { title: 'Local alerts and weather impacts', source: 'Local', time: 'Placeholder', category: 'Local' },
  { title: 'Technology and health updates', source: 'General', time: 'Placeholder', category: 'Health' },
  { title: 'Market and world brief', source: 'World', time: 'Placeholder', category: 'World' },
  { title: 'Training science notes', source: 'Performance', time: 'Placeholder', category: 'Training' },
];

const categories = ['Top', 'Local', 'Cycling', 'Health', 'World'];

export function NewsPage() {
  return (
    <Stack spacing={3}>
      <PageHeader title="News" subtitle="Top stories, local context, and cycling headlines" />
      <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
        {categories.map((category, index) => (
          <Chip key={category} color={index === 0 ? 'primary' : 'default'} label={category} />
        ))}
      </Stack>

      <Grid container spacing={2}>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Lead Story">
            <Stack spacing={2}>
              <Stack direction="row" spacing={1.5} alignItems="center">
                <Box sx={{ color: 'primary.main', display: 'flex' }}>
                  <ArticleIcon />
                </Box>
                <Typography variant="h2">{leadStory.title}</Typography>
              </Stack>
              <Typography color="text.secondary">{leadStory.summary}</Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                <Chip size="small" icon={<PublicIcon />} label={leadStory.source} />
                <Chip size="small" icon={<TrendingUpIcon />} label={leadStory.time} />
              </Stack>
            </Stack>
          </DashboardCard>
        </Grid>

        <Grid item xs={12} md={5}>
          <DashboardCard title="Feed Status" value="Ready">
            <Typography color="text.secondary">
              Next step is choosing a source: NewsAPI, RSS feeds, Home Assistant webhook, or a small backend aggregator.
            </Typography>
          </DashboardCard>
        </Grid>

        <Grid item xs={12}>
          <DashboardCard title="Top Stories">
            <Stack spacing={1.5}>
              {stories.map((story) => (
                <Box
                  key={story.title}
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: { xs: '1fr', sm: '1fr auto' },
                    gap: 1,
                    alignItems: 'center',
                    py: 1,
                    borderBottom: '1px solid rgba(255,255,255,0.08)',
                    '&:last-child': { borderBottom: 0 },
                  }}
                >
                  <Box sx={{ minWidth: 0 }}>
                    <Typography fontWeight={800} noWrap>
                      {story.title}
                    </Typography>
                    <Typography color="text.secondary" noWrap>
                      {story.source} · {story.time}
                    </Typography>
                  </Box>
                  <Chip label={story.category} size="small" />
                </Box>
              ))}
            </Stack>
          </DashboardCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
