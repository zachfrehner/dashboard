import { createBrowserRouter, Navigate } from 'react-router-dom';

import { AppLayout } from './shell/AppLayout';
import { CalendarPage } from './pages/CalendarPage';
import { CaloriesPage } from './pages/CaloriesPage';
import { CyclingPage } from './pages/CyclingPage';
import { HomePage } from './pages/HomePage';
import { NewsPage } from './pages/NewsPage';
import { RideDetailPage } from './pages/RideDetailPage';
import { SettingsPage } from './pages/SettingsPage';
import { WeatherPage } from './pages/WeatherPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'calendar', element: <CalendarPage /> },
      { path: 'weather', element: <WeatherPage /> },
      { path: 'cycling', element: <CyclingPage /> },
      { path: 'calories', element: <CaloriesPage /> },
      { path: 'news', element: <NewsPage /> },
      { path: 'cycling/rides/:rideId', element: <RideDetailPage /> },
      { path: 'settings', element: <SettingsPage /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
]);
