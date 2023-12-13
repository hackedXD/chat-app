/* @refresh reload */
import { Route, Router } from "@solidjs/router";
import { render } from "solid-js/web";

import "./global.css";

import Landing from "./pages/Landing";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import Chat from "./pages/Chat";

render(
	() => (
		<Router>
			<Route path="/" component={Landing} />
			<Route path="/login" component={Login} />
			<Route path="/signup" component={Signup} />
			<Route path="/chat" component={Chat} />
		</Router>
	),
	document.getElementById("root")!
);
