import ArticleIcon from '@mui/icons-material/Article';
import PublicIcon from '@mui/icons-material/Public';
import TrendingUpIcon from '@mui/icons-material/TrendingUp';
import { Box, Chip, Grid, Stack, Typography } from '@mui/material';

import { DashboardCard } from '../components/DashboardCard';
import { PageHeader } from '../components/PageHeader';
import { dash } from '../utils/format';

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
                <Typography variant="h2">{dash}</Typography>
              </Stack>
              <Typography color="text.secondary">{dash}</Typography>
              <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
                <Chip size="small" icon={<PublicIcon />} label={dash} />
                <Chip size="small" icon={<TrendingUpIcon />} label={dash} />
              </Stack>
            </Stack>
          </DashboardCard>
        </Grid>

        <Grid item xs={12} md={5}>
          <DashboardCard title="Feed Status" value={dash} />
        </Grid>

        <Grid item xs={12}>
          <DashboardCard title="Top Stories">
            <Stack spacing={1.5}>
              {[0, 1, 2, 3, 4].map((story) => (
                <Box
                  key={story}
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
                      {dash}
                    </Typography>
                    <Typography color="text.secondary" noWrap>
                      {dash}
                    </Typography>
                  </Box>
                  <Chip label={dash} size="small" />
                </Box>
              ))}
            </Stack>
          </DashboardCard>
        </Grid>
      </Grid>
    </Stack>
  );
}
