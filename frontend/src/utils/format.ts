export const dash = '-';

export const hasValue = (value: number | string | null | undefined) => value !== null && value !== undefined && value !== '';

export const formatMiles = (value: number | null | undefined) => (hasValue(value) ? `${Number(value).toFixed(1)} mi` : dash);

export const formatFeet = (value: number | null | undefined) => (hasValue(value) ? `${Math.round(Number(value)).toLocaleString()} ft` : dash);

export const formatInteger = (value: number | null | undefined) => (hasValue(value) ? Number(value).toLocaleString() : dash);

export const formatNumberUnit = (value: number | null | undefined, unit: string) => (hasValue(value) ? `${value} ${unit}` : dash);

export const formatDuration = (seconds: number | null | undefined) => {
  if (!hasValue(seconds)) {
    return dash;
  }
  const totalSeconds = Number(seconds);
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.round((totalSeconds % 3600) / 60);
  return `${hours}h ${minutes}m`;
};

export const formatDateTime = (value: string | null | undefined) => {
  if (!value) {
    return dash;
  }

  return new Intl.DateTimeFormat(undefined, {
    weekday: 'short',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
};
