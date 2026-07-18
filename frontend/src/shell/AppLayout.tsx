import CalendarMonthIcon from '@mui/icons-material/CalendarMonth';
import DirectionsBikeIcon from '@mui/icons-material/DirectionsBike';
import ArticleIcon from '@mui/icons-material/Article';
import LocalFireDepartmentIcon from '@mui/icons-material/LocalFireDepartment';
import HomeIcon from '@mui/icons-material/Home';
import PowerSettingsNewIcon from '@mui/icons-material/PowerSettingsNew';
import SettingsIcon from '@mui/icons-material/Settings';
import WbSunnyIcon from '@mui/icons-material/WbSunny';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Drawer, List, ListItemButton, ListItemIcon, ListItemText, Toolbar, Typography } from '@mui/material';
import { useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { closeKiosk } from '../api/dashboardApi';

const navItems = [
  { label: 'Home', path: '/', icon: <HomeIcon /> },
  { label: 'Calendar', path: '/calendar', icon: <CalendarMonthIcon /> },
  { label: 'Weather', path: '/weather', icon: <WbSunnyIcon /> },
  { label: 'Cycling', path: '/cycling', icon: <DirectionsBikeIcon /> },
  { label: 'Calories', path: '/calories', icon: <LocalFireDepartmentIcon /> },
  { label: 'News', path: '/news', icon: <ArticleIcon /> },
  { label: 'Settings', path: '/settings', icon: <SettingsIcon /> },
];

const drawerWidth = 212;

export function AppLayout() {
  const [confirmClose, setConfirmClose] = useState(false);

  const handleCloseKiosk = async () => {
    await closeKiosk();
    setConfirmClose(false);
  };

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
            display: 'flex',
          },
        }}
      >
        <Toolbar sx={{ minHeight: 88, px: 3 }}>
          <Box>
            <Typography fontWeight={800} fontSize="1.15rem">
              Frehner Home
            </Typography>
            <Typography color="text.secondary" fontSize="0.8rem">
              Dashboard
            </Typography>
          </Box>
        </Toolbar>
        <Divider />
        <List sx={{ px: 1.5, py: 2, flexGrow: 1 }}>
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
        <Box sx={{ p: 1.5 }}>
          <Button
            fullWidth
            color="error"
            variant="outlined"
            size="large"
            startIcon={<PowerSettingsNewIcon />}
            onClick={() => setConfirmClose(true)}
          >
            Close
          </Button>
        </Box>
      </Drawer>
      <Box component="main" sx={{ flexGrow: 1, minWidth: 0, p: { xs: 2, md: 3 }, pb: 5 }}>
        <Outlet />
      </Box>
      <Dialog open={confirmClose} onClose={() => setConfirmClose(false)}>
        <DialogTitle>Close BurnMetrix?</DialogTitle>
        <DialogContent>
          <Typography color="text.secondary">
            This closes the kiosk browser. Start it again from the Pi with burnmetrix-start.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmClose(false)}>Cancel</Button>
          <Button color="error" variant="contained" onClick={handleCloseKiosk}>
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
