import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function Stocks(){

    const [message, setMessage] = useState("");
    const [stockList, setStockList] = useState([]);
    useEffect(() => {
        const fetchStock = async() => {

            try{
                const response = await api.get("/stocks");
                setStockList(response.data?.data?.stocks);
                setMessage(response.data?.message || "Stock Retrieved Successful");
        
            } catch(error){
                setMessage(error?.response?.data?.message || "Stocks Retrieval Failed")
    
            }
        };

        fetchStock();
        
        }, []);

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