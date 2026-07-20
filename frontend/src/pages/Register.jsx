import { useState } from "react";
import api from "../api/axiosConfig";
import { Link, useNavigate } from "react-router-dom";

function Register(){

    const [formData, setFormData] = useState({
        firstName: "",
        lastName: "",
        email: "",
        password: "",
        gender:""
    });

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
            if (
                !formData.firstName ||
                !formData.lastName ||
                !formData.email ||
                !formData.password ||
                !formData.gender
            ) {
                setMessage("Please fill all required fields");
                return;
            }
            const response = await api.post("/auth/register", formData);
            setMessage(response?.data?.message);
            navigate("/login");
        } catch(error){
            setMessage(error.response?.data?.message || "Registration failed");
        }
    };

    return (
        <div className="body-content">
            <h1>Register</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="firstName" >FirstName</label>
                    <input name="firstName" id="firstName"  value={formData.firstName} onChange={handleChange}/>
                </div>
                <div>
                    <label htmlFor="lastName">LastName</label>
                    <input name="lastName" value={formData.lastName} onChange={handleChange}/>
                </div>
                <div>
                    <label htmlFor="email">Email</label>
                    <input type="email" name="email" value={formData.email} onChange={handleChange}/>
                </div>
                <div>
                    <label htmlFor="password">Password</label>
                    <input type="password" value={formData.password} name="password" onChange={handleChange} />
                </div>
                <div>
                    <label htmlFor="gender">Gender</label>
                    <select name="gender" id="gender" value={formData.gender} onChange={handleChange}>
                        <option value="">Select gender</option>
                        <option value="Male">MALE</option>
                        <option value="Female">FEMALE</option>
                        
                    </select>
                </div>
                <button type="submit">Register</button>
            </form>
            <p><Link to="/login">Already have an account? Log in</Link></p>
            {message && <p>{message}</p>}
        </div>
    );
}
export default Register;