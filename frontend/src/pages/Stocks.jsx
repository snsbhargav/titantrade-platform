import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function Stocks(){

    const [message, setMessage] = useState("");
    const [stockList, setStockList] = useState([]);
    const [quantities, setQuantities] = useState({});
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [last, setLast] = useState(false);
    const [search, setSearch] = useState("");
    
    const fetchStock = async() => {

        try{
            const response = await api.get(`/stocks?page=${page}&size=1&search=${encodeURIComponent(search)}`);
            setStockList(response.data?.data?.stocks || []);
            setTotalPages(response?.data?.data?.totalPages || 1)
            setLast(response?.data?.data?.last)
            setMessage(response.data?.message || "Stock Retrieved Successful");
    
        } catch(error){
            setMessage(error?.response?.data?.message || "Stocks Retrieval Failed")

        }
    };
    
    useEffect(() => {

        fetchStock();
        
        }, [page]);

    const handleBuyChange = (stockId, value) => {
        setQuantities({
            ...quantities,
            [stockId] : value
        });
        
    }

    const handleBuyAction = async (stockId) => {
        const quantity = quantities[stockId];
        if(quantity && Number(quantity) <= 0){
            setMessage("Enter a valid quantity value")
            return;
        }

        const  buyStockRequest = {stockId : stockId, quantity : Number(quantities[stockId])};
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
            <div className="page-header">
                <h1>Stocks</h1>
                <div className="stock-search-box">
                    <input type="text" placeholder="Search" value={search} onChange={(event) => setSearch(event.target.value)} />
                    <button onClick={() => {
                        if(page===0){
                            fetchStock();
                        }else
                            setPage(0);
                        }}>Search</button>
                </div>
            </div>
            {stockList.length===0 && <p>"No stocks found.</p>}
            {stockList.length>0 && (
            <>
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
                <div className="pagination">
                    <button name="prev" onClick={handlePrev} disabled={page===0}>Prev</button>
                    <button name="next" onClick={handleNext} disabled={last}>Next</button>
                </div>
                <div className="pagination-tail">
                    <p >Page {page+1} of {totalPages}</p>
                </div>
            </>
            )}
            {message && <p>{message}</p>}
        </div>
    );
}
export default Stocks;