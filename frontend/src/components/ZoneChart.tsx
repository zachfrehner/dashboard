import { Box, Typography } from '@mui/material';
import { Bar, BarChart, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import type { ZoneBucket } from '../api/types';
import { dash } from '../utils/format';

const colors = ['#44d19d', '#5fb7ff', '#f6bd60', '#ff8f70', '#ff6b6b'];

interface ZoneChartProps {
  data: ZoneBucket[];
}

export function ZoneChart({ data }: ZoneChartProps) {
  if (data.length === 0) {
    return (
      <Box sx={{ width: '100%', height: 220, display: 'grid', placeItems: 'center' }}>
        <Typography color="text.secondary">{dash}</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ width: '100%', height: 220 }}>
      <ResponsiveContainer>
        <BarChart data={data}>
          <XAxis dataKey="label" stroke="#a9b4c2" tickLine={false} axisLine={false} />
          <YAxis stroke="#a9b4c2" tickLine={false} axisLine={false} />
          <Tooltip
            cursor={{ fill: 'rgba(255,255,255,0.05)' }}
            contentStyle={{ background: '#111820', border: '1px solid rgba(255,255,255,0.12)' }}
            formatter={(value) => [`${value}%`, 'Time']}
          />
          <Bar dataKey="percent" radius={[6, 6, 0, 0]}>
            {data.map((entry, index) => (
              <Cell key={entry.label} fill={colors[index % colors.length]} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
      <Typography color="text.secondary" fontSize="0.8rem">
        Percent of time
      </Typography>
    </Box>
  );
}
