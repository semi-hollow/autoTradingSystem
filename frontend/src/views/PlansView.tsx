import { useState } from "react";
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
  message
} from "antd";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { apiClient } from "../api/client";
import type { Plan } from "../api/types";

dayjs.extend(relativeTime);

const holidayPolicies = [
  { label: "Skip (不执行)", value: "SKIP" },
  { label: "Next Business Day (顺延)", value: "NEXT_BUSINESS_DAY" }
];

const defaultCron = "0 0 9 * * MON-FRI";

export function PlansView() {
  const queryClient = useQueryClient();
  const [isModalOpen, setModalOpen] = useState(false);

  const { data: plans, isLoading } = useQuery({
    queryKey: ["plans"],
    queryFn: async () => {
      const response = await apiClient.get<Plan[]>("/api/plans");
      return response.data;
    },
    refetchInterval: 3000
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

  const createPlanMutation = useMutation({
    mutationFn: async (payload: unknown) => {
      const response = await apiClient.post("/api/plans", payload);
      return response.data;
    },
    onSuccess: () => {
      message.success("Plan created");
      queryClient.invalidateQueries({ queryKey: ["plans"] });
      setModalOpen(false);
    },
    onError: () => message.error("Failed to create plan")
  });

  const runNowMutation = useMutation({
    mutationFn: async (planId: number) => {
      const response = await apiClient.post(`/api/plans/${planId}/run-now`);
      return response.data;
    },
    onSuccess: () => {
      message.success("Plan triggered");
      queryClient.invalidateQueries({ queryKey: ["plans"] });
    },
    onError: () => message.error("Failed to trigger plan")
  });

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="large">
      <Card
        title="Dollar-Cost Averaging Plans"
        extra={
          <Button type="primary" onClick={() => setModalOpen(true)}>
            New plan
          </Button>
        }
      >
        <Table<Plan>
          rowKey="id"
          dataSource={plans}
          loading={isLoading}
          pagination={{ pageSize: 5 }}
          columns={[
            { title: "Plan ID", dataIndex: "id" },
            {
              title: "Account",
              render: (_, plan) =>
                accounts?.find((acc) => acc.id === plan.accountId)?.name ??
                plan.accountId
            },
            {
              title: "Instrument",
              render: (_, plan) =>
                instruments?.find((ins) => ins.id === plan.instrumentId)?.symbol ??
                plan.instrumentId
            },
            { title: "Cash Amount", dataIndex: "cashAmount" },
            { title: "CRON", dataIndex: "cron" },
            {
              title: "Status",
              dataIndex: "status",
              render: (status) => <Tag color="blue">{status}</Tag>
            },
            {
              title: "Last Run",
              dataIndex: "lastRunAt",
              render: (value?: string) => (value ? dayjs(value).fromNow() : "-")
            },
            {
              title: "Actions",
              render: (_, plan) => (
                <Button
                  size="small"
                  onClick={() => runNowMutation.mutate(plan.id)}
                  loading={runNowMutation.isPending}
                >
                  Run now
                </Button>
              )
            }
          ]}
        />
      </Card>

      <PlanModal
        open={isModalOpen}
        onCancel={() => setModalOpen(false)}
        accounts={accounts ?? []}
        instruments={instruments ?? []}
        onCreate={(values) => createPlanMutation.mutate(values)}
      />
    </Space>
  );
}

interface PlanModalProps {
  open: boolean;
  onCancel: () => void;
  onCreate: (payload: unknown) => void;
  accounts: Array<{ id: number; name: string }>;
  instruments: Array<{ id: number; symbol: string }>;
}

function PlanModal({ open, onCancel, onCreate, accounts, instruments }: PlanModalProps) {
  const [form] = Form.useForm();

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        accountId: values.accountId,
        instrumentId: values.instrumentId,
        cashAmount: values.cashAmount,
        cron: values.cron,
        startAt: values.startAt.toISOString(),
        endAt: values.endAt ? values.endAt.toISOString() : null,
        holidayPolicy: values.holidayPolicy,
        status: "ACTIVE"
      };
      onCreate(payload);
      form.resetFields();
    } catch {
      // validation handled by antd
    }
  };

  return (
    <Modal
      open={open}
      title="Create DCA Plan"
      onCancel={onCancel}
      onOk={handleOk}
      okText="Create"
      destroyOnClose
    >
      <Form
        layout="vertical"
        form={form}
        initialValues={{
          cron: defaultCron,
          holidayPolicy: "NEXT_BUSINESS_DAY",
          startAt: dayjs()
        }}
      >
        <Form.Item label="Account" name="accountId" rules={[{ required: true }]}>
          <Select
            placeholder="Select account"
            options={accounts.map((acc) => ({ label: acc.name, value: acc.id }))}
          />
        </Form.Item>
        <Form.Item label="Instrument" name="instrumentId" rules={[{ required: true }]}>
          <Select
            placeholder="Select instrument"
            options={instruments.map((ins) => ({ label: ins.symbol, value: ins.id }))}
          />
        </Form.Item>
        <Form.Item label="Cash amount (USD)" name="cashAmount" rules={[{ required: true }]}>
          <InputNumber min={50} precision={2} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="CRON expression"
          name="cron"
          tooltip="Quartz CRON syntax, default is 9AM weekdays"
          rules={[{ required: true }]}
        >
          <Input />
        </Form.Item>
        <Form.Item label="Start at" name="startAt" rules={[{ required: true }]}>
          <DatePicker showTime style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="End at" name="endAt">
          <DatePicker showTime style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Holiday policy" name="holidayPolicy" rules={[{ required: true }]}>
          <Select options={holidayPolicies} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
