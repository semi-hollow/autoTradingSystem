import { Layout, Tabs, Typography } from "antd";
import { OrdersView } from "./views/OrdersView";
import { PositionsView } from "./views/PositionsView";
import { StrategiesView } from "./views/StrategiesView";

const { Header, Content } = Layout;

function App() {
  const tabItems = [
    {
      key: "strategies",
      label: "Strategy Studio",
      children: <StrategiesView />
    },
    {
      key: "orders",
      label: "Orders & Executions",
      children: <OrdersView />
    },
    {
      key: "positions",
      label: "Positions",
      children: <PositionsView />
    }
  ];

  return (
    <Layout style={{ minHeight: "100%" }}>
      <Header
        style={{
          display: "flex",
          alignItems: "center",
          color: "#fff",
          fontSize: "1.2rem",
          fontWeight: 600
        }}
      >
        Trading MVP Console
      </Header>
      <Content style={{ padding: 24 }}>
        <Typography.Paragraph>
          Focus on reusable strategies, their resulting orders, and live positions—no extra clutter.
        </Typography.Paragraph>
        <Tabs defaultActiveKey="strategies" items={tabItems} />
      </Content>
    </Layout>
  );
}

export default App;
