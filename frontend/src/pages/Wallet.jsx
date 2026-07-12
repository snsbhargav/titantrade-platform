import { useEffect, useState } from "react";
import api from "../api/axiosConfig";

function Wallet(){

    const [balance, setBalance] = useState(0);
    const [currency, setCurrency] = useState("");
    const [message, setMessage] = useState("");
    const [depositAmount, setDepositAmount] = useState({
        amount:""
    });
    const [withdrawAmount, setWithdrawAmount] = useState({
        amount:""
    });

    const handleDepositChange = (event) => {
        const {name, value} = event.target;
        
        setDepositAmount({
            ...depositAmount,
            [name] : value
        });

    };
    const handleWithdrawChange = (event) => {
        const {name, value} = event.target;
        
        setWithdrawAmount({
            ...withdrawAmount,
            [name] : value
        });

    };

    useEffect(()=> {
        const getWallet = async() => {

            try{
                const response = await api.get("/wallet/walletBalance");
                setBalance(response?.data?.data?.balance ?? 0);
                setCurrency(response?.data?.data?.currency ?? "");
                setMessage(response?.data?.message || "");

            } catch(error){
                setMessage(error?.response?.data?.message || "Failed to load wallet page.");
            }

            
        };
        getWallet();
    }, []);

    const handleDeposit = async (event) => {
        event.preventDefault();
        try{
            const requestBody = {amount: Number(depositAmount.amount)};
            const response = await api.post("/wallet/deposit", requestBody);
            setBalance(response?.data?.data?.balance ?? balance)
            setMessage(response?.data?.message || "");

        } catch(error){
            setMessage(error?.response?.data?.message || "Deposit failed");
        }
        setDepositAmount({"amount":""});
        };

    const handleWithdraw = async (event) => {
        event.preventDefault();
        try{
            const requestBody = {amount: Number(withdrawAmount.amount)};
            const response = await api.post("/wallet/withdraw", requestBody);
            setBalance(response?.data?.data?.balance ?? balance)
            setMessage(response?.data?.message || "");
        } catch(error){
            setMessage(error?.response?.data?.message || "Withdraw failed");
        }
        setWithdrawAmount({"amount":""});
    };

    return (
        <div>
            <h1>Wallet will appear here!!</h1>
            <p>Wallet Balance : {balance} {currency}</p>
            <div className="deposit">
                <form onSubmit={handleDeposit}>
                    <label htmlFor="deposit">Deposit Amount : </label>
                    <input type="number" name="amount" id="deposit" min="0.01" step="0.01" onChange={handleDepositChange} value={depositAmount.amount} />
                    <button type="submit">Deposit</button>
                </form>
            </div>
            <div className="withdraw">
                <form onSubmit={handleWithdraw}>
                    <label htmlFor="withdraw">Withdraw Amount : </label>
                    <input type="number" name="amount" id="withdraw" min="0.01" step="0.01" onChange={handleWithdrawChange} value={withdrawAmount.amount} />
                    <button type="submit">Withdraw</button>
                </form>
            </div>
            {message && <p>{message}</p>}
        </div>
    );
}
export default Wallet;