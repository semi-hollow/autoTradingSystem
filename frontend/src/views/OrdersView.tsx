import { useState } from "react";
import {
  Button,
  Card,
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
import { apiClient } from "../api/client";
import type { Execution, Order } from "../api/types";

const orderSides = [
  { label: "Buy", value: "BUY" },
  { label: "Sell", value: "SELL" }
];

const orderTypes = [
  { label: "Market", value: "MARKET" },
  { label: "Limit", value: "LIMIT" }
];

export function OrdersView() {
  const queryClient = useQueryClient();
  const [openModal, setOpenModal] = useState(false);

  const { data: orders, isLoading } = useQuery({
    queryKey: ["orders"],
    queryFn: async () => {
      const response = await apiClient.get<Order[]>("/api/orders");
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

  const createOrderMutation = useMutation({
    mutationFn: async (payload: unknown) => {
      const response = await apiClient.post("/api/orders", payload);
      return response.data;
    },
    onSuccess: () => {
      message.success("Order submitted");
      queryClient.invalidateQueries({ queryKey: ["orders"] });
      setOpenModal(false);
    },
    onError: () => message.error("Failed to submit order")
  });

  const cancelOrderMutation = useMutation({
    mutationFn: async (orderId: number) => {
      const response = await apiClient.post(`/api/orders/${orderId}/cancel`);
      return response.data;
    },
    onSuccess: () => {
      message.success("Order canceled");
      queryClient.invalidateQueries({ queryKey: ["orders"] });
    },
    onError: () => message.error("Failed to cancel order")
  });

  return (
    <Space direction="vertical" style={{ width: "100%" }} size="large">
      <Card
        title="Strategy Orders"
        extra={
          <Button type="primary" onClick={() => setOpenModal(true)}>
            New order
          </Button>
        }
      >
        <Table<Order>
          rowKey="id"
          loading={isLoading}
          dataSource={orders}
          pagination={{ pageSize: 8 }}
          expandable={{
            expandedRowRender: (order) => <Executions orderId={order.id} />
          }}
          columns={[
            {
              title: "Order ID",
              dataIndex: "id"
            },
            {
              title: "Client ID",
              dataIndex: "clientOrderId"
            },
            {
              title: "Account",
              render: (_, order) =>
                accounts?.find((acc) => acc.id === order.accountId)?.name ??
                order.accountId
            },
            {
              title: "Instrument",
              render: (_, order) =>
                instruments?.find((ins) => ins.id === order.instrumentId)?.symbol ??
                order.instrumentId
            },
            {
              title: "Side",
              dataIndex: "side"
            },
            {
              title: "Type",
              dataIndex: "type"
            },
            {
              title: "Qty",
              dataIndex: "qty"
            },
            {
              title: "Cash",
              dataIndex: "cashAmount"
            },
            {
              title: "Limit",
              dataIndex: "limitPrice"
            },
            {
              title: "Status",
              dataIndex: "status",
              render: (status) => <Tag color="geekblue">{status}</Tag>
            },
            {
              title: "Reason",
              dataIndex: "reason"
            },
            {
              title: "Actions",
              render: (_, order) => (
                <Button
                  size="small"
                  danger
                  disabled={["FILLED", "CANCELED"].includes(order.status)}
                  loading={cancelOrderMutation.isPending}
                  onClick={() => cancelOrderMutation.mutate(order.id)}
                >
                  Cancel
                </Button>
              )
            }
          ]}
        />
      </Card>

      <OrderModal
        open={openModal}
        onCancel={() => setOpenModal(false)}
        accounts={accounts ?? []}
        instruments={instruments ?? []}
        onCreate={(payload) => createOrderMutation.mutate(payload)}
      />
    </Space>
  );
}

function Executions({ orderId }: { orderId: number }) {
  const { data, isLoading } = useQuery({
    queryKey: ["executions", orderId],
    queryFn: async () => {
      const response = await apiClient.get<Execution[]>(`/api/executions`, {
        params: { orderId }
      });
      return response.data;
    },
    refetchInterval: 3000
  });

  if (!data?.length) {
    return <span style={{ marginLeft: 16 }}>No executions yet.</span>;
  }

  return (
    <Table<Execution>
      size="small"
      loading={isLoading}
      pagination={false}
      rowKey="id"
      dataSource={data}
      columns={[
        { title: "Exec ID", dataIndex: "id" },
        { title: "Price", dataIndex: "price" },
        { title: "Qty", dataIndex: "qty" },
        { title: "Fee", dataIndex: "fee" },
        { title: "Timestamp", dataIndex: "timestamp" }
      ]}
    />
  );
}

interface OrderModalProps {
  open: boolean;
  onCancel: () => void;
  onCreate: (payload: unknown) => void;
  accounts: Array<{ id: number; name: string }>;
  instruments: Array<{ id: number; symbol: string }>;
}

function OrderModal({
  open,
  onCancel,
  onCreate,
  accounts,
  instruments
}: OrderModalProps) {
  const [form] = Form.useForm();

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        planId: values.planId || null,
        accountId: values.accountId,
        instrumentId: values.instrumentId,
        side: values.side,
        type: values.type,
        qty: values.qty || null,
        cashAmount: values.cashAmount || null,
        limitPrice: values.limitPrice || null,
        strategy: values.strategy || "MANUAL"
      };
      onCreate(payload);
      form.resetFields();
    } catch {
      // validation messages handled by antd
    }
  };

  return (
    <Modal
      open={open}
      title="Submit Manual Order"
      onCancel={onCancel}
      onOk={handleOk}
      okText="Submit"
      destroyOnClose
    >
      <Form layout="vertical" form={form}>
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
        <Form.Item label="Order side" name="side" rules={[{ required: true }]}>
          <Select options={orderSides} />
        </Form.Item>
        <Form.Item label="Order type" name="type" rules={[{ required: true }]}>
          <Select options={orderTypes} />
        </Form.Item>
        <Form.Item
          label="Quantity"
          name="qty"
          tooltip="Leave blank when using cash amount"
        >
          <InputNumber min={0} precision={4} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Cash amount"
          name="cashAmount"
          tooltip="Used for market orders targeting a fixed notional"
        >
          <InputNumber min={0} precision={2} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item label="Limit price" name="limitPrice">
          <InputNumber min={0} precision={4} style={{ width: "100%" }} />
        </Form.Item>
        <Form.Item
          label="Plan reference (optional)"
          name="planId"
          tooltip="Link to an existing plan"
        >
          <Input />
        </Form.Item>
        <Form.Item label="Strategy tag" name="strategy">
          <Input placeholder="e.g. GRID, MANUAL" />
        </Form.Item>
      </Form>
    </Modal>
  );
}
