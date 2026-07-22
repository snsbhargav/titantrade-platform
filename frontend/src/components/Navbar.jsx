import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Navbar(){

    const navigate = useNavigate();
    const token = localStorage.getItem("token");

    const handleLogout = (event) => {
        localStorage.removeItem("token");
        navigate("/login");
    };

    return (
        <div>
            <nav>
                {!token &&
                (
                    <>
                        <Link to="/login">Login</Link> | {" "}
                        <Link to="/register">Register</Link>
                    </>
                )
                }
                {token &&
                (
                    <>
                        <Link to="/dashboard">Dashboard</Link> | {" "}
                        <Link to="/stocks">Stocks</Link> | {" "}
                        <Link to="/portfolio">Portfolio</Link> | {" "}
                        <Link to="/trades">Trades</Link> | {" "}
                        <Link to="/wallet">Wallet</Link>
                        <Link to="/admin">Admin</Link>
                        <button type="button" onClick={handleLogout}>Logout</button>
                    </>
                )
                }
            </nav>
        </div>
    );
}
export default Navbar;