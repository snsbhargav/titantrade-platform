import { useState } from "react";
import api from "../api/axiosConfig";
import { Link, useNavigate } from "react-router-dom";

function Login(){
    const [formData, setFormData] = useState(
        {
            email : "",
            password : ""
        }
    );
    const [message, setMessage] = useState("");

    const handleChange = (event) => {
        const {name, value} = event.target;

        setFormData({
            ...formData,
            [name] : value
        });
    };
    const navigate = useNavigate();

    const handleSubmit = async (event) => {
        event.preventDefault();

        try{
            if(!formData.email || !formData.password){
                setMessage("Please enter email and password");
                return;
            }
            const response = await api.post("/auth/login", formData);
            const token = response.data.data.token;
            localStorage.setItem("token", token);
            setMessage("Login Successful");
            window.location.href = "/dashboard";
            // navigate("/dashboard");
        } catch(error){
            setMessage(error.response?.data?.message || "Login failed");
        }

    };

    return (
        <div className="body-content">
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="email">Email</label>
                    <input type="email" name="email" id="email" value={formData.email} onChange={handleChange}/>

                </div>
                <div>
                    <label htmlFor="password">Password</label>
                    <input type="password" name="password" id="password" value={formData.password} onChange={handleChange} />
                </div>
                <button type="submit">Login</button>

            </form>
            <p><Link to="/register">Don't have an account? Create One</Link></p>
            {message &&  <p>{message}</p>}
        </div>
    );
}

export default Login;