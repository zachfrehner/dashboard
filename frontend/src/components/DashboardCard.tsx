import { Card, CardContent, Stack, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface DashboardCardProps {
  title: string;
  value?: ReactNode;
  detail?: ReactNode;
  children?: ReactNode;
}

export function DashboardCard({ title, value, detail, children }: DashboardCardProps) {
  return (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Stack spacing={1}>
          <Typography color="text.secondary" fontSize="0.92rem" fontWeight={700}>
            {title}
          </Typography>
          {value && (
            <Typography variant="h2" component="div">
              {value}
            </Typography>
          )}
          {detail && <Typography color="text.secondary">{detail}</Typography>}
          {children}
        </Stack>
      </CardContent>
    </Card>
  );
}

