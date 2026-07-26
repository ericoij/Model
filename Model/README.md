# Corrected 500 hPa Weather Model

This directory contains the active, repaired version of the original Java
weather-balloon experiment. The files under `../Masters Project/` are retained
as a historical archive and are not used by this implementation.

## What this model is

This is an educational limited-area, single-pressure-level model. It:

- reads NOAA IGRA v2/v2.2 fixed-width sounding records;
- rejects missing and nonphysical values before vector conversion;
- converts IGRA wind speed from tenths of metres per second;
- selects one observation cycle rather than mixing sounding times;
- interpolates wind components, height, temperature, and humidity from the
  eight nearest usable stations;
- transports fields with stable semi-Lagrangian advection;
- applies `f = 2 Ω sin(latitude)` using latitude in radians;
- uses geopotential acceleration `-g ∇Z`;
- integrates Coriolis rotation analytically;
- preserves the full rectangular grid and fixed lateral boundaries; and
- feeds every timestep into the next timestep.

It does not claim to replace a three-dimensional operational forecast model.
It has no vertical motion, moisture physics, radiation, terrain, or modern data
assimilation. Its output should be treated as an explorable 500 hPa experiment.

## Requirements

- Java 17 or newer
- An IGRA fixed-width input file containing at least four valid 500 hPa
  observations from the same analysis cycle

## Compile

From this directory in PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -Xlint:all -d out (Get-ChildItem src/model -Filter *.java | ForEach-Object FullName)
```

## Test

```powershell
java -ea -cp out model.ModelTests
```

The tests cover the original failure modes: unit scaling, missing-value
sentinels, wind-vector conversion, cycle alignment, bounded integration,
step-to-step feedback, and complete file/CSV processing.

## Run a forecast

```powershell
java -cp out model.ForecastRunner C:\path\to\balloon.d forecast-output 9
```

Arguments are:

1. IGRA input path
2. output directory (optional, defaults to `forecast-output`)
3. forecast length from 1 to 48 hours (optional, defaults to 9)

The output contains `analysis-000.csv` plus one CSV for each forecast hour.
Every record includes valid time, position, 500 hPa height, wind speed and
direction, temperature, humidity, and the `u`/`v` wind components.
