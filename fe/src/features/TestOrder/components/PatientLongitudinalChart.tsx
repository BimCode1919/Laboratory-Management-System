// src/features/TestOrder/components/PatientLongitudinalChart.tsx

import { useEffect, useState } from "react";
import type {
  AvailableParameter,
  TestResultTrendDTO,
} from "../types/TestResults";
import {
  Container,
  Paper,
  Box,
  Typography,
  Tabs,
  Tab,
  TextField,
} from "@mui/material";

import dayjs from "dayjs";
import { Dayjs } from "dayjs";
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  Line,
  ComposedChart,
  Legend,
} from "recharts";
import {
  getAvailableParameters,
  getPatientResultTrendData,
} from "../services/TestResultServices";

const DEFAULT_TAB = "HEMATOCRIT";

interface PatientLongitudinalChartProps {
  patientId: string;
}

const transformData = (data: any[]) => {
  return data.map((item) => {
    const low =
      item.referenceLow != null ? Number(item.referenceLow) : undefined;
    const high =
      item.referenceHigh != null ? Number(item.referenceHigh) : undefined;

    return {
      date: dayjs(item.orderCreatedAt).valueOf(),
      low,
      high,
      range: low != null && high != null ? high - low : undefined,
      result: Number(item.resultValue),
      unit: item.unit || "",
      parameterName: item.parameterName,
      alertLevel: item.alertLevel,
    };
  });
};

export default function PatientLongitudinalChart({
  patientId,
}: PatientLongitudinalChartProps) {
  const [tabNames, setTabNames] = useState<AvailableParameter[]>([]);
  const [activeTab, setActiveTab] = useState<string>(DEFAULT_TAB);
  const [chartData, setChartData] = useState<TestResultTrendDTO[]>([]);
  const [unit, setUnit] = useState<string>("");

  const [loading, setLoading] = useState(true);
  const [dataError, setDataError] = useState<string | null>(null);

  const [startTime, setStartTime] = useState<Dayjs>(
    dayjs().subtract(6, "month")
  );
  const [endTime, setEndTime] = useState<Dayjs>(dayjs().add(5, "day"));

  const handleTabChange = (_event: React.SyntheticEvent, newValue: string) => {
    setActiveTab(newValue);
  };

  const handleStartDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = dayjs(e.target.value);
    if (newDate.isValid()) setStartTime(newDate.startOf("day"));
  };

  const handleEndDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = dayjs(e.target.value);
    if (newDate.isValid()) setEndTime(newDate.endOf("day"));
  };

  useEffect(() => {
    if (!patientId) return;
    fetchTabs(patientId);
  }, [patientId]);

  const fetchTabs = async (patientId: string) => {
    try {
      const names = await getAvailableParameters(patientId);
      if (names && names.length > 0) {
        setTabNames(names);
        const initial = names.includes(DEFAULT_TAB) ? DEFAULT_TAB : names[0];
        setActiveTab(initial);
      } else {
        setTabNames([]);
        setActiveTab("");
      }
    } catch (err) {
      console.error("Error fetching parameters:", err);
    }
  };

  useEffect(() => {
    if (!patientId || !activeTab) return;
    fetchChartData(patientId, startTime, endTime, activeTab);
  }, [patientId, activeTab, startTime, endTime]);

  const fetchChartData = async (
    patientId: string,
    startTime: Dayjs,
    endTime: Dayjs,
    activeTab: string
  ) => {
    setDataError(null);
    setLoading(true);

    const start = startTime.isValid()
      ? startTime.toISOString()
      : dayjs().subtract(1, "year").toISOString();
    const end = endTime.isValid()
      ? endTime.toISOString()
      : dayjs().toISOString();

    try {
      const data = await getPatientResultTrendData(
        patientId,
        start,
        end,
        activeTab
      );

      if (data && data.length > 0) {
        const transformed = transformData(data);
        console.log("Raw API data:", data);
        console.table(transformed);
        setChartData(transformed);
        setUnit(data[0].unit || "");
      } else {
        console.log("No trend data returned for", activeTab);
        setChartData([]);
        setUnit("");
      }
    } catch (err) {
      console.error("Error fetching trend data:", err);
      setDataError(`Failed to fetch trend data for ${activeTab}.`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4 }}>
      <Paper elevation={4} sx={{ borderRadius: 2, overflow: "hidden", p: 3 }}>
        <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>
          Patient Longitudinal Trend Chart
        </Typography>

        <Box
          sx={{
            borderBottom: 1,
            borderColor: "divider",
            bgcolor: "grey.50",
            mb: 3,
          }}
        >
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            aria-label="parameter tabs"
            variant="scrollable"
            scrollButtons="auto"
          >
            {tabNames.map((name) => (
              <Tab
                key={name}
                label={name.toUpperCase()}
                value={name}
                sx={{ fontWeight: 600, textTransform: "none" }}
              />
            ))}
          </Tabs>
        </Box>

        <Box sx={{ display: "flex", gap: 2, mb: 3 }}>
          <TextField
            label="Start Date"
            type="date"
            value={startTime.format("YYYY-MM-DD")}
            onChange={handleStartDateChange}
            InputLabelProps={{ shrink: true }}
          />
          <TextField
            label="End Date"
            type="date"
            value={endTime.format("YYYY-MM-DD")}
            onChange={handleEndDateChange}
            InputLabelProps={{ shrink: true }}
          />
        </Box>

        <Box sx={{ height: 400 }}>
          <ResponsiveContainer width="100%" height="100%">
            <ComposedChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis
                dataKey="date"
                type="number"
                scale="time"
                domain={[
                  (dataMin: number) => dataMin - 24 * 60 * 60 * 1000,
                  (dataMax: number) => dataMax + 24 * 60 * 60 * 1000,
                ]}
                tickFormatter={(tick) => dayjs(tick).format("MMM D, YYYY")}
              />
              <YAxis
                domain={[
                  (dataMin: number) => dataMin - 2,
                  (dataMax: number) => dataMax + 2,
                ]}
                label={{
                  value: unit,
                  angle: -90,
                  position: "insideLeft",
                  style: { textAnchor: "middle", fontWeight: 600 },
                }}
              />
              <Tooltip />
              <Legend verticalAlign="top" align="right" />
              <Area
                type="monotone"
                dataKey="low"
                stackId="range"
                stroke="none"
                fill="transparent"
                isAnimationActive={false}
              />
              <Area
                type="monotone"
                dataKey="range"
                stackId="range"
                stroke="none"
                fill="rgba(46, 204, 113, 0.25)"
                isAnimationActive={false}
              />
              <Line
                type="monotone"
                dataKey="result"
                stroke="#1976d2"
                strokeWidth={3}
                dot={{ r: 6 }}
              />
            </ComposedChart>
          </ResponsiveContainer>
        </Box>
      </Paper>
    </Container>
  );
}
