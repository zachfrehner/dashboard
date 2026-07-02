import type { CalendarEvent, CyclingSummary, RideDetail, Settings, WeatherCurrent } from './types';

const now = new Date();
const isoFromNow = (hours: number) => new Date(now.getTime() + hours * 60 * 60 * 1000).toISOString();

export const mockWeather: WeatherCurrent = {
  provider: 'mock',
  condition: 'Clear',
  temperatureF: 72,
  feelsLikeF: 70,
  humidityPercent: 32,
  windMph: 8.4,
  uvIndex: 5,
  observedAt: now.toISOString(),
};

export const mockEvents: CalendarEvent[] = [
  {
    id: 'mock-calendar-1',
    title: 'Recovery spin',
    location: 'Garage trainer',
    startsAt: isoFromNow(2),
    endsAt: isoFromNow(3),
    provider: 'mock',
  },
  {
    id: 'mock-calendar-2',
    title: 'Bike fit notes review',
    location: 'Home',
    startsAt: isoFromNow(26),
    endsAt: isoFromNow(27),
    provider: 'mock',
  },
];

const zones = ['Z1', 'Z2', 'Z3', 'Z4', 'Z5'].map((label, index) => ({
  label,
  percent: [18, 34, 26, 14, 8][index],
}));

export const mockCyclingSummary: CyclingSummary = {
  period: 'WEEK',
  distanceMiles: 108.7,
  rideTimeSeconds: 25_608,
  movingTimeSeconds: 23_056,
  elevationFeet: 4928,
  calories: 3608,
  averageSpeedMph: 16.9,
  averagePowerWatts: 184,
  normalizedPowerWatts: 206,
  ftpWatts: 255,
  averageHeartRateBpm: 142,
  averageCadenceRpm: 86,
  tss: 270.6,
  trainingLoad: 211.2,
  fatCalories: 1342,
  carbCalories: 2266,
  powerZones: zones,
  heartRateZones: [
    { label: 'Easy', percent: 20 },
    { label: 'Endurance', percent: 38 },
    { label: 'Tempo', percent: 24 },
    { label: 'Threshold', percent: 12 },
    { label: 'Max', percent: 6 },
  ],
  recentRides: [
    {
      id: 'ride-1',
      name: 'Foothills tempo loop',
      startedAt: isoFromNow(-24),
      distanceMiles: 24.7,
      elevationFeet: 1120,
    },
    {
      id: 'ride-2',
      name: 'Recovery spin',
      startedAt: isoFromNow(-72),
      distanceMiles: 15.2,
      elevationFeet: 320,
    },
  ],
};

export const mockRideDetail: RideDetail = {
  id: 'ride-1',
  name: 'Foothills tempo loop',
  startedAt: isoFromNow(-24),
  summary: { ...mockCyclingSummary, period: 'TODAY', distanceMiles: 24.7, elevationFeet: 1120 },
  power: [
    { label: '00:00', value: 142 },
    { label: '00:20', value: 188 },
    { label: '00:40', value: 214 },
    { label: '01:00', value: 176 },
  ],
  heartRate: [
    { label: '00:00', value: 118 },
    { label: '00:20', value: 142 },
    { label: '00:40', value: 151 },
    { label: '01:00', value: 136 },
  ],
  elevation: [
    { label: '00:00', value: 4820 },
    { label: '00:20', value: 5120 },
    { label: '00:40', value: 5540 },
    { label: '01:00', value: 5080 },
  ],
  burnMetrixSummary: 'Balanced endurance burn with a moderate carbohydrate load.',
  notes: 'Sample ride detail; replace with imported Strava, FIT, or Garmin data later.',
};

export const mockSettings: Settings = {
  strava: { name: 'Strava', connected: false, provider: 'mock' },
  googleCalendar: { name: 'Google Calendar', connected: false, provider: 'mock' },
  weather: { name: 'Weather API', connected: false, provider: 'mock' },
  displayMode: 'kiosk',
  units: 'imperial',
  version: '0.1.0',
};

