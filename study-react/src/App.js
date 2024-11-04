import './App.css';
import ClassA from "./ClassA";
import DisplayComponent from "./DisplayComponent";
import {BrowserRouter as Router, Link, Route, Routes} from 'react-router-dom';
import Login from "./component/Login";
import Header from "./component/Header";
import Navigation from "./component/Navigation";
import SignUp from "./component/SignUp";

function App() {
    return (
        <div className="App">
            <Router>
                <Header/>
                <section className="App-section">
                    <Routes>
                        <Route path='/hi' element={<ClassA/>}/>
                        <Route path='/hi2' element={<DisplayComponent/>}/>
                        <Route path='/login' element={<Login/>}/>
                        <Route path='/signUp' element={<SignUp/>}/>
                    </Routes>
                </section>
                <Navigation/>
            </Router>
        </div>
    );
}

export default App;
