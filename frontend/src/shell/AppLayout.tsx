import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import HomeIcon from '@mui/icons-material/Home';
import SettingsIcon from '@mui/icons-material/Settings';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import { Box, Divider, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography } from '@mui/material';
import { NavLink, Outlet } from 'react-router-dom';

const navItems = [
  { label: 'Home', path: '/', icon: <HomeIcon /> },
  { label: 'Calendar', path: '/calendar', icon: <CalendarMonthIcon /> },
  { label: 'Weather', path: '/weather', icon: <WbSunnyIcon /> },
  { label: 'Cycling', path: '/cycling', icon: <DirectionsBikeIcon /> },
  { label: 'Settings', path: '/settings', icon: <SettingsIcon /> },
];

const drawerWidth = 212;

export function AppLayout() {
  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      <Drawer
        variant="permanent"
        sx={{
          width: drawerWidth,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: drawerWidth,
            boxSizing: 'border-box',
            borderRight: '1px solid rgba(255,255,255,0.08)',
            backgroundColor: '#0b1016',
          },
        }}
      >
        <Toolbar sx={{ minHeight: 88, px: 3 }}>
          <Box>
            <Typography fontWeight={800} fontSize="1.15rem">
              BurnMetrix
            </Typography>
            <Typography color="text.secondary" fontSize="0.8rem">
              Dashboard
            </Typography>
          </Box>
        </Toolbar>
        <Divider />
        <List sx={{ px: 1.5, py: 2 }}>
          {navItems.map((item) => (
            <ListItemButton
              key={item.path}
              component={NavLink}
              to={item.path}
              end={item.path === '/'}
              sx={{
                minHeight: 58,
                borderRadius: 2,
                mb: 0.75,
                '&.active': {
                  backgroundColor: 'rgba(68, 209, 157, 0.14)',
                  color: 'primary.main',
                },
              }}
            >
              <ListItemIcon sx={{ minWidth: 42, color: 'inherit' }}>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} primaryTypographyProps={{ fontWeight: 700 }} />
            </ListItemButton>
          ))}
        </List>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, minWidth: 0, p: { xs: 2, md: 3 }, pb: 5 }}>
        <Outlet />
      </Box>
    </Box>
  );
}

