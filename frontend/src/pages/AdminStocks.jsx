import { useState } from "react";
import api from "../api/axiosConfig";
import Alert from "../components/Alert";

function AdminStocks(){

    const [formData, setFormData] = useState({
        ticker:"",
        companyName:"",
        lastKnownPrice : "",
        exchange : "",
        currency : "",
        sector : "",
        assetType : ""
    });

    const [updateStockFormData, setUpdateStockFormData] = useState({
        stockId:"",
        price:""
    });
    const [message, setMessage] = useState("");
    const [alertType, setAlertType] = useState("info");

    const handleChange = (event) => {
        const {name, value} = event.target;

        setFormData({
            ...formData,
            [name]:value
        });
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        if (
            !formData.ticker ||
            !formData.companyName ||
            !formData.lastKnownPrice ||
            Number(formData.lastKnownPrice) <= 0 ||
            !formData.exchange ||
            !formData.currency ||
            !formData.currency === "" ||
            !formData.sector ||
            !formData.assetType ||
            formData.assetType === "" 
        ) {
            setAlertType("warning");
            setMessage("Please fill all fields with valid values");
            return;
        }
        try{
            const requestbody = {
                ...formData,
                lastKnownPrice : Number(formData.lastKnownPrice)
            };
            const response = await api.post("/stocks", requestbody);
            setMessage(response?.data?.message || "Stock created!");
            setAlertType("success");
        } catch(error){
            setMessage(error?.response?.data?.message || "Stock creation failed");
            setAlertType("error");
        }
    };

    const handleUpdateStockChange = (event) => {
        const {name, value} = event.target;
        setUpdateStockFormData({
            ...updateStockFormData,
            [name] : value
        });
    };

    const handleUpdatePriceSubmit = async (event) => {
        event.preventDefault();
        if(!updateStockFormData.price || !updateStockFormData.stockId || updateStockFormData.price <=0){
            setMessage("Please enter valid inputs");
            setAlertType("info");
            return;
        }
        try{
            const requestbody = {price : Number(updateStockFormData.price)};
            const response = await api.put(`/stocks/${updateStockFormData.stockId}/price`, requestbody);
            setMessage(response?.data?.message || "Stock price updated")
            setAlertType("success");
        } catch(error){
            setMessage(error?.response?.data?.message || "Stock update failed")
        }
    };



    return (
        <div className="body-content">
            <h1>Admin</h1>
            <div className="adminForms">
                
                <form onSubmit={handleSubmit} className="createStock">
                    <h3>Create Stock Form</h3>
                    <div>
                        <label htmlFor="ticker">Ticker : </label>
                        <input name="ticker" type="text" id="ticker" value={formData.ticker} onChange={handleChange}/>
                    </div>
                    <div>
                        <label htmlFor="companyName">Company Name : </label>
                        <input name="companyName" type="text" id="companyName" value={formData.companyName}  onChange={handleChange}/>
                    </div>
                    <div>
                        <label htmlFor="lastKnownPrice">Last Known Price : </label>
                        <input name="lastKnownPrice" type="number" id="lastKnownPrice" value={formData.lastKnownPrice}  onChange={handleChange}/>
                    </div>
                    <div>
                        <label htmlFor="exchange">Exchange : </label>
                        <input name="exchange" type="text" id="exchange" value={formData.exchange}  onChange={handleChange}/>
                    </div>
                    <div>
                        <label htmlFor="currency">Currency : </label>
                        <select name="currency" id="currency"  value={formData.currency}  onChange={handleChange}>
                            <option value="">Select Currency</option>
                            <option value="USD">USD</option>
                            <option value="INR">INR</option>
                        </select>
                    </div>
                    <div>
                        <label htmlFor="sector">Sector : </label>
                        <input name="sector" type="text" id="sector" value={formData.sector}  onChange={handleChange}/>
                    </div>
                    <div>
                        <label htmlFor="assetType">AssetType : </label>
                        <select name="assetType" id="assetType"  value={formData.assetType}  onChange={handleChange}>
                            <option value="">Select Asset Type</option>
                            <option value="STOCK">STOCK</option>
                        </select>                
                    </div>
                    <div>
                        <button type="submit">Create Stock</button>
                    </div>
                    
                </form>
                <form onSubmit={handleUpdatePriceSubmit} className="updateStockPrice">
                    <h3>Update Stock Price Form</h3>
                    <div>
                        <label htmlFor="stockId">Stock Id :</label>
                        <input type="text" name="stockId" id="stockId" value={updateStockFormData.stockId} onChange={handleUpdateStockChange} />
                    </div>
                    <div>
                        <label htmlFor="price">Price : </label>
                        <input type="number" name="price" id="price" value={updateStockFormData.price} onChange={handleUpdateStockChange} />
                    </div>
                    <div>
                        <button type="submit">Update stock price</button>
                    </div>
                </form>
            </div>
            <Alert type={alertType} message={message}/>
        </div>
    );

}
export default AdminStocks;