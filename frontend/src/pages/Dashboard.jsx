import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axiosConfig";

function Dashboard(){

    const [balance, setBalance] = useState();
    const [currency, setCurrency] = useState("USD");
    const [message, setMessage] = useState("");
    const [alertType, setAlertType] = useState("info");
    const [portfolio, setPortfolio] = useState(null);

    const fetchWallet = async() => {
        try{
            const response = await api.get("/wallet/walletBalance");
            setBalance(response?.data?.data?.balance || "");
            setCurrency(response?.data?.data?.currency || "USD");
            setMessage("");
        } catch(error){
            setMessage(error?.response?.data?.message || "Failed to retrieve wallet balance")
            setAlertType("error");
        }
    };

    const fetchPortfolio = async()=>{
        try{
            const response = await api.get("/portfolio");
            setPortfolio(response?.data?.data || null);            
            setMessage("");


        } catch(error){
            setMessage(error?.response?.data?.message ?? "Failed to load portfolio");
            setAlertType("error");
        }
    };

    useEffect(()=>{
        fetchWallet();
        fetchPortfolio();
    }, []);

    return (
        <div className="body-content">
            <h1>Dashboard</h1>
            <h2>Welcome to Titan Trade</h2>
            {!portfolio && <p>Failed to load Dashboard cards</p>}
            {portfolio && 
            <>
            <div className="dashboard-cards">
                <div className="card balance-card">
                    <h2>Wallet balance</h2>
                    <h3>{balance} {currency}</h3>
                </div>
                <div className="card portfolio-value-card">
                    <h2>Portfolio Value</h2>
                    <h3>{portfolio.totalPortfolioValue} {currency}</h3>
                </div>
                <div className="card total-invested-card">
                    <h2>Total Invested</h2>
                    <h3>{portfolio.totalInvestedValue} {currency}</h3>
                </div>
                <div className="card unrealized-profitloss-percentage-card">
                    <h2>Unrealized P/L percentage</h2>
                    <h3>{portfolio.totalUnrealizedProfitLossPercentage}%</h3>
                </div>
            </div>
            </>
            }
            <div>
                <h3>Quick Actions</h3>
                <div className="quick-actions">
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