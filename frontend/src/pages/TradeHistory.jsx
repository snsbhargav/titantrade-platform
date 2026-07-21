import { useEffect, useState } from "react";
import api from "../api/axiosConfig";
import Alert from "../components/Alert";
import Pagination from "../components/Pagination";

function TradeHistory(){

    const [tradesList, setTradesList] = useState([]);
    const [message, setMessage] = useState("");
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [last, setLast] = useState(false);
    const [tradeType, setTradeType] = useState("");
    const [loading, setLoading] = useState(true);
    const [alertType, setAlertType] = useState("info");

    const getTrades = async() => {
        try{
            let url = `/trades?page=${page}&size=10`;
            if(tradeType)
                url = url+`&tradeType=${tradeType}`;
            const response = await api.get(url);
            setTradesList(response?.data?.data?.trades || []);
            setTotalPages(response?.data?.data?.totalPages);
            setLast(response?.data?.data?.last);
            setMessage("");
        } catch(error){
            setMessage(error?.response?.data?.message || "Trade history retrieval failed");
            setAlertType("error");
        }
    }
    useEffect(() => {
        
        getTrades();
    }, [page, tradeType]);

    const handleTradeType = (event) => {
        setTradeType(event.target.value);
        setPage(0);
    }
    const handlePrev = () => {
    if(page>0) {
        setPage(page-1);
    }
    }

    const handleNext = () => {
        if(!last){
            setPage(page+1);
        }
    }
    return (
        <div className="body-content">
            <div>
                <h1>Trade History</h1>
                <select name="tradetype" value={tradeType} id="tradetype" onChange={handleTradeType}>
                    <option value="">ALL</option>
                    <option value="BUY">BUY</option>
                    <option value="SELL">SELL</option>
                </select>
            </div>
            {!tradesList && <p>Loading Trade History...</p>}
            {tradesList.length === 0 && <p>No trade history found</p>}
            {tradesList && tradesList.length !== 0 &&
            <>
                
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
                <Pagination handlePrev={handlePrev} handleNext={handleNext} page={page} last={last} totalPages={totalPages} />

            </>
            }
            <Alert message={message} type={alertType}/>
        </div>
    );
}
export default TradeHistory;