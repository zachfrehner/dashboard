export const formatMiles = (value: number) => `${value.toFixed(1)} mi`;

export const formatFeet = (value: number) => `${Math.round(value).toLocaleString()} ft`;

export const formatDuration = (seconds: number) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.round((seconds % 3600) / 60);
  return `${hours}h ${minutes}m`;
};

export const formatDateTime = (value: string) =>
  new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));

