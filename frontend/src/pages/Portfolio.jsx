import { useEffect, useState } from "react";
import api from "../api/axiosConfig";
import Alert from "../components/Alert";

function Portfolio(){

    const [message, setMessage] = useState("");
    const [portfolio, setPortfolio] = useState(null);
    const [holdings, setHoldings] = useState([]);
    const [quantities, setQuantities] = useState({});
    const [alertType, setAlertType] = useState("info");
    const [loading, setLoading] = useState(true);

    const getPortfolio = async() => {
        try{
            setLoading(true);
            const response = await api.get("/portfolio");
            setPortfolio(response?.data?.data || null);
            setHoldings(response?.data?.data?.holdings || []);
            
            setMessage("");


        } catch(error){
            setPortfolio(null);
            setHoldings([]);
            setMessage(error?.response?.data?.message ?? "Failed to load portfolio");
            setAlertType("error");
        } finally{
            setLoading(false);
        }
    }

    useEffect(() => {
        
        getPortfolio();
    }, []); 

    const handleQuantityChange = (stockId, quantity) =>{
        setQuantities({
            ...quantities,
            [stockId] : quantity
        });

    }

    const handleSellRequest = async (stockId) => {
        const quantity = quantities[stockId];
        if(!quantity || Number(quantity) <=0){
            setMessage("Please enter a valid quantity");
            setAlertType("warning");
            return;
        }
        else{
            const sellRequest = {"stockId" : stockId, "quantity" : Number(quantity)};
            try{
                const response = await api.post("/trades/sell", sellRequest);
                setMessage(response?.data?.message || "Stock sold successfully");
                setAlertType("success");
                setQuantities({
                    ...quantities,
                    [stockId] : ""
                });
                await getPortfolio();
            } catch(error){
                setMessage(error?.response?.data?.message || "Unable to sell the stock");
                setAlertType("error");
            }
        }
    }

    return (
        <div className="body-content">
            <Alert type={alertType} message={message} />
            {loading && <p>Portfolio Loading ....</p>}
            {!loading && !portfolio && <p>Unable to load portfolio</p>}
            {!loading && portfolio && (
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
                            <td>{portfolio.totalUnrealizedProfitLoss}</td>
                        </tr>
                        <tr>
                            <th>totalUnrealizedProfitLossPercentage</th>
                            <td>{portfolio.totalUnrealizedProfitLossPercentage}</td>
                        </tr>
                    </tbody>
                </table>
                <h2>Portfolio Holdings</h2>
                {holdings.length === 0 && <p>No holdings found.</p>}
                {holdings.length >0 &&
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
                }
            </>
            )}
        </div>
    );
}
export default Portfolio;