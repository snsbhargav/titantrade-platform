import { useState } from "react";
import api from "../api/axiosConfig";
import { Link, Navigate, useNavigate } from "react-router-dom";

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
            const response = await api.post("/auth/login", formData);
            const token = response.data.data.token;
            localStorage.setItem("token", token);
            console.log(token);
            setMessage("Login Successful");
            navigate("/dashboard");
        } catch(error){
            setMessage(error.response?.data?.message || "Login failed");
        }

    };

    return (
        <div>
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