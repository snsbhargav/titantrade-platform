import { useEffect, useState } from "react";
import api from "../api/axiosConfig";
import Pagination from "../components/Pagination";

function Orders(){

    const [orderList, setOrderList] = useState([]);
    const [message, setMessage] = useState("");
    const [alertType, setAlertType] = useState("info");
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [toalPages, setTotalPages] = useState(0);
    const [last, setLast] = useState(false);

    const fetchOrders = async () => {
        try{
            setLoading(true);
            const response = await api.get(`/orders?page=${page}`);
            const ordersData = response?.data?.data;
            setOrderList(ordersData?.orders || []);
            setMessage(response?.data?.message || "Orders retrieved successfully");
            setTotalPages(ordersData?.totalPages || 1);
            setLast(ordersData?.last);
            setAlertType("success");
            console.log(orderList, last);
        } catch(error){
            setMessage(error?.response?.data.message ?? "Unable to fetch orders")
            setAlertType("error");
        } finally{
            setLoading(false);
        }
    };

    useEffect(()=> {
        fetchOrders();
    }, [page]);

    const handleNext = (event) => {
        if(!last)
            setPage(page+1);
    }

    const handlePrev = () => {
        if(page>0)
            setPage(page-1);
    }

    return (
        <>
        <h2>Orders</h2>
        {loading && <p>Orders Loading...</p>}
        {!loading && orderList.length===0 && <p>Orders loading failed</p>}
        {!loading && orderList.length !==0 &&
        <>
        <table>
            <thead>
                <th>Order Id</th>
                <th>Stock Id</th>
                <th>Trade Type</th>
                <th>Order Status</th>
                <th>Quantity</th>
                <th>Requested Price</th>
                <th>Execution Price</th>
                <th>Total Amount</th>
                <th>Rejection Reason</th>
                <th>Idempotency Key</th>
                <th>Created On</th>
                <th>Executed At</th>
            </thead>
            <tbody>
                { orderList.length!==0 &&
                    orderList.map((order) => (
                        <tr>
                        <td>{order.orderId}</td>
                        <td>{order.stockId}</td>
                        <td>{order.tradeType}</td>
                        <td>{order.orderStatus}</td>
                        <td>{order.quantity}</td>
                        <td>{order.requestedPrice}</td>
                        <td>{order.executionPrice}</td>
                        <td>{order.totalAmount}</td>
                        <td>{order.rejectionReason}</td>
                        <td>{order.idempotencyKey}</td>
                        <td>{order.createdOn}</td>
                        <td>{order.executedAt}</td>
                        </tr>
                    ))
                    
                }
            </tbody>
        </table>
        <Pagination handleNext={handleNext} handlePrev={handlePrev} last={last} page={page} totalPages={toalPages}/>
        </>
        }
        </>
    );
}

export default Orders;