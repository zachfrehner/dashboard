export interface WeatherCurrent {
  provider: string;
  condition: string;
  temperatureF: number;
  feelsLikeF: number;
  humidityPercent: number;
  windMph: number;
  uvIndex: number;
  observedAt: string;
  sunrise?: string;
  sunset?: string;
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

export interface MetabolicStatus {
  configured: boolean;
  connected: boolean;
  athlete: null | {
    id: number;
    firstname: string;
    lastname: string;
  };
}

export interface MetabolicActivity {
  id: number;
  name: string;
  sportType: string;
  startDateLocal: string;
  movingTime: number;
  distance: number;
  hasHeartrate: boolean;
  averageHeartrate: number | null;
  maxHeartrate: number | null;
}

export interface MetabolicMetrics {
  fatCalories: number;
  carbohydrateCalories: number;
  totalCalories: number;
  maxPower: number | null;
  gramsOfFatBurned: number;
  gramsOfCarbohydrateBurned: number;
  fatOxidationRateGPerHr: number;
  carbOxidationRateGPerHr: number;
  estimatedGlycogenDepletion: number;
  remainingGlycogen: number;
  timeUntilBonk: string;
  fuelingRecommendation: string;
  fuelDeficitOverRide: number;
  efficiencyDrift: number | null;
  heartRateDecoupling: number | null;
  aerobicEfficiency: string;
  metabolicFlexibilityScore: number;
  personalizedZone2Range: string;
}

export interface MetabolicChartSample {
  second: number;
  heartRate: number;
  fatGPerMin: number;
  carbGPerMin: number;
}

export interface MetabolicAnalysis {
  activity: {
    id: number;
    name: string;
    description: string;
    movingTime: number;
    distance: number;
  };
  metrics: MetabolicMetrics;
  report: string;
  chartSamples: MetabolicChartSample[];
  sampleCount: number;
  labRows: number;
}
