import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function TradeHistory(){

    const [tradesList, setTradesList] = useState([]);
    const [message, setMessage] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [last, setLast] = useState(false);
    const [tradeType, setTradeType] = useState("");

    const getTrades = async() => {
        try{
            let url = `/trades?page=${page}&size=10`;
            if(tradeType)
                url = url+`&tradeType=${tradeType}`;
            const response = await api.get(url);
            setTradesList(response?.data?.data?.trades || []);
            setTotalPages(response?.data?.data?.totalPages);
            setLast(response?.data?.data?.last);
            setMessage(response?.data?.message || "Trades History retrieved successful");
        } catch(error){
            setMessage(error?.response?.data?.message || "Trade history retrieval failed");
        }
    }
    useEffect(() => {
        
        getTrades();
    }, [page, tradeType]);

    const handleTradeType = (event) => {
        setTradeType(event.target.value);
        setPage(0);
    }
    return (
        <div>
            {!tradesList && <p>Loading Trade History...</p>}
            {tradesList.length === 0 && <p>No trade history found</p>}
            {tradesList && tradesList.length != 0 &&
            <>
                <h1>Trade History</h1>
                <select name="tradetype" value={tradeType} id="tradetype" onChange={handleTradeType}>
                    <option value="">ALL</option>
                    <option value="BUY">BUY</option>
                    <option value="SELL">SELL</option>
                </select>
                <table border="1">
                    <thead>
                        <tr>
                            <th>Stock Id</th>
                            <th>Ticker</th>
                            <th>Company Name</th>
                            <th>Trade Type</th>
                            <th>Quantity</th>
                            <th>Price Per Share</th>
                            <th>Total Amount</th>
                            <th>Trade Status</th>
                            <th>Executed At</th>
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
                <button name="prev" disabled={page===0}  onClick={() => {if(page!=0) setPage(page-1)}}>Prev</button>
                <button name="next" disabled={last} onClick={()=>{if(!last) setPage(page+1)}}>Next</button>
                <p>page {page+1} of {totalPages}</p>
            </>
            }
            {message && <p>{message}</p>}
        </div>
    );
}
export default TradeHistory;