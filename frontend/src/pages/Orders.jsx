import { useEffect, useState } from "react";
import api from "../api/axiosConfig";
import Pagination from "../components/Pagination";

function Orders(){

    const [orderList, setOrderList] = useState([]);
    const [message, setMessage] = useState("");
    const [alertType, setAlertType] = useState("info");
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [last, setLast] = useState(false);
    const [orderStatus, setOrderStatus] = useState("");
    const [tradeType, setTradeType] = useState("");

    const fetchOrders = async () => {
        try{
            setLoading(true);
            let url = `/orders?page=${page}&size=10`;
            if(orderStatus !== "")
                url = url+`&orderStatus=${orderStatus}`;
            if(tradeType !== "")
                url = url + `&tradeType=${tradeType}`;
            const response = await api.get(url);
            const ordersData = response?.data?.data;
            setOrderList(ordersData?.orders || []);
            setMessage(response?.data?.message || "");
            setTotalPages(ordersData?.totalPages || 1);
            setLast(ordersData?.last);
            setAlertType("success");
        } catch(error){
            setMessage(error?.response?.data.message ?? "Unable to fetch orders")
            setAlertType("error");
        } finally{
            setLoading(false);
        }
    };

    useEffect(()=> {
        fetchOrders();
    }, [page, orderStatus, tradeType]);

    const handleNext = (event) => {
        if(!last)
            setPage(page+1);
    }

    const handlePrev = () => {
        if(page>0)
            setPage(page-1);
    }

    const handleOrderStatusChange = (event) => {
        setOrderStatus(event.target.value || "");
        setPage(0);
    }
    const handleTradeTypeChange = (event) => {
        setTradeType(event.target.value || "");
        setPage(0);
    };

    return (
        <>
        <h2>Orders</h2>
        <select name="orderStatus" id="orderStatus" value={orderStatus} onChange={handleOrderStatusChange}>
            <option value="">ALL</option>
            <option value="EXECUTED">EXECUTED</option>
            <option value="PENDING">PENDING</option>
            <option value="REJECTED">REJECTED</option>
        </select>

        <select name="tradeType" value={tradeType} id="tradeType" onChange={handleTradeTypeChange}>
            <option value="">ALL</option>
            <option value="BUY">BUY</option>
            <option value="SELL">SELL</option>
        </select>
        {loading && <p>Orders Loading...</p>}
        {!loading && orderList.length===0 && <p>No orders matches the criteria</p>}
        {!loading && orderList.length !==0 &&
        <>
        <table>
            <thead>
                <tr>
                    <th>Stock Id</th>
                    <th>Trade Type</th>
                    <th>Order Status</th>
                    <th>Quantity</th>
                    <th>Requested Price</th>
                    <th>Execution Price</th>
                    <th>Total Amount</th>
                    <th>Rejection Reason</th>
                    <th>Created On</th>
                    <th>Executed At</th>
                </tr>
            </thead>
            <tbody>
                { orderList.length!==0 &&
                    orderList.map((order) => (
                        <tr key={order.orderId}>
                            <td>{order.stockId}</td>
                            <td>{order.tradeType}</td>
                            <td>{order.orderStatus}</td>
                            <td>{order.quantity}</td>
                            <td>{order.requestedPrice}</td>
                            <td>{order.executionPrice || "-"}</td>
                            <td>{order.totalAmount ?? "-"}</td>
                            <td>{order.rejectionReason || "-"}</td>
                            <td>{order.createdOn}</td>
                            <td>{order.executedAt || "-"}</td>
                        </tr>
                    ))
                    
                }
            </tbody>
        </table>
        <Pagination handleNext={handleNext} handlePrev={handlePrev} last={last} page={page} totalPages={totalPages}/>
        </>
        }
        </>
    );
}

export default Orders;