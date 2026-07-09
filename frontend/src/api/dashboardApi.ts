import { apiClient } from './client';
import { mockCyclingSummary, mockEvents, mockRideDetail, mockSettings, mockWeather } from './mockData';
import type { CalendarEvent, CyclingSummary, RideDetail, Settings, WeatherCurrent } from './types';

const withFallback = async <T>(request: () => Promise<{ data: T }>, fallback: T): Promise<T> => {
  if (import.meta.env.MODE === 'test') {
    return fallback;
  }

  try {
    const response = await request();
    return response.data;
  } catch {
    return fallback;
  }
};

export const getCurrentWeather = () =>
  withFallback<WeatherCurrent>(() => apiClient.get('/api/weather/current'), mockWeather);

export const getCalendarEvents = () =>
  withFallback<CalendarEvent[]>(() => apiClient.get('/api/calendar/events'), mockEvents);

export const getCyclingSummary = (period: string) =>
  withFallback<CyclingSummary>(
    () => apiClient.get(`/api/cycling/${period}`),
    { ...mockCyclingSummary, period: period.toUpperCase() as CyclingSummary['period'] },
  );

export const getRideDetail = (rideId: string) =>
  withFallback<RideDetail>(() => apiClient.get(`/api/cycling/rides/${rideId}`), {
    ...mockRideDetail,
    id: rideId,
  });

export const getSettings = () =>
  withFallback<Settings>(() => apiClient.get('/api/settings'), mockSettings);

export const closeKiosk = async () => {
  if (import.meta.env.MODE === 'test') {
    return { requested: false, message: 'Kiosk close skipped during tests' };
  }

  try {
    const response = await apiClient.post<{ requested: boolean; message: string }>('/api/system/kiosk/close');
    return response.data;
  } catch {
    window.close();
    return { requested: false, message: 'Backend unavailable; browser close requested' };
  }
};
