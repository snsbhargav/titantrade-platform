import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function Portfolio(){

    const [message, setMessage] = useState("");
    const [portfolio, setPortfolio] = useState(null);
    const [holdings, setHoldings] = useState([]);
    const [quantities, setQuantities] = useState({});

    const getPortfolio = async() => {
        try{
            const response = await api.get("/portfolio");
            setPortfolio(response?.data?.data || null);
            setHoldings(response?.data?.data?.holdings || []);
            
            setMessage(response?.data?.message || "Portfolio loaded successfully");


        } catch(error){
            setMessage(error?.response?.data?.message ?? "Failed to load portfolio");
        }
    }

    useEffect(() => {
        
        getPortfolio();
    }, []); 

    const handleQuantityChange = (stockId, quantity) =>{
        setQuantities({
            ...quantities,
            [stockId] : Number(quantity)
        });

    }

    const handleSellRequest = async (stockId) => {
        const quantity = quantities[stockId];
        if(!quantity || quantity <=0)
            setMessage("Please enter a valid quantity");
        else{
            const sellRequest = {"stockId" : stockId, "quantity" : quantity};
            try{
                const response = await api.post("/trades/sell", sellRequest);
                setMessage(response?.data?.message || "Stock Sold Successfulll");
                setQuantities({
                    ...quantities,
                    [stockId] : ""
                });
                await getPortfolio();
            } catch(error){
                setMessage(error?.response?.data?.message || "Unable to sell the stock");
            }
        }
    }

    return (
        <div>
            {!portfolio && <p>Portfolio Loading ....</p>}
            {portfolio && (
            <>
                <h1>Portfolio</h1>
                <table border="1">
                    <tbody>
                        <tr>
                            <td>totalPortfolioValue</td>
                            <td>{portfolio.totalPortfolioValue}</td>
                        </tr>
                        <tr>
                            <td>totalInvestedValue</td>
                            <td>{portfolio.totalInvestedValue}</td>
                        </tr>
                        <tr>
                            <td>totalUnrealizedProfitLoss</td>
                            <td>{portfolio.totalUnrealizedProfitLoss}</td>
                        </tr>
                        <tr>
                            <td>totalUnrealizedProfitLossPercentage</td>
                            <td>{portfolio.totalUnrealizedProfitLossPercentage}</td>
                        </tr>
                    </tbody>
                </table>
                <h2>Portfolio Holdings</h2>

                <table className="holdings" border="1">
                    <thead>
                        <tr>
                            <td>Ticker</td>
                            <td>Company Name</td>
                            <td>Quantity</td>
                            <td>Average Buy Price</td>
                            <td>Current Price</td>
                            <td>Market Value</td>
                            <td>Invested Value</td>
                            <td>Unrealised Profit Loss</td>
                            <td>Unrealised Profit Loss Percentage</td>
                            <td>Quantity</td>
                            <td>Actions</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            holdings.map((holding) => (
                                <tr key={holding.stockId}>
                                    <td>{holding.ticker}</td>
                                    <td>{holding.companyName}</td>
                                    <td>{holding.quantity}</td>
                                    <td>{holding.averageBuyPrice}</td>
                                    <td>{holding.currentPrice}</td>
                                    <td>{holding.marketValue}</td>
                                    <td>{holding.investedValue}</td>
                                    <td>{holding.unrealizedProfitLoss}</td>
                                    <td>{holding.unrealizedProfitLossPercentage}</td>
                                    <td><input type="number" name="quantity" placeholder="quantity" min="0.000001" step="0.000001" value={quantities[holding.stockId] || ""} onChange={(event)=> handleQuantityChange(holding.stockId, event.target.value)}/></td>
                                    <td><button onClick={() => handleSellRequest(holding.stockId)}>SELL</button></td>
                                </tr>
                            ))
                        }
                    </tbody>

                </table>
                {message && <p>{message}</p>}
            </>
            )}
        </div>
    );
}
export default Portfolio;