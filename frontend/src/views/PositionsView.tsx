import { Card, Table } from "antd";
import { useQuery } from "@tanstack/react-query";
import { apiClient } from "../api/client";
import type { Position } from "../api/types";

export function PositionsView() {
  const { data: positions, isLoading } = useQuery({
    queryKey: ["positions"],
    queryFn: async () => {
      const response = await apiClient.get<Position[]>("/api/positions");
      return response.data;
    },
    refetchInterval: 5000
  });

  const { data: accounts } = useQuery({
    queryKey: ["accounts"],
    queryFn: async () => {
      const response = await apiClient.get("/api/accounts");
      return response.data as Array<{ id: number; name: string }>;
    }
  });

  const { data: instruments } = useQuery({
    queryKey: ["instruments"],
    queryFn: async () => {
      const response = await apiClient.get("/api/instruments");
      return response.data as Array<{ id: number; symbol: string }>;
    }
  });

  return (
    <Card title="Portfolio Positions">
      <Table<Position>
        rowKey={(position) => `${position.accountId}-${position.instrumentId}`}
        loading={isLoading}
        dataSource={positions}
        pagination={{ pageSize: 8 }}
        columns={[
          {
            title: "Account",
            render: (_, position) =>
              accounts?.find((acc) => acc.id === position.accountId)?.name ??
              position.accountId
          },
          {
            title: "Instrument",
            render: (_, position) =>
              instruments?.find((ins) => ins.id === position.instrumentId)?.symbol ??
              position.instrumentId
          },
          {
            title: "Quantity",
            dataIndex: "qty"
          },
          {
            title: "Average price",
            dataIndex: "avgPrice"
          },
          {
            title: "Updated at",
            dataIndex: "updatedAt"
          }
        ]}
      />
    </Card>
  );
}
