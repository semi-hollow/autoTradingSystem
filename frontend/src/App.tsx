import { Layout, Tabs, Typography } from "antd";
import { PlansView } from "./views/PlansView";
import { OrdersView } from "./views/OrdersView";
import { PositionsView } from "./views/PositionsView";
import { MetricsView } from "./views/MetricsView";

const { Header, Content } = Layout;

function App() {
  const tabItems = [
    {
      key: "plans",
      label: "DCA Plans",
      children: <PlansView />
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
    },
    {
      key: "metrics",
      label: "Latency Metrics",
      children: <MetricsView />
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
          Monitor and operate your automated DCA and strategy orders.
        </Typography.Paragraph>
        <Tabs defaultActiveKey="plans" items={tabItems} />
      </Content>
    </Layout>
  );
}

export default App;
