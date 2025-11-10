import { useMemo, useState } from "react";
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message
} from "antd";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { apiClient } from "../api/client";
import type { Strategy, StrategyRunResponse } from "../api/types";

dayjs.extend(relativeTime);

const strategyTypes = [
  { label: "Fixed Amount DCA", value: "FIXED_AMOUNT_DCA" },
  { label: "Grid Buy", value: "GRID_BUY" }
];

const statusOptions = [
  { label: "Active", value: "ACTIVE" },
  { label: "Paused", value: "PAUSED" },
  { label: "Archived", value: "ARCHIVED" }
];

const defaultCron = "0 0 9 * * MON-FRI";

export function StrategiesView() {
  const queryClient = useQueryClient();
  const [isModalOpen, setModalOpen] = useState(false);

  const { data: strategies, isLoading } = useQuery({
    queryKey: ["strategies"],
    queryFn: async () => {
      const response = await apiClient.get<Strategy[]>("/api/strategies");
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

  const createStrategyMutation = useMutation({
    mutationFn: async (payload: unknown) => {
      const response = await apiClient.post("/api/strategies", payload);
      return response.data;
    },
    onSuccess: () => {
      message.success("Strategy created");
      queryClient.invalidateQueries({ queryKey: ["strategies"] });
      setModalOpen(false);
    },
    onError: () => message.error("Failed to create strategy")
  });

  const runStrategyMutation = useMutation({
    mutationFn: async (strategyId: number) => {
      const response = await apiClient.post<StrategyRunResponse>(
        `/api/strategies/${strategyId}/run-now`
      );
      return response.data;
    },
    onSuccess: (result) => {
      message.success(
        `Triggered ${result.orders.length} order(s) via ${result.strategy.name}`
      );
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      queryClient.invalidateQueries({ queryKey: ["strategies"] });
    },
    onError: () => message.error("Failed to run strategy")
  });

  const accountMap = useMemo(
    () =>
      (accounts ?? []).reduce<Record<number, string>>((acc, account) => {
        acc[account.id] = account.name;
        return acc;
      }, {}),
    [accounts]
  );

  const instrumentMap = useMemo(
    () =>
      (instruments ?? []).reduce<Record<number, string>>((acc, instrument) => {
        acc[instrument.id] = instrument.symbol;
        return acc;
      }, {}),
    [instruments]
  );

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="large">
      <Card
        title="Strategy Studio"
        extra={
          <Button type="primary" onClick={() => setModalOpen(true)}>
            New strategy
          </Button>
        }
      >
        <Table<Strategy>
          rowKey="id"
          dataSource={strategies}
          loading={isLoading}
          pagination={{ pageSize: 6 }}
          expandable={{
            expandedRowRender: (strategy) => (
              <StrategyPreview strategy={strategy} />
            )
          }}
          columns={[
            { title: "Name", dataIndex: "name" },
            {
              title: "Type",
              dataIndex: "type",
              render: (value) => <Tag color="purple">{value}</Tag>
            },
            {
              title: "Account",
              render: (_, strategy) => accountMap[strategy.accountId] ?? strategy.accountId
            },
            {
              title: "Instrument",
              render: (_, strategy) =>
                instrumentMap[strategy.instrumentId] ?? strategy.instrumentId
            },
            { title: "CRON", dataIndex: "cron" },
            {
              title: "Status",
              dataIndex: "status",
              render: (status) => (
                <Tag color={status === "ACTIVE" ? "green" : "orange"}>{status}</Tag>
              )
            },
            {
              title: "Last run",
              dataIndex: "lastRunAt",
              render: (value?: string) => (value ? dayjs(value).fromNow() : "-")
            },
            {
              title: "Actions",
              render: (_, strategy) => (
                <Button
                  size="small"
                  loading={runStrategyMutation.isPending}
                  onClick={() => runStrategyMutation.mutate(strategy.id)}
                >
                  Run now
                </Button>
              )
            }
          ]}
        />
      </Card>

      <StrategyModal
        open={isModalOpen}
        onCancel={() => setModalOpen(false)}
        onCreate={(payload) => createStrategyMutation.mutate(payload)}
        accounts={accounts ?? []}
        instruments={instruments ?? []}
      />
    </Space>
  );
}

function StrategyPreview({ strategy }: { strategy: Strategy }) {
  return (
    <Space direction="vertical" size="small" style={{ width: "100%" }}>
      <Typography.Text type="secondary">Parameters</Typography.Text>
      <pre style={{ margin: 0, background: "#fafafa", padding: 12 }}>
        {JSON.stringify(strategy.parameters, null, 2)}
      </pre>
    </Space>
  );
}

interface StrategyModalProps {
  open: boolean;
  onCancel: () => void;
  onCreate: (payload: unknown) => void;
  accounts: Array<{ id: number; name: string }>;
  instruments: Array<{ id: number; symbol: string }>;
}

function StrategyModal({
  open,
  onCancel,
  onCreate,
  accounts,
  instruments
}: StrategyModalProps) {
  const [form] = Form.useForm();
  const currentType = Form.useWatch("type", form) ?? "FIXED_AMOUNT_DCA";

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        name: values.name,
        description: values.description,
        accountId: values.accountId,
        instrumentId: values.instrumentId,
        type: values.type,
        cron: values.cron,
        status: values.status,
        startAt: values.startAt.toISOString(),
        endAt: values.endAt ? values.endAt.toISOString() : null,
        parameters: values.parameters
      };
      onCreate(payload);
      form.resetFields();
    } catch {
      // handled by form validation
    }
  };

  return (
    <Modal
      open={open}
      title="Create Strategy"
      onOk={handleOk}
      onCancel={() => {
        form.resetFields();
        onCancel();
      }}
      okText="Create"
      destroyOnClose
    >
      <Form
        layout="vertical"
        form={form}
        initialValues={{
          type: "FIXED_AMOUNT_DCA",
          status: "ACTIVE",
          cron: defaultCron,
          startAt: dayjs()
        }}
      >
        <Form.Item label="Name" name="name" rules={[{ required: true }]}>
          <Input />
        </Form.Item>
        <Form.Item label="Description" name="description">
          <Input.TextArea rows={2} />
        </Form.Item>
        <Form.Item label="Account" name="accountId" rules={[{ required: true }]}>
          <Select
            placeholder="Select account"
            options={accounts.map((account) => ({
              label: account.name,
              value: account.id
            }))}
          />
        </Form.Item>
        <Form.Item
          label="Instrument"
          name="instrumentId"
          rules={[{ required: true }]}
        >
          <Select
            placeholder="Select instrument"
            options={instruments.map((instrument) => ({
              label: instrument.symbol,
              value: instrument.id
            }))}
          />
        </Form.Item>
        <Form.Item label="Strategy type" name="type" rules={[{ required: true }]}>
          <Select options={strategyTypes} />
        </Form.Item>
        <Form.Item
          label="CRON expression"
          name="cron"
          rules={[{ required: true }]}
          tooltip="Quartz cron syntax"
        >
          <Input />
        </Form.Item>
        <Form.Item label="Start at" name="startAt" rules={[{ required: true }]}>
          <DatePicker showTime style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="End at" name="endAt">
          <DatePicker showTime style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Status" name="status" rules={[{ required: true }]}>
          <Select options={statusOptions} />
        </Form.Item>
        <StrategyParametersFields type={currentType} />
      </Form>
    </Modal>
  );
}

function StrategyParametersFields({ type }: { type: string }) {
  if (type === "GRID_BUY") {
    return (
      <>
        <Form.Item
          label="Anchor price"
          name={["parameters", "anchorPrice"]}
          rules={[{ required: true }]}
        >
          <InputNumber min={1} precision={2} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Step percent"
          name={["parameters", "stepPercent"]}
          rules={[{ required: true }]}
          tooltip="Percentage drop between each grid level"
        >
          <InputNumber min={0.1} precision={2} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Levels"
          name={["parameters", "levels"]}
          rules={[{ required: true }]}
        >
          <InputNumber min={1} max={10} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Cash per level"
          name={["parameters", "cashPerLevel"]}
          rules={[{ required: true }]}
        >
          <InputNumber min={50} precision={2} style={{ width: "100%" }} />
        </Form.Item>
      </>
    );
  }

  return (
    <Form.Item
      label="Cash amount (USD)"
      name={["parameters", "cashAmount"]}
      rules={[{ required: true }]}
    >
      <InputNumber min={50} precision={2} style={{ width: "100%" }} />
    </Form.Item>
  );
}
