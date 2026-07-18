import { apiClient } from './client';
import type { CalendarEvent, CyclingSummary, MetabolicActivity, MetabolicAnalysis, MetabolicStatus, RideDetail, Settings, WeatherCurrent } from './types';

export const getCurrentWeather = async () => {
  const response = await apiClient.get<WeatherCurrent>('/api/weather/current');
  return response.data;
};

export const getCalendarEvents = async () => {
  const response = await apiClient.get<CalendarEvent[]>('/api/calendar/events');
  return response.data;
};

export const getCyclingSummary = async (period: string) => {
  const response = await apiClient.get<CyclingSummary>(`/api/cycling/${period}`);
  return response.data;
};

export const getRideDetail = async (rideId: string) => {
  const response = await apiClient.get<RideDetail>(`/api/cycling/rides/${rideId}`);
  return response.data;
};

export const getSettings = async () => {
  const response = await apiClient.get<Settings>('/api/settings');
  return response.data;
};

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

export const getMetabolicStatus = async () => {
  const response = await apiClient.get<MetabolicStatus>('/api/metabolic/status');
  return response.data;
};

export const getMetabolicActivities = async () => {
  const response = await apiClient.get<MetabolicActivity[]>('/api/metabolic/activities');
  return response.data;
};

export const analyzeMetabolicActivity = async (activityId: number | string) => {
  const response = await apiClient.get<MetabolicAnalysis>(`/api/metabolic/analyze/${activityId}`);
  return response.data;
};

export const updateMetabolicDescription = async (activityId: number | string, report: string) => {
  const response = await apiClient.post<{ ok: boolean }>(`/api/metabolic/update-description/${activityId}`, { report });
  return response.data;
};
