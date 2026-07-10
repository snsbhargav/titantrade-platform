import { useState } from "react";
import api from "../api/axiosConfig";

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

    const handleSubmit = async (event) => {
        event.preventDefault();

        try{
            const response = await api.post("/auth/register", formData);
            setMessage(response?.data?.message);
            console.log(response?.data?.data);
        } catch(error){
            setMessage(error.response?.data?.message || "Registration failed");
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="firstName" >FirstName</label>
                    <input name="firstName"  value={formData.firstName} onChange={handleChange}/>
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

            {message && <p>{message}</p>}
        </div>
    );
}
export default Register;