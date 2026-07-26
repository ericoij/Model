# Multi-Level Hydrostatic Weather Model

This directory contains the active, repaired successor to the original Java
weather-balloon experiment. The files under `../Masters Project/` remain a
historical archive and are not used by this implementation.

## What it models

The atmosphere is represented on five pressure surfaces: 850, 700, 500, 300,
and 200 hPa. The model:

- downloads current NOAA IGRA v2.2 soundings without external libraries;
- rejects missing and nonphysical fields before calculations;
- converts IGRA wind and humidity units correctly;
- selects a single observation cycle;
- creates a smooth vector analysis from nearby stations;
- applies semi-Lagrangian horizontal transport;
- uses the correct Coriolis parameter and geopotential acceleration;
- diagnoses pressure vertical velocity from three-dimensional continuity;
- transports wind, temperature, and moisture vertically;
- applies pressure-coordinate adiabatic temperature change;
- reconstructs heights with the virtual-temperature hypsometric equation;
- preserves fixed lateral boundaries and zero top/bottom pressure velocity;
- advances every timestep from the preceding state; and
- emits hourly, multi-level CSV forecasts with `u`, `v`, and `omega`.

This is now a genuine multi-level research model, but it is not yet an
operational forecasting system. Operational status would require terrain and
surface coupling, cloud and precipitation microphysics, radiation, observation
error models, cycling data assimilation, ensemble forecasts, and verified
forecast skill against independent observations.

## Requirements

- Java 17 or newer
- Internet access when downloading current NOAA observations

## Compile and test

From this directory in PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -Xlint:all -d out (Get-ChildItem src/model -Filter *.java | ForEach-Object FullName)
java -ea -cp out model.ModelTests
```

The regression suite covers the original unit and missing-data failures,
observation-cycle alignment, hydrostatic ordering, bounded vertical velocity,
top and bottom continuity conditions, timestep feedback, nine-hour stability,
and complete IGRA-to-multi-level-CSV processing.

## Download current observations

```powershell
java -cp out model.NoaaIngest latest-igra.d
```

This downloads the latest sounding from 19 NOAA stations across the contiguous
United States and creates a compact model input.

## Run a forecast

```powershell
java -cp out model.ForecastRunner latest-igra.d forecast-output 9
```

Arguments are:

1. IGRA input path
2. output directory (optional, defaults to `forecast-output`)
3. forecast length from 1 to 48 hours (optional, defaults to 9)

The output contains `analysis-000.csv` and one CSV per forecast hour. Records
contain valid time, location, pressure, height, wind, temperature, humidity,
horizontal wind components, and vertical pressure velocity.
