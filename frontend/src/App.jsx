import { useState } from 'react'
import './App.css'
import Login from './pages/Login'
import Register from './pages/Register'
import Portfolio from './pages/Portfolio'
import Stocks from './pages/Stocks'
import TradeHistory from './pages/TradeHistory'
import Wallet from './pages/Wallet'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import Dashboard from './pages/Dashboard'
import ProtectedRoute from './components/ProtectedRoute'

function App() {

  return (

    <BrowserRouter>
      <Navbar/>

      <Routes>
        <Route path='/' element={<Navigate to="/login"/>} />
        <Route path='/login' element={<Login />}/>
        <Route path='/register' element={<Register />}/>
        <Route path='/dashboard' element={<ProtectedRoute><Dashboard/></ProtectedRoute>} />
        <Route path='/portfolio' element={<ProtectedRoute><Portfolio /></ProtectedRoute>}/>
        <Route path='/stocks' element={<ProtectedRoute><Stocks /></ProtectedRoute>}/>
        <Route path='/trades' element={<ProtectedRoute><TradeHistory /></ProtectedRoute>}/>
        <Route path='/wallet' element={<ProtectedRoute><Wallet /></ProtectedRoute>}/>
      </Routes>
    </BrowserRouter>
  )
}

export default App
