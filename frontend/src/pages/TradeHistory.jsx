import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function TradeHistory(){

    const [tradesList, setTradesList] = useState([]);
    const [message, setMessage] = useState("");

    useEffect(() => {
        const getTrades = async() => {
            try{
                const response = await api.get("/trades");
                setTradesList(response?.data?.data?.trades || []);
                setMessage(response?.data?.message || "Trades History retrieved successful");
            } catch(error){
                setMessage(error?.response?.data?.message || "Trade history retrieval failed");
            }
        }
        getTrades();
    }, []);
    return (
        <div>
            {!tradesList && <p>Loading Trade History...</p>}
            {tradesList.length === 0 && <p>No trade history found</p>}
            {tradesList && tradesList.length != 0 &&
            <>
                <h1>Trade History</h1>
                <table border="1">
                    <thead>
                        <tr>
                            <td>Stock Id</td>
                            <td>Ticker</td>
                            <td>Company Name</td>
                            <td>Trade Type</td>
                            <td>Quantity</td>
                            <td>Price Per Share</td>
                            <td>Total Amount</td>
                            <td>Trade Status</td>
                            <td>Executed At</td>
                        </tr>
                    </thead>
                    <tbody>
                        {
                            tradesList.map((trade) => (
                                
                                <tr key={trade.id}>
                                    <td>{trade.stockId}</td>
                                    <td>{trade.ticker}</td>
                                    <td>{trade.companyName}</td>
                                    <td>{trade.tradeType}</td>
                                    <td>{trade.quantity}</td>
                                    <td>{trade.pricePerShare}</td>
                                    <td>{trade.totalAmount}</td>
                                    <td>{trade.tradeStatus}</td>
                                    <td>{trade.executedAt}</td>
                                </tr>
                                
                            ))
                        }
                    </tbody>
                </table>
            </>
            }
            {message && <p>{message}</p>}
        </div>
    );
}
export default TradeHistory;