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
        <div className="body-content">
            {!portfolio && <p>Portfolio Loading ....</p>}
            {portfolio && (
            <>
                <h1>Portfolio</h1>
                <table border="1">
                    <tbody>
                        <tr>
                            <th>totalPortfolioValue</th>
                            <td>{portfolio.totalPortfolioValue}</td>
                        </tr>
                        <tr>
                            <th>totalInvestedValue</th>
                            <td>{portfolio.totalInvestedValue}</td>
                        </tr>
                        <tr>
                            <th>totalUnrealizedProfitLoss</th>
                            <td>{portfolio.totalUnrealizedProfitLohs}</td>
                        </tr>
                        <tr>
                            <th>totalUnrealizedProfitLossPercentage</th>
                            <td>{portfolio.totalUnrealizedProfitLossPercentage}</td>
                        </tr>
                    </tbody>
                </table>
                <h2>Portfolio Holdings</h2>

                <table className="holdings" border="1">
                    <thead>
                        <tr>
                            <th>Ticker</th>
                            <th>Company Name</th> 
                            <th>Quantity</th>
                            <th>Average Buy Price</th>
                            <th>Current Price</th>
                            <th>Market Value</th>
                            <th>Invested Value</th>
                            <th>Unrealised Profit Loss</th>
                            <th>Unrealised Profit Loss Percentage</th>
                            <th>Quantity</th>
                            <th>Actions</th>
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