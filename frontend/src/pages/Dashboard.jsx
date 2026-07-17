import { Link } from "react-router-dom";

function Dashboard(){

    return (
        <div className="body-content">
            <h1>Dashboard</h1>
            <h2>Welcome to Titan Trade</h2>
            <div>
                <h3>Quick Actions</h3>
                <div>
                    <p><Link to="/stocks">Browse Stocks</Link></p>
                    <p><Link to="/portfolio">View Portfolio</Link></p>
                    <p><Link to="/wallet">Manage Wallet</Link></p>
                    <p><Link to="/trades">Trade History</Link></p>
                </div>
            </div>
        </div>
    );
}
export default Dashboard;