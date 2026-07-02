export interface WeatherCurrent {
  provider: string;
  condition: string;
  temperatureF: number;
  feelsLikeF: number;
  humidityPercent: number;
  windMph: number;
  uvIndex: number;
  observedAt: string;
}

export interface CalendarEvent {
  id: string;
  title: string;
  location: string;
  startsAt: string;
  endsAt: string;
  provider: string;
}

export interface ZoneBucket {
  label: string;
  percent: number;
}

export interface RideSummary {
  id: string;
  name: string;
  startedAt: string;
  distanceMiles: number;
  elevationFeet: number;
}

export interface CyclingSummary {
  period: 'TODAY' | 'WEEK' | 'MONTH' | 'YEAR' | 'LIFETIME';
  distanceMiles: number;
  rideTimeSeconds: number;
  movingTimeSeconds: number;
  elevationFeet: number;
  calories: number;
  averageSpeedMph: number;
  averagePowerWatts: number;
  normalizedPowerWatts: number;
  ftpWatts: number;
  averageHeartRateBpm: number;
  averageCadenceRpm: number;
  tss: number;
  trainingLoad: number;
  fatCalories: number;
  carbCalories: number;
  powerZones: ZoneBucket[];
  heartRateZones: ZoneBucket[];
  recentRides: RideSummary[];
}

export interface ChartPoint {
  label: string;
  value: number;
}

export interface RideDetail {
  id: string;
  name: string;
  startedAt: string;
  summary: CyclingSummary;
  power: ChartPoint[];
  heartRate: ChartPoint[];
  elevation: ChartPoint[];
  burnMetrixSummary: string;
  notes: string;
}

export interface IntegrationStatus {
  name: string;
  connected: boolean;
  provider: string;
}

export interface Settings {
  strava: IntegrationStatus;
  googleCalendar: IntegrationStatus;
  weather: IntegrationStatus;
  displayMode: string;
  units: string;
  version: string;
}

