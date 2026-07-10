import { Link } from "react-router-dom";

function Navbar(){

    return (
        <div>
            <nav>
                <Link to="/login">Login</Link> | {" "}
                <Link to="/register">Register</Link> | {" "}
                <Link to="/stocks">Stocks</Link> | {" "}
                <Link to="/portfolio">Portfolio</Link> | {" "}
                <Link to="/trades">Trades</Link> | {" "}
                <Link to="/wallet">Wallet</Link>

            </nav>
        </div>
    );
}
export default Navbar;