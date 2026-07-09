import { Grid, Slider, Stack, TextField, Typography } from '@mui/material';
import { useMemo, useState } from 'react';
import { Pie, PieChart, ResponsiveContainer, Cell, Tooltip } from 'recharts';

import { DashboardCard } from '../components/DashboardCard';
import { MetricGrid } from '../components/MetricGrid';
import { PageHeader } from '../components/PageHeader';

const colors = ['#44d19d', '#5fb7ff'];

export function CaloriesPage() {
  const [weight, setWeight] = useState(185);
  const [duration, setDuration] = useState(75);
  const [averagePower, setAveragePower] = useState(185);
  const [intensity, setIntensity] = useState(72);

  const result = useMemo(() => {
    const workKilojoules = averagePower * duration * 60 / 1000;
    const totalCalories = Math.round(workKilojoules * 1.04);
    const carbRatio = Math.min(0.9, Math.max(0.35, intensity / 100));
    const carbCalories = Math.round(totalCalories * carbRatio);
    const fatCalories = totalCalories - carbCalories;
    const carbsGrams = Math.round(carbCalories / 4);
    const fatGrams = Math.round(fatCalories / 9);
    const caloriesPerHour = Math.round(totalCalories / (duration / 60));
    const wattsPerKg = averagePower / (weight / 2.20462);

    return {
      totalCalories,
      carbCalories,
      fatCalories,
      carbsGrams,
      fatGrams,
      caloriesPerHour,
      wattsPerKg,
      chart: [
        { name: 'Fat', value: fatCalories },
        { name: 'Carb', value: carbCalories },
      ],
    };
  }, [averagePower, duration, intensity, weight]);

  return (
    <Stack spacing={3}>
      <PageHeader title="Calories" subtitle="BurnMetrix ride fuel calculator" />
      <Grid container spacing={2}>
        <Grid item xs={12} md={5}>
          <DashboardCard title="Ride Inputs">
            <Stack spacing={3}>
              <NumberInput label="Body Weight" suffix="lb" value={weight} onChange={setWeight} />
              <NumberInput label="Ride Duration" suffix="min" value={duration} onChange={setDuration} />
              <NumberInput label="Average Power" suffix="W" value={averagePower} onChange={setAveragePower} />
              <Stack spacing={1}>
                <Typography fontWeight={700}>Intensity</Typography>
                <Slider
                  value={intensity}
                  min={35}
                  max={95}
                  step={1}
                  valueLabelDisplay="auto"
                  onChange={(_, value) => setIntensity(value as number)}
                />
                <Typography color="text.secondary">{intensity}% carbohydrate bias</Typography>
              </Stack>
            </Stack>
          </DashboardCard>
        </Grid>
        <Grid item xs={12} md={7}>
          <DashboardCard title="Fuel Split" value={`${result.totalCalories.toLocaleString()} kcal`} detail={`${result.caloriesPerHour.toLocaleString()} kcal/hour`}>
            <Stack direction={{ xs: 'column', md: 'row' }} alignItems="center" spacing={2}>
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie data={result.chart} dataKey="value" nameKey="name" innerRadius={62} outerRadius={96} paddingAngle={3}>
                    {result.chart.map((entry, index) => (
                      <Cell key={entry.name} fill={colors[index]} />
                    ))}
                  </Pie>
                  <Tooltip formatter={(value) => [`${Number(value).toLocaleString()} kcal`, '']} />
                </PieChart>
              </ResponsiveContainer>
              <Stack spacing={1} sx={{ minWidth: 180 }}>
                <Typography fontWeight={800}>Fat: {result.fatCalories.toLocaleString()} kcal</Typography>
                <Typography color="text.secondary">{result.fatGrams} g estimated</Typography>
                <Typography fontWeight={800}>Carb: {result.carbCalories.toLocaleString()} kcal</Typography>
                <Typography color="text.secondary">{result.carbsGrams} g estimated</Typography>
              </Stack>
            </Stack>
          </DashboardCard>
        </Grid>
      </Grid>
      <MetricGrid
        metrics={[
          { title: 'Total Burn', value: `${result.totalCalories.toLocaleString()} kcal` },
          { title: 'Calories / Hour', value: `${result.caloriesPerHour.toLocaleString()}` },
          { title: 'Carb Calories', value: result.carbCalories.toLocaleString() },
          { title: 'Fat Calories', value: result.fatCalories.toLocaleString() },
          { title: 'Carbs Needed', value: `${result.carbsGrams} g` },
          { title: 'Fat Oxidized', value: `${result.fatGrams} g` },
          { title: 'Watts / kg', value: result.wattsPerKg.toFixed(2) },
          { title: 'Work', value: `${Math.round(averagePower * duration * 60 / 1000)} kJ` },
        ]}
      />
    </Stack>
  );
}

interface NumberInputProps {
  label: string;
  suffix: string;
  value: number;
  onChange: (value: number) => void;
}

function NumberInput({ label, suffix, value, onChange }: NumberInputProps) {
  return (
    <TextField
      label={label}
      value={value}
      type="number"
      onChange={(event) => onChange(Number(event.target.value))}
      InputProps={{ endAdornment: <Typography color="text.secondary">{suffix}</Typography> }}
      fullWidth
    />
  );
}
