import { Card, Table } from "antd";
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/client";
import type { LatencyMetric } from "../api/types";

export function MetricsView() {
  const { data: metrics, isLoading } = useQuery({
    queryKey: ["metrics", "latency"],
    queryFn: async () => {
      const response = await apiClient.get<LatencyMetric[]>("/api/metrics/latency");
      return response.data;
    },
    refetchInterval: 5000
  });

  return (
    <Card title="Latency metrics">
      <Table<LatencyMetric>
        rowKey={(row, index) => `${row.stage}-${index}`}
        loading={isLoading}
        dataSource={metrics}
        pagination={{ pageSize: 10 }}
        columns={[
          { title: "Stage", dataIndex: "stage" },
          { title: "Latency (ms)", dataIndex: "latencyMs" },
          { title: "Timestamp", dataIndex: "timestamp" }
        ]}
      />
    </Card>
  );
}
