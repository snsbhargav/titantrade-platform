import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function Stocks(){

    const [message, setMessage] = useState("");
    const [stockList, setStockList] = useState([]);
    const [quantities, setQuantities] = useState({});
    useEffect(() => {
        const fetchStock = async() => {

            try{
                const response = await api.get("/stocks");
                setStockList(response.data?.data?.stocks || []);
                setMessage(response.data?.message || "Stock Retrieved Successful");
        
            } catch(error){
                setMessage(error?.response?.data?.message || "Stocks Retrieval Failed")
    
            }
        };

        fetchStock();
        
        }, []);

    const handleBuyChange = (stockId, value) => {
        setQuantities({
            ...quantities,
            [stockId] : value
        });
        
    }

    const handleBuyAction = async (stockId) => {
        const quantity = quantities[stockId];
        if(!quantity && quantity <= 0){
            setMessage("Enter a valid quantity value")
        }

        const  buyStockRequest = {"stockId" : stockId, "quantity" : quantities[stockId]};
        try{
            const response = await api.post("/trades/buy", buyStockRequest);
            setMessage(response?.data?.message || "Stock Bought successful")
            setQuantities({
                ...quantities,
                [stockId] : ""
            });
        } catch(error){
            setMessage(response?.data?.message || "Trade action failed.")
        }
    }

    return (
        <div>
            <h1>Stocks</h1>
            <table border="1">
                <thead>
                    <tr>
                        <th>Ticker</th>
                        <th>Company Name</th>
                        <th>Last Know Price</th>
                        <th>Last Price Updated At</th>
                        <th>Exchange</th>
                        <th>Currency</th>
                        <th>Sector</th>
                        <th>Asset Type</th>
                        <th>Quantity</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {
                        stockList.map((stock) => (
                                <tr key={stock.id}>
                                    <td>{stock.ticker}</td>
                                    <td>{stock.companyName}</td>
                                    <td>{stock.lastKnownPrice}</td>
                                    <td>{stock.lastPriceUpdatedAt}</td>
                                    <td>{stock.exchange}</td>
                                    <td>{stock.currency}</td>
                                    <td>{stock.sector}</td>
                                    <td>{stock.assetType}</td>
                                    <td><input type="number" name="quantity" min="0.000001" step="0.000001" id="quantity" placeholder="quantity" value={quantities[stock.id] ||  ""} onChange={(event)=> {handleBuyChange(stock.id, event.target.value)}} /></td>
                                    <td><button type="button" onClick={()=> handleBuyAction(stock.id)}>BUY</button></td>

                                </tr>
                    ))
                    }
                </tbody>

            </table>

            {message && <p>{message}</p>}
        </div>
    );
}
export default Stocks;