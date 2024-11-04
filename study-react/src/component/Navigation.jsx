import {Link} from "react-router-dom";
import './navigation.css'

function Navigation() {
    return <>
        <section className={"navigation"}>
            <div>
                <span><Link to='/hi'>hi</Link></span>
                <span><Link to='/hi2'>hi2</Link></span>
                <span><Link to='/signUp'>signUp</Link></span>
            </div>
        </section>
    </>
}

export default Navigation;