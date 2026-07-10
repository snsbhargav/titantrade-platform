import { useState } from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import heroImg from './assets/hero.png'
import './App.css'
import Login from './pages/Login'
import Register from './pages/Register'
import Portfolio from './pages/Portfolio'
import Stocks from './pages/Stocks'
import TradeHistory from './pages/TradeHistory'
import Wallet from './pages/Wallet'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'

function App() {

  return (

    <BrowserRouter>
      <Navbar/>

      <Routes>
        <Route path='/' element={<Navigate to="/login"/>} />
        <Route path='/login' element={<Login />}/>
        <Route path='/register' element={<Register />}/>
        <Route path='/portfolio' element={<Portfolio />}/>
        <Route path='/stocks' element={<Stocks />}/>
        <Route path='/trades' element={<TradeHistory />}/>
        <Route path='/wallet' element={<Wallet />}/>
      </Routes>
    </BrowserRouter>
  )
}

export default App
